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

package com.rultor.guard.github;

import java.io.IOException;
import java.net.HttpURLConnection;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link ReassignTo}.
 * @author Bharath Bolisetty (bharathbolisetty@gmail.com)
 * @version $Id$
 */
public final class ReassignToTest {

    /**
     * ReassignTo can approve and reassigned to user.
     * @throws IOException If some problem inside
     */
    @Test
    public void canApproveAndReassign() throws IOException {
        final String test = "test";
        final Approval approval = new ReassignTo(test);
        final PullRequest request = new PullRequest();
        final Github github = Mockito.mock(Github.class);
        final User assignee = new User();
        assignee.setLogin(test);
        final GitHubClient client = Mockito.mock(GitHubClient.class);
        Mockito.doReturn(client).when(github).client();
        Mockito.when(client.get(Mockito.any(GitHubRequest.class)))
            .thenReturn(
                new GitHubResponse(
                    Mockito.mock(HttpURLConnection.class),
                    assignee
                )
            );
        MatcherAssert.assertThat(
            approval.has(
                request ,
                github ,
                new Github.Repo("xembly/xembly")
            ),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            request.getAssignee().getLogin(),
            Matchers.is(test)
        );
    }

    /**
     * ReassignTo assigns to and approves.
     * @throws IOException If some problem inside
     */
    @Test
    public void rejectUnAssigned() throws IOException {
        final Approval approval = new ReassignTo("");
        final PullRequest request = new PullRequest();
        final Github github = Mockito.mock(Github.class);
        MatcherAssert.assertThat(
            approval.has(
                request ,
                github ,
                new Github.Repo("xembly1/xembly")
            ),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            request.getAssignee(),
            Matchers.nullValue()
        );
    }
}
