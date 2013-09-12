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
package com.rultor.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * S3 client.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface S3Client {

    /**
     * Get AWS S3 client.
     * @return Get it
     */
    AmazonS3 get();

    /**
     * Bucket name.
     * @return Name of it
     */
    String bucket();

    /**
     * Simple client.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "key", "secret", "bkt" })
    @Loggable(Loggable.DEBUG)
    final class Simple implements S3Client {
        /**
         * Key.
         */
        private final transient String key;
        /**
         * Secret.
         */
        private final transient String secret;
        /**
         * Bucket name.
         */
        private final transient String bkt;
        /**
         * Public ctor.
         * @param akey AWS key
         * @param scrt AWS secret
         * @param bucket S3 bucket
         */
        public Simple(
            @NotNull(message = "AWS key can't be NULL") final String akey,
            @NotNull(message = "AWS secret can't be NULL") final String scrt,
            @NotNull(message = "S3 bucket can't be NULL") final String bucket) {
            this.key = akey;
            this.secret = scrt;
            this.bkt = bucket;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public AmazonS3 get() {
            return new AmazonS3Client(
                new BasicAWSCredentials(this.key, this.secret)
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String bucket() {
            return this.bkt;
        }
    }

}
