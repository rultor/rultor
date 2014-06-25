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
package com.rultor.board;

import java.util.Arrays;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.schwering.irc.lib.IRCConnection;

/**
 * A mocked test of IRC board.
 *
 * @author Konstantin Voytenko (cppruler@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class IRCTest {
    /**
     * Sends a correct message.
     *
     * @throws Exception If some problem inside
     */
    @Test
    public void sendsMessage() throws Exception {
        final IRCServer server = Mockito.mock(
            IRCServer.class
        );
        final IRCConnection conn = Mockito.mock(IRCConnection.class);
        final String channel = "channelTest";
        final String body = "test irc message";
        final String password = "";
        final String nickname = "nickTest";
        final String username = "userTest";
        final String realname = "nameTest";
        final boolean ssl = false;
        final Bill bill = new Bill.Simple(
            body,
            nickname,
            Arrays.asList("")
        );
        Mockito.when(
            server.connect(
                Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean()
            )
        ).thenReturn(conn);
        final Billboard board = new IRC(bill, server, channel, password,
            nickname, username, realname, ssl);
        board.announce(true);
        Mockito.verify(conn).doPrivmsg(
            Mockito.argThat(
                Matchers.equalTo(
                    String.format("#%s", channel)
                )
            ),
            Mockito.argThat(
                Matchers.equalTo(
                    bill.subject()
                )
            )
        );
    }
}
