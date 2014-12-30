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

import com.jcabi.log.Logger;
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
    public static final String ARCHIVE_LOG = "//archive/log";

    /**
     * Name of the log index attribute.
     */
    public static final String INDEX = "index";

    @Override
    public void execute(final Talks talks) throws IOException {
        if (talks == null) {
            return;
        }
        int maxIndexOfAllTalks = this.getMaxIndexOfAllTalks(talks);
        for (final Talk talk : talks.active()) {
            final List<String> requests = talk.read().xpath("//request");
            if (requests.size() == 0) {
                int indexValue = 0;
                final List<XML> logs = talk.read().nodes(ARCHIVE_LOG);
                if (logs.isEmpty()) {
                    indexValue = maxIndexOfAllTalks + 1;
                } else {
                    final int maxLogIndex = this.getMaxLogIndex(logs);
                    indexValue = Math.max(maxLogIndex, maxIndexOfAllTalks) + 1;
                }
                this.addIndex(talk, indexValue);
                maxIndexOfAllTalks += 1;
            }
        }
    }

    /**
     * Returns the greatest index of all log children of an archive node.
     * @param logs A list of XML objects for individual log nodes (e. g.
     *  <log id="3" index="1" title="title3"/>
     * @return Highest value of the index attribute of all log nodes contained
     *  in the logs list.
     */
    private int getMaxLogIndex(final List<XML> logs) {
        int maxLogIndex = 0;
        for (final XML log : logs) {
            int curIndex = 0;
            final List<String> indexTexts = log.xpath("@index");
            if (indexTexts.size() == 1) {
                final String indexText = indexTexts.get(0);
                try {
                    curIndex = Integer.parseInt(indexText);
                } catch (final NumberFormatException exception) {
                    Logger.error(
                        this,
                        String.format(
                            "Invalid index number '%s'",
                            indexText
                        )
                    );
                }
                if (curIndex > maxLogIndex) {
                    maxLogIndex = curIndex;
                }
            }
        }
        return maxLogIndex;
    }

    /**
     * Returns the highest index of all log nodes in all talks contained in
     *  the talks list.
     * @param talks The list of talks to traverse.
     * @return Highest value of the index attribute of all talks in the talks
     *  list
     * @throws IOException Thrown, when problems with reading XML occur.
     */
    private int getMaxIndexOfAllTalks(final Talks talks) throws IOException {
        int maxIndex = 0;
        for (final Talk talk : talks.active()) {
            final int talkIndex = this.getMaxLogIndex(talk.read()
                .nodes(ARCHIVE_LOG)
            );
            if (talkIndex > maxIndex) {
                maxIndex = talkIndex;
            }
        }
        return maxIndex;
    }

    /**
     * Adds a request tag to a talk node.
     * @param talk Talk, to which the request node should be added.
     * @param index Value of the index attribute of the newly created request
     *  node.
     * @throws IOException Thrown, when problems with reading XML occur.
     */
    private void addIndex(final Talk talk, final int index) throws IOException {
        talk.modify(
            new Directives().xpath("//talk").add("request")
            .attr(INDEX, Integer.toString(index))
            .attr("id", this.createRequestId())
            .add("type").set(INDEX)
            .up()
            .add("args")
        );
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