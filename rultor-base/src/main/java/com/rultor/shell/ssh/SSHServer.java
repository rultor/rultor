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
package com.rultor.shell.ssh;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.env.Environment;
import com.rultor.shell.Shell;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Single SSH Server.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "env", "key" })
@Loggable(Loggable.DEBUG)
public final class SSHServer implements Shell {

    /**
     * Environment.
     */
    private final transient Environment env;

    /**
     * User name.
     */
    private final transient String login;

    /**
     * Private SSH key.
     */
    private final transient PrivateKey key;

    /**
     * Public ctor.
     * @param environ Environment
     * @param user Login
     * @param priv Private SSH key
     */
    public SSHServer(
        @NotNull(message = "env can't be NULL") final Environment environ,
        @NotNull(message = "user name can't be NULL") final String user,
        @NotNull(message = "private key can't be NULL") final PrivateKey priv) {
        this.env = environ;
        this.login = user;
        this.key = priv;
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (10 lines)
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = 1, unit = TimeUnit.HOURS)
    public int exec(
        @NotNull(message = "command can't be NULL") final String command,
        @NotNull(message = "stdin can't be NULL") final InputStream stdin,
        @NotNull(message = "stdout can't be NULL") final OutputStream stdout,
        @NotNull(message = "stderr can't be NULL") final OutputStream stderr)
        throws IOException {
        return new SSHChannel(this.env.address(), this.login, this.key)
            .exec(command, stdin, stdout, stderr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.env.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void badge(final String name, final String value) {
        throw new UnsupportedOperationException();
    }

}
