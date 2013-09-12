/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.guard;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.ci.Build;
import com.rultor.shell.Batch;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.XemblyLine;
import com.rultor.spi.Instance;
import com.rultor.stateful.ConcurrentNotepad;
import com.rultor.tools.Exceptions;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;

/**
 * On pull request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "requests", "busy", "batch" })
@Loggable(Loggable.DEBUG)
public final class OnPullRequest implements Instance {

    /**
     * All available pull requests.
     */
    private final transient MergeRequests requests;

    /**
     * List of requests we're busy with at the moment.
     */
    private final transient ConcurrentNotepad busy;

    /**
     * Batch to execute.
     */
    private final transient Batch batch;

    /**
     * Public ctor.
     * @param rqsts Requests
     * @param ntp Notepad
     * @param btch Batch to use
     */
    public OnPullRequest(
        @NotNull(message = "requests can't be NULL") final MergeRequests rqsts,
        @NotNull(message = "notepad can't be NULL") final ConcurrentNotepad ntp,
        @NotNull(message = "batch can't be NULL") final Batch btch) {
        this.requests = rqsts;
        this.busy = ntp;
        this.batch = btch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        final Iterator<MergeRequest> iterator = this.requests.iterator();
        while (iterator.hasNext()) {
            final MergeRequest request = iterator.next();
            if (!this.busy.addIfAbsent(request.name())) {
                continue;
            }
            try {
                this.merge(request);
            } finally {
                this.busy.remove(request.name());
            }
        }
    }

    /**
     * Merge this pull request.
     * @param request The request to merge
     * @return TRUE if success
     * @throws IOException If IO problem
     */
    @Step(
        before = "building merge request ${args[0].name()}",
        // @checkstyle LineLength (1 line)
        value = "merge request ${args[0].name()} #if($result)built successfully#{else}failed to build#end"
    )
    private boolean merge(final MergeRequest request) throws IOException {
        final String tag = "on-pull-request";
        request.started();
        final Snapshot snapshot = new Build(tag, this.batch).exec(
            new ImmutableMap.Builder<String, Object>()
                .putAll(request.params())
                .build()
        );
        final boolean failure = this.failure(snapshot, tag);
        if (failure) {
            request.reject(snapshot);
        } else {
            request.accept(snapshot);
        }
        this.tag(request, failure);
        return !failure;
    }

    /**
     * Was it a failed merge?
     * @param snapshot Snapshot received
     * @param tag Tag to look for
     * @return TRUE if it was a failure
     */
    private boolean failure(final Snapshot snapshot, final String tag) {
        boolean failure = true;
        try {
            failure = snapshot.xml()
                .nodes(String.format("//tag[label='%s' and level='FINE']", tag))
                .isEmpty();
        } catch (ImpossibleModificationException ex) {
            Exceptions.warn(this, ex);
        }
        return failure;
    }

    /**
     * Log a tag.
     * @param request Request
     * @param failure TRUE if failed
     * @throws IOException If fails
     */
    private void tag(final MergeRequest request, final boolean failure)
        throws IOException {
        final StringWriter data = new StringWriter();
        final JsonGenerator json = Json.createGenerator(data)
            .writeStartObject()
            .write("request", request.name())
            .writeStartObject("params");
        for (Map.Entry<String, Object> entry : request.params().entrySet()) {
            json.write(entry.getKey(), entry.getValue().toString());
        }
        json.writeEnd()
            .write("failure", Boolean.toString(failure))
            .writeEnd()
            .close();
        final StringBuilder desc = new StringBuilder();
        desc.append("merge request ").append(request.name());
        if (failure) {
            desc.append(" failed");
        } else {
            desc.append(" succeeded");
        }
        new XemblyLine(
            new Directives()
                .xpath("/snapshot").strict(1).addIfAbsent("tags")
                .add("tag").add("label").set("merge").up()
                .add("level").set(Level.INFO.toString()).up()
                .add("data").set(data.toString()).up()
                .add("markdown").set(desc.toString())
        ).log();
    }

}
