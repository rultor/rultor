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
package com.rultor.ci;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rexsl.test.SimpleXml;
import com.rultor.shell.Batch;
import com.rultor.snapshot.Snapshot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.xml.transform.dom.DOMSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Document;
import org.xembly.ImpossibleModificationException;
import org.xembly.XemblySyntaxException;

/**
 * Build.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "batch")
@Loggable(Loggable.DEBUG)
final class Build {

    /**
     * Batch to execute.
     */
    private final transient Batch batch;

    /**
     * Public ctor.
     * @param btch Batch to use
     */
    protected Build(@NotNull(message = "batch can't be NULL")
        final Batch btch) {
        this.batch = btch;
    }

    /**
     * Build and return a snapshot/XML.
     * @param args Arguments to pass to the batch
     * @return XML of snapshot
     * @throws IOException If some IO problem
     */
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public String exec(@NotNull(message = "args can't be NULL")
        final Map<String, Object> args) throws IOException {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        this.batch.exec(args, stdout);
        try {
            final Snapshot snapshot = new Snapshot(
                new ByteArrayInputStream(stdout.toByteArray())
            );
            final Document dom = Snapshot.empty();
            snapshot.apply(dom);
            return new SimpleXml(new DOMSource(dom)).toString();
        } catch (XemblySyntaxException ex) {
            throw new IOException(ex);
        } catch (ImpossibleModificationException ex) {
            throw new IOException(ex);
        }
    }

}
