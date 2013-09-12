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
package com.rultor.shell.bash;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import com.rultor.shell.Batch;
import com.rultor.shell.Shells;
import com.rultor.shell.Terminal;
import com.rultor.snapshot.XemblyLine;
import com.rultor.tools.Vext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directives;

/**
 * Incremental bash batch.
 *
 * <p>This class executes bash script, constructing it from the provided
 * collection of Velocity templates. Internally it uses {@link Bash} in
 * order to execute the script, built as an aggregation of provided lines.
 * The main purpose of this class is to build a bash script that reports
 * its execution steps in Xembly format, in order to make log more
 * informative. Every line in the collection turns into a {@code step} in
 * the {@link Snapshot} built from Xembly lines.
 *
 * <p>It is recommended to use this class instead of bare {@link Bash},
 * because it provides much more logging information, in Xembly format. This
 * means that the execution of the bash script will be visible in work
 * {@link Snapshot} and rendered in stand and drain.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "shells", "commands" })
@Loggable(Loggable.DEBUG)
public final class IncrementalBash implements Batch {

    /**
     * Escaped Xembly mark.
     */
    private static final String ESCAPED_MARK =
        IncrementalBash.escape(XemblyLine.MARK);

    /**
     * Shells to be used for actual execution of bash script.
     */
    private final transient Shells shells;

    /**
     * Bash commands to execute, one by one in provided order.
     */
    private final transient Array<Vext> commands;

    /**
     * Public ctor.
     * @param shls Shells to encapsulate
     * @param cmds Bash commands to encapsulate
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public IncrementalBash(
        @NotNull(message = "shells can't be NULL") final Shells shls,
        @NotNull(message = "list of commands can't be NULL")
        final Collection<String> cmds) {
        this.shells = shls;
        final Collection<Vext> vexts = new ArrayList<Vext>(cmds.size());
        for (String cmd : cmds) {
            vexts.add(new Vext(cmd));
        }
        this.commands = new Array<Vext>(vexts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public int exec(
        @NotNull(message = "args can't be NULL") final Map<String, Object> args,
        @NotNull(message = "stream can't be NULL") final OutputStream output)
        throws IOException {
        return new Bash(this.shells, this.script(args)).exec(args, output);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "bash batch of %d command(s) through %s",
            this.commands.size(), this.shells
        );
    }

    /**
     * Make a script using custom arguments provided.
     *
     * <p>The method returns a Vext script that is a composition of
     * all commands encapsulated. This script when executed through BASH
     * outputs Xembly log lines that, when parsed, produce a {@link Snapshot}
     * with steps. Every step will have a summary, a log level, and
     * an exception if its exit code is not zero.
     *
     * @param args All arguments to inject into Velocity script
     * @return Bash script ready for execution
     */
    private String script(final Map<String, Object> args) {
        final StringBuilder script = new StringBuilder()
            .append("#set($dollar='$')")
            .append("set -o pipefail;\n")
            .append("set +o histexpand;\n")
            .append("ESCAPE=")
            .append(Terminal.quotate(Terminal.escape(IncrementalBash.escape())))
            .append(';').append('\n');
        for (Vext cmd : this.commands) {
            script.append(this.script(args, cmd));
        }
        return script.toString();
    }

    /**
     * Make a script of one BASH command.
     *
     * <p>The method converts one command in Vext format
     * to a script ready for execution in bash.
     *
     * @param args All arguments to inject into Velocity script
     * @param cmd Command in Vext format
     * @return Bash script ready for execution
     * @see http://stackoverflow.com/questions/18665603
     */
    private String script(final Map<String, Object> args, final Vext cmd) {
        final String uid = String.format("bash-%d", System.nanoTime());
        final String velocity = StringUtils.strip(cmd.velocity(), " ;");
        final String command = cmd.print(args);
        return new StringBuilder()
            .append("echo; echo ${dollar} ")
            .append(Terminal.quotate(Terminal.escape(command)))
            .append(';').append('\n')
            .append(
                this.echo(
                    new Directives()
                        .xpath("/snapshot")
                        .addIfAbsent("steps")
                        .add("step")
                        .attr("id", uid)
                        .add("start")
                        .set("${dollar}(date  -u +%Y-%m-%dT%H:%M:%SZ)")
                )
            )
            .append(';').append('\n')
            .append(this.echo(uid, command))
            .append(";\nSTART=${dollar}(date +%s%N | tr -d N);\n")
            .append("STDERR=${dollar}(mktemp /tmp/bash-XXXX);\n")
            .append("{ ")
            .append(velocity)
            .append(
                "; } 2> >(tee ${dollar}STDERR);\n"
            )
            .append("CODE=${dollar}?;\n")
            .append("sync; wait;\n")
            .append("FINISH=${dollar}(date +%s%N | tr -d N);\n")
            .append("if [ ${dollar}CODE = 0 ]; then\n  ")
            .append(
                this.echo(
                    new Directives()
                        .xpath(this.xpath(uid))
                        // @checkstyle MultipleStringLiterals (1 line)
                        .add("level")
                        .set(Level.INFO.toString())
                )
            )
            .append(";\nelse\n  ")
            .append(
                this.echo(
                    new Directives()
                        .xpath(this.xpath(uid))
                        .add("level")
                        .set(Level.SEVERE.toString())
                        .up()
                        .add("exception")
                        .add("cause")
                        .set("exit code ${dollar}CODE")
                        .up()
                        .add("stacktrace")
                        // @checkstyle LineLength (1 line)
                        .set("${dollar}(tail -100 ${dollar}STDERR | eval ${dollar}ESCAPE)")
                )
            )
            .append(";\nfi;\n")
            .append(
                this.echo(
                    new Directives()
                        .xpath(this.xpath(uid))
                        .add("finish")
                        .set("${dollar}(date -u +%Y-%m-%dT%H:%M:%SZ)")
                        .up()
                        .add("duration")
                        .set("${dollar}(((FINISH-START)/1000000))")
                )
            )
            .append(";\n")
            .append("rm -f ${dollar}STDERR;\n")
            .append("if [ ${dollar}CODE != 0 ]; then\n  ")
            .append("exit ${dollar}CODE;\nfi;\n\n")
            .toString();
    }

    /**
     * Make bash command that ECHO Xembly log line with directives.
     *
     * <p>The method creates a bash ECHO command that outputs Xembly
     * directives provided, properly escaping them before.
     *
     * @param dirs Xembly directives to be echoed
     * @return Bash command
     */
    private String echo(final Directives dirs) {
        return String.format(
            "echo -e \"%s\"",
            new XemblyLine(dirs)
                .toString()
                // @checkstyle MultipleStringLiterals (2 lines)
                .replace("\\", "\\\\\\")
                .replace("\"", "\\\"")
                .replace(XemblyLine.MARK, IncrementalBash.ESCAPED_MARK)
        );
    }

    /**
     * Echo step summary in Xembly format.
     *
     * <p>The method creates a bash command that echos step summary
     * in Xembly format. The Xembly directives echoed will add summary
     * to the previously created step, finding it by its unique ID
     * provided as the first parameter.
     *
     * @param uid Unique ID of the step
     * @param summary Summary to add to the step
     * @return Bash command
     */
    private String echo(final String uid, final String summary) {
        final String xembly = new XemblyLine(
            new Directives()
                .xpath(this.xpath(uid))
                .add("summary")
                .set(Terminal.escape(summary))
        ).toString();
        return String.format(
            "echo -e '%s'",
            xembly.replace("'", "\\x27").replace(
                XemblyLine.MARK, IncrementalBash.ESCAPED_MARK
            )
        );
    }

    /**
     * Escape Unicode chars for bash command.
     * @param text Original text
     * @return Escaped
     */
    private static String escape(final String text) {
        final StringBuilder out = new StringBuilder();
        for (byte chr : text.getBytes(Charsets.UTF_8)) {
            out.append("\\x").append(String.format("%X", chr));
        }
        return out.toString();
    }

    /**
     * XPath to find the step.
     * @param name Unique ID of the step
     * @return XPath for xembly
     */
    private String xpath(final String name) {
        return String.format("/snapshot/steps/step[@id='%s']", name);
    }

    /**
     * Escaping bash script.
     *
     * <p>The method creates a bash command that escapes sensitive characters
     * in its standard input stream (stdin) and outputs a clean text as
     * its output stream (stdout).
     *
     * @return Bash script
     */
    @Cacheable(forever = true)
    private static String escape() {
        final ImmutableMap<String, String> pairs =
            new ImmutableMap.Builder<String, String>()
                .put("\\&", "&amp;")
                .put("'\"'\"'", "&apos;")
                .put("\"", "&quot;")
                .put("<", "&lt;")
                .put(">", "&gt;")
                .build();
        final StringBuilder script = new StringBuilder().append("cat");
        for (Map.Entry<String, String> pair : pairs.entrySet()) {
            script.append(" | LANG=C sed -e 's/")
                .append(pair.getKey())
                .append("/\\")
                .append(pair.getValue())
                .append("/g'");
        }
        return script.append(" | awk 1 ORS='&#10;'")
            .append(" | LANG=C tr -cd '[:print:]'")
            .toString();
    }

}
