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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.log.Logger;
import com.rexsl.test.SimpleXml;
import com.rexsl.test.XmlDocument;
import com.rultor.guard.MergeRequest;
import com.rultor.snapshot.Step;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;

/**
 * Github pull request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "github", "repository", "parameters" })
@Loggable(Loggable.DEBUG)
final class GhRequest implements MergeRequest {

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Repository.
     */
    private final transient Github.Repo repository;

    /**
     * Map of parameters.
     */
    private final transient ArrayMap<String, Object> parameters;

    /**
     * Issue ID.
     */
    private final transient int issue;

    /**
     * Public ctor.
     * @param ghub Github
     * @param rep Repository name
     * @param req Pull request from Github
     */
    protected GhRequest(final Github ghub, final Github.Repo rep,
        final PullRequest req) {
        this.github = ghub;
        this.repository = rep;
        this.parameters = new ArrayMap<String, Object>(
            new ImmutableMap.Builder<String, Object>()
                .put("issue", req.getNumber())
                .put("baseRepo", req.getBase().getRepo().getName())
                .put("baseBranch", req.getBase().getRef())
                .put("baseUser", req.getBase().getUser().getLogin())
                .put("headRepo", req.getHead().getRepo().getName())
                .put("headBranch", req.getHead().getRef())
                .put("headUser", req.getHead().getUser().getLogin())
                .put("date", new Time(req.getCreatedAt()))
                .put("title", req.getTitle())
                .build()
        );
        this.issue = req.getNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return Integer.toString(this.issue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(final int code, final String xml) {
        final XmlDocument snapshot = new SimpleXml(xml);
        try {
            if (code == 0) {
                this.accept(snapshot);
            } else {
                this.reject(snapshot);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "%s (#%d) in %s",
            this.name(),
            this.issue,
            this.repository
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> params() {
        return this.parameters;
    }

    /**
     * Accept pull request.
     * @param snapshot Snapshot XML
     * @throws IOException If fails
     */
    @Step("accepted GitHub pull request #${this.issue}")
    private void accept(final XmlDocument snapshot) throws IOException {
        final GitHubClient client = this.github.client();
        final PullRequestService svc = new PullRequestService(client);
        svc.merge(
            this.repository, this.issue,
            String.format(
                "Tested, looks correct:\n\n```\n%s\n```",
                this.summary(snapshot)
            )
        );
    }

    /**
     * Reject pull request.
     * @param snapshot Snapshot XML
     * @throws IOException If fails
     */
    @Step("rejected GitHub pull request #${this.issue}")
    private void reject(final XmlDocument snapshot) throws IOException {
        final GitHubClient client = this.github.client();
        final IssueService svc = new IssueService(client);
        svc.createComment(
            this.repository, this.issue,
            String.format(
                "Tests failed, can't merge:\n\n```\n%s\n```\n\n%s",
                this.summary(snapshot),
                snapshot.xpath("//product[name='stdout']/markdown").get(0)
            )
        );
    }

    /**
     * Make summary out of snapshot.
     * @param snapshot Snapshot XML
     * @return Summary
     */
    private String summary(final XmlDocument snapshot) {
        return new StringBuilder()
            .append("started: ")
            .append(snapshot.xpath("/snapshot/start/text()").get(0))
            .append("\n")
            .append("duration: ")
            .append(snapshot.xpath("/snapshot/duration/text()").get(0))
            .append("ms\n")
            .toString();
    }

}
