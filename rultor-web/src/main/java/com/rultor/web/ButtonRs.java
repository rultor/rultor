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

import com.google.common.net.MediaType;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.rultor.snapshot.XSLT;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.stream.StreamSource;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Health button.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Path("/b/stand/{stand:[\\w\\-]+}/")
@Loggable(Loggable.DEBUG)
public final class ButtonRs extends BaseRs {

    /**
     * Instance that retrieves XML from application.
     */
    private static final Build DEFAULT_BUILD = new Build() {
        @Override
        public String info(final URI uri) {
            try {
                return new JdkRequest(uri)
                    .header(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_XML_UTF_8.toString()
                )
                    .fetch()
                    .as(RestResponse.class)
                    .body();
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    };

    /**
     * Provider of build information.
     */
    private final transient Build build;

    /**
     * Stand name.
     */
    private transient String stand;

    /**
     * Public constructor.
     */
    public ButtonRs() {
        this(ButtonRs.DEFAULT_BUILD);
        final GraphicsEnvironment env = GraphicsEnvironment
            .getLocalGraphicsEnvironment();
        this.font(env, "http://img.rultor.com/rultor.ttf");
    }

    /**
     * Constructor.
     * @param bld Build info retriever.
     */
    public ButtonRs(final Build bld) {
        super();
        this.build = bld;
    }

    /**
     * Inject it from query.
     * @param stnd Stand name
     */
    @PathParam("stand")
    public void setStand(@NotNull(message = "stand name can't be NULL")
        final String stnd) {
        this.stand = stnd;
    }

    /**
     * Draw PNG image with build stats.
     * @param rule Rule to use.
     * @return Image generated.
     * @throws Exception In case of problems generating image.
     */
    @GET
    @Path("{rule:[\\w\\-]+}.png")
    @Produces("image/png")
    public Response pngButton(@PathParam("rule") final String rule)
        throws Exception {
        final PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(
            PNGTranscoder.KEY_WIDTH, (float) Tv.HUNDRED
        );
        transcoder.addTranscodingHint(
            PNGTranscoder.KEY_HEIGHT, (float) Tv.FIFTY
        );
        final ByteArrayOutputStream png = new ByteArrayOutputStream();
        transcoder.transcode(
            new TranscoderInput(IOUtils.toInputStream(this.svg(rule))),
            new TranscoderOutput(png)
        );
        return Response
            .ok(png.toByteArray(), MediaType.PNG.toString())
            .build();
    }

    /**
     * Draw SVG image with build stats.
     * @param rule Rule to use.
     * @return Image generated.
     * @throws Exception In case of problems generating image.
     */
    @GET
    @Path("{rule:[\\w\\-]+}.svg")
    @Produces("image/svg+xml")
    public Response svgButton(@PathParam("rule") final String rule)
        throws Exception {
        return Response
            .ok(this.svg(rule), MediaType.SVG_UTF_8.toString())
            .build();
    }

    /**
     * Create SVG from build.
     * @param rule Rule to use.
     * @return String with SVG.
     * @throws Exception In case of transformation error.
     */
    private String svg(final String rule) throws Exception {
        return new XSLT(
            new StreamSource(
                IOUtils.toInputStream(
                    StringUtils.defaultIfBlank(
                        this.build.info(
                            UriBuilder.fromUri(this.uriInfo().getBaseUri())
                                .segment("s", this.stand).build()
                        ),
                        "<page/>"
                    )
                )
            ),
            new StreamSource(
                IOUtils.toInputStream(
                    String.format(
                        IOUtils.toString(
                            this.getClass().getResourceAsStream(
                                "button.xsl"
                            )
                        ),
                        rule
                    )
                )
            )
        ).xml();
    }

    /**
     * Register a new font.
     * @param env Environment to register in.
     * @param file Font file to register.
     */
    private void font(final GraphicsEnvironment env, final String file) {
        try {
            env.registerFont(
                Font.createFont(
                    Font.TRUETYPE_FONT,
                    UriBuilder.fromPath(file)
                        .build().toURL().openStream()
                )
            );
        } catch (final FontFormatException ex) {
            throw new IllegalStateException(ex);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Retrieves build information.
     */
    public interface Build {
        /**
         * Retrieve build info.
         * @param uri Location to use.
         * @return Response.
         */
        String info(final URI uri);
    }
}
