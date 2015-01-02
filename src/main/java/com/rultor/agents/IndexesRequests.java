/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.agents;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.jcabi.xml.XML;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.xembly.Directives;

/**
 * Adds index to all the requests received.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class IndexesRequests implements SuperAgent {
    /**
     * Xpath expression for retrieving log entries from a talk's XML.
     */
    private static final String ARCHIVE_LOG = "//archive/log";

    /**
     * Name of the log index attribute.
     */
    private static final String INDEX = "index";

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void execute(final Talks talks) throws IOException {
        int max = this.max(talks);
        for (Talk talk : talks.active()) {
            max = this.update(talk, max);
        }
/*
        int maxTalk = this.max(talks);
        for (final Talk talk : talks.active()) {
            final List<String> requests = talk.read().xpath("//request");
            if (requests.isEmpty()) {
                int index = 0;
                final List<XML> logs = talk.read().nodes(ARCHIVE_LOG);
                if (logs.isEmpty()) {
                    index = maxTalk + 1;
                } else {
                    index = this.getMaxLogIndex(logs) + 1;
                }
                talk.modify(
                    new Directives().xpath("//talk").add("request")
                        .attr(INDEX, Integer.toString(index))
                        .attr("id", this.createRequestId())
                        .add("type").set(INDEX)
                        .up()
                        .add("args")
                );
                maxTalk += 1;
            }
        }
        */
    }

    private int update(final Talk talk, final int max) throws IOException {
        final List<String> requests = talk.read().xpath("//request");
        int newMax = max;
        if (requests.isEmpty()) {
            int index = 0;
            final List<XML> logs = talk.read().nodes(ARCHIVE_LOG);
            if (logs.isEmpty()) {
                index = max + 1;
            } else {
                index = this.max(talk) + 1;
            }
            talk.modify(
                new Directives().xpath("//talk").add("request")
                    .attr(INDEX, Integer.toString(index))
                    .attr("id", this.createRequestId())
                    .add("type").set(INDEX)
                    .up()
                    .add("args")
            );
            newMax += 1;
        }

        return newMax;
    }

    private int max(final Talk talk) throws IOException {
        final Iterable<Integer> indexes = Iterables.transform(
            talk.read()
            .xpath("/talk/archive/log/@index|/talk/request/@index"),
            new Function<String, Integer>() {
                @Override
                public Integer apply(final String input) {
                    return Integer.parseInt(input);
                }
            });
        System.out.println("indexes.iterator().hasNext(): " +
            indexes.iterator().hasNext());

        if (indexes.iterator().hasNext()) {
            return Ordering.<Integer>natural().max(indexes);
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the highest index of all log nodes in all talks contained in
     *  the talks list.
     * @param talks The list of talks to traverse.
     * @return Highest value of the index attribute of all talks in the talks
     *  list
     * @throws IOException Thrown, when problems with reading XML occur.
     */
    private int max(final Talks talks) throws IOException {
        int max = 0;
        for (final Talk talk : talks.active()) {
            final int index = max(talk);
            if (index > max) {
                max = index;
            }
        }
        return max;
    }

    /**
     * Creates a unique alphanumeric identifier.
     * @return Random unique identifier without dashes (only numbers and
     *  letters).
     */
    private String createRequestId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
