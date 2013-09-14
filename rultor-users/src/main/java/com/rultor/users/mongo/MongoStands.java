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
package com.rultor.users.mongo;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Pulses;
import com.rultor.spi.Stand;
import com.rultor.spi.Stands;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Stands in Mongo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "mongo", "origin" })
@Loggable(Loggable.DEBUG)
final class MongoStands implements Stands {

    /**
     * Mongo container.
     */
    private final transient Mongo mongo;

    /**
     * Original stands.
     */
    private final transient Stands origin;

    /**
     * Public ctor.
     * @param mng Mongo container
     * @param stands Original
     */
    protected MongoStands(final Mongo mng, final Stands stands) {
        this.mongo = mng;
        this.origin = stands;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final String name) {
        this.origin.create(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Stand> iterator() {
        final Iterator<Stand> iter = this.origin.iterator();
        return new Iterator<Stand>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }
            @Override
            public Stand next() {
                return new MongoStand(MongoStands.this.mongo, iter.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final String name) {
        return this.origin.contains(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stand get(final String name) {
        return this.origin.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pulses flow() {
        return new MongoPulses(
            this.mongo,
            new Predicate.InStands(
                Lists.newArrayList(
                    Iterables.transform(
                        this.origin,
                        new Function<Stand, String>() {
                            @Override
                            public String apply(final Stand stand) {
                                return stand.name();
                            }
                        }
                    )
                )
            ),
            new Predicate.Any()
        );
    }

}
