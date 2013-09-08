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
package com.rultor.widget;

import com.jcabi.urn.URN;
import com.rexsl.test.XhtmlMatchers;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pageable;
import com.rultor.spi.Pulse;
import com.rultor.spi.Stand;
import com.rultor.spi.Tag;
import com.rultor.spi.Tags;
import com.rultor.spi.Widget;
import java.io.StringReader;
import java.util.Arrays;
import java.util.logging.Level;
import javax.json.Json;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.xembly.Xembler;

/**
 * Tests for {@link BuildHealth}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 */
public final class BuildHealthTest {

    /**
     * BuildHealth can find recent builds and their health.
     * @throws Exception If fails
     */
    @Test
    @SuppressWarnings("unchecked")
    public void findsBuildsAndTheirHealth() throws Exception {
        final Widget widget = new BuildHealth();
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("widget"));
        final Pulse first = Mockito.mock(Pulse.class);
        final Coordinates coords = new Coordinates.Simple(
            new URN("urn:test:3"), "rule-a"
        );
        Mockito.doReturn(coords).when(first).coordinates();
        Mockito.doReturn(
            new Tags.Simple(
                Arrays.<Tag>asList(
                    new Tag.Simple(
                        "git", Level.INFO,
                        Json.createReader(
                            new StringReader(
                                // @checkstyle LineLength (1 line)
                                "{\"hash\":\"98aeb7d\",\"author\":\"Jeff\",\"time\":\"2011-07-21T12:15:00Z\"}"
                            )
                        ).readObject(),
                        ""
                    ),
                    new Tag.Simple(
                        "on-commit", Level.SEVERE,
                        Json.createReader(
                            new StringReader("{\"code\":127,\"duration\":9870}")
                        ).readObject(),
                        ""
                    )
                )
            )
        ).when(first).tags();
        final Pulse second = Mockito.mock(Pulse.class);
        Mockito.doReturn(coords).when(second).coordinates();
        Mockito.doReturn(
            new Tags.Simple(
                Arrays.<Tag>asList(
                    new Tag.Simple(
                        "on-commit", Level.INFO,
                        Json.createReader(
                            new StringReader("{\"code\":0,\"duration\":98574}")
                        ).readObject(),
                        ""
                    )
                )
            )
        ).when(second).tags();
        final Pageable<Pulse, Coordinates> pulses =
            Mockito.mock(Pageable.class);
        Mockito.doReturn(
            Arrays.asList(first, second).iterator()
        ).when(pulses).iterator();
        final Stand stand = Mockito.mock(Stand.class);
        Mockito.doReturn(pulses).when(stand).pulses();
        new Xembler(widget.render(stand)).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/widget[title='Builds Health']",
                "/widget[width='4']",
                "/widget/builds/build/coordinates[owner='urn:test:3']",
                "/widget/builds/build/coordinates[rule='rule-a']",
                "/widget/builds/build/commit[name='98aeb7d']",
                "/widget/builds/build/commit[author='Jeff']",
                "/widget/builds/build[duration='9870']",
                "/widget/builds/build[code='127']",
                "/widget/builds/build[health='0.5']"
            )
        );
    }

}
