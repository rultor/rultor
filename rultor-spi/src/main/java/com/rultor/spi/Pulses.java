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

import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Vector of pulses.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Pulses extends Iterable<Time> {

    /**
     * Get a subset of this vector.
     * @param head Maximum time that is allowed in the result vector
     * @return Similar vector, that contains only pulses that are older than
     *  or equal to the provided date
     * @throws IOException If fails with some IO problem
     */
    @NotNull(message = "pulses are never NULL")
    Pulses tail(@NotNull(message = "head can't be NULL") Time head)
        throws IOException;

    /**
     * Immutable collection, based on array.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    @Loggable(Loggable.DEBUG)
    final class Array implements Pulses {
        /**
         * Encapsulated array.
         */
        private final transient Time[] times;
        /**
         * Public ctor.
         */
        public Array() {
            this(new ArrayList<Time>(0));
        }
        /**
         * Public ctor.
         * @param array Array of data
         */
        public Array(@NotNull(message = "array can't be NULL")
            final Collection<Time> array) {
            final Collection<Time> set =
                new TreeSet<Time>(Collections.reverseOrder());
            set.addAll(array);
            this.times = set.toArray(new Time[set.size()]);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull
        public Pulses tail(@NotNull(message = "head is NULL") final Time head) {
            return new Pulses.Array(
                new TreeSet<Time>(Arrays.asList(this.times)).tailSet(head)
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Time> iterator() {
            return Arrays.asList(this.times).iterator();
        }
    }

    /**
     * Sequence of pulses.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    @Loggable(Loggable.DEBUG)
    final class Sequence implements Pulses {
        /**
         * First.
         */
        private final transient Pulses first;
        /**
         * Second.
         */
        private final transient Pulses second;
        /**
         * Public ctor.
         * @param frst First
         * @param scnd Second
         */
        public Sequence(
            @NotNull(message = "first can't be NULL") final Pulses frst,
            @NotNull(message = "second can't be NULL") final Pulses scnd) {
            this.first = frst;
            this.second = scnd;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull
        public Pulses tail(
            @NotNull(message = "head can't be NULL") final Time head)
            throws IOException {
            return new Pulses.Sequence(
                this.first.tail(head), this.second.tail(head)
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public Iterator<Time> iterator() {
            return Iterators.mergeSorted(
                Arrays.asList(this.first.iterator(), this.second.iterator()),
                new Comparator<Time>() {
                    @Override
                    public int compare(final Time left, final Time right) {
                        return right.compareTo(left);
                    }
                }
            );
        }
    }

}
