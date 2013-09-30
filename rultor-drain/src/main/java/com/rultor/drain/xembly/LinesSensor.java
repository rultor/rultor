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
package com.rultor.drain.xembly;

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.snapshot.XemblyLine;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.stateful.Spinbox;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.Validate;
import org.xembly.Directives;

/**
 * Sensor of output lines (their counter).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "delta", "spinbox", "origin" })
@Loggable(Loggable.DEBUG)
public final class LinesSensor implements Drain {

    /**
     * Delta.
     */
    private final transient long delta;

    /**
     * Spinbox used for counting.
     */
    private final transient Spinbox spinbox;

    /**
     * Original drain.
     */
    private final transient Drain origin;

    /**
     * Public ctor.
     * @param dlt How many lines to sense
     * @param box Spinbox for counting
     * @param drain Original drain
     */
    public LinesSensor(
        final long dlt,
        @NotNull(message = "spinbox can't be NULL") final Spinbox box,
        @NotNull(message = "drain can't be NULL") final Drain drain) {
        Validate.isTrue(dlt > Tv.TEN, "delta %d can't be less than ten", dlt);
        this.delta = dlt;
        this.spinbox = box;
        this.origin = drain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Time, Time> pulses() throws IOException {
        return this.origin.pulses();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines) throws IOException {
        final long before = this.spinbox.add(0);
        final long after = this.spinbox.add(Iterables.size(lines));
        if ((after / this.delta) * this.delta > before) {
            this.origin.append(
                Arrays.asList(
                    new XemblyLine(
                        new Directives()
                            .xpath("/snapshot")
                            .addIf("lines")
                            .set(Long.toString(after))
                    ).toString()
                )
            );
        }
        this.origin.append(lines);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(
                String.format(
                    "LinesSensor: spinbox=%s%n",
                    this.spinbox
                ),
                CharEncoding.UTF_8
            ),
            this.origin.read()
        );
    }

}
