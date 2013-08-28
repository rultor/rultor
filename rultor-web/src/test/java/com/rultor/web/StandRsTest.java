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
package com.rultor.web;

import com.rexsl.test.XhtmlMatchers;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.XSLT;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link StandRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class StandRsTest {

    /**
     * StandRs can fetch snapshot in HTML.
     * @throws Exception If some problem inside
     */
    @Test
    @org.junit.Ignore
    public void fetchesSnapshotInHtml() throws Exception {
        // todo
    }

    /**
     * StandRs can process snapshot with XSLT.
     * @throws Exception If some problem inside
     */
    @Test
    @org.junit.Ignore
    public void processesSnapshotWithXslt() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new XSLT(
                    new Snapshot(
                        new Directives()
                            .xpath("/snapshot")
                            .add("start")
                            .set("2012-08-23T15:00:00Z")
                            .toString()
                    ),
                    this.getClass().getResourceAsStream("fetch.xsl")
                ).dom()
            ),
            XhtmlMatchers.hasXPaths(
                "/xhtml:div",
                "/xhtml:div/xhtml:ul"
            )
        );
    }

}
