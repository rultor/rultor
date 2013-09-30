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
package com.rultor.shell;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wrapper of shells.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "shells")
@Loggable(Loggable.DEBUG)
public final class Shallow implements Shell {

    /**
     * Shells.
     */
    private final transient Shells shells;

    /**
     * Public ctor.
     * @param shls Shells
     */
    public Shallow(
        @NotNull(message = "shells can't be NULL") final Shells shls) {
        this.shells = shls;
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (5 lines)
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public int exec(final String command, final InputStream stdin,
        final OutputStream stdout, final OutputStream stderr)
        throws IOException {
        return this.shells.acquire().exec(command, stdin, stdout, stderr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // nothing to do;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void badge(final String name, final String value) {
        throw new UnsupportedOperationException();
    }

}
