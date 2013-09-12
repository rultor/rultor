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
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import com.jcabi.urn.URN;
import java.net.URI;
import javax.validation.constraints.NotNull;

/**
 * Stand.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Stand {

    /**
     * Public Amazon SQS queue where anyone can report their details.
     */
    URI QUEUE = URI.create(
        "https://sqs.us-east-1.amazonaws.com/019644334823/rultor-stands"
    );

    /**
     * Name of it.
     * @return Name
     */
    @NotNull(message = "name of stand is never NULL")
    String name();

    /**
     * Owner of it.
     * @return Owner's URN
     */
    @NotNull(message = "owner of stand is never NULL")
    URN owner();

    /**
     * Update it.
     * @param acl ACL to save
     * @param widgets Widgets spec to save
     */
    void update(
        @NotNull(message = "ACL can't be NULL") Spec acl,
        @NotNull(message = "spec of widgets is never NULL") Spec widgets);

    /**
     * Get its ACL.
     * @return Spec of ACL
     */
    @NotNull(message = "ACL is never NULL")
    Spec acl();

    /**
     * Get all pulses.
     * @return Pageable
     */
    @NotNull(message = "collection of pulses is never NULL")
    Pageable<Pulse, Coordinates> pulses();

    /**
     * Post new xembly script to the pulse of the stand.
     * @param pulse Unique pulse name
     * @param nano Order of the script in log
     * @param xembly Xembly script
     */
    void post(
        @NotNull(message = "pulse can't be NULL") Coordinates pulse,
        long nano,
        @NotNull(message = "text can't be NULL") String xembly);

    /**
     * Get spec of widgets.
     * @return Spec of array of widgets
     */
    @NotNull(message = "spec of widgets is never NULL")
    Spec widgets();

    /**
     * When Xembly can't be accepted.
     */
    final class BrokenXemblyException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 1L;
        /**
         * Public ctor.
         * @param cause Cause of it
         */
        public BrokenXemblyException(final Throwable cause) {
            super(cause);
        }
    }

}
