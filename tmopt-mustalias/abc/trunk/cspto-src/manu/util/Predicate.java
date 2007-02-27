/****************************************
 * 
 * Copyright (c) 2006, University of California, Berkeley.
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 * - Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the 
 *   distribution.
 * - Neither the name of the University of California, Berkeley nor the 
 *   names of its contributors may be used to endorse or promote 
 *   products derived from this software without specific prior written 
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ***************************************/ 


package manu.util;

/**
 * Interface for defining an arbitrary predicate on {@link Object}s.
 */
public abstract class Predicate<T> {
    public static final Predicate FALSE = new Predicate() {

        @Override
        public boolean test(Object obj_) {
            return false;
        }
    };
    
    public static final Predicate TRUE = FALSE.not();
    
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> truePred() {
        return (Predicate<T>)TRUE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> falsePred() {
        return (Predicate<T>)FALSE;
    }
    
    /** Test whether an {@link Object} satisfies this {@link Predicate} */
    public abstract boolean test(T obj_);

    /** Return a predicate that is a negation of this predicate */
    public Predicate<T> not() {
        final Predicate<T> originalPredicate = this;
        return new Predicate<T>() {
            public boolean test(T obj_) {
                return !originalPredicate.test(obj_);
            }
        };
    }

    /**
     * Return a predicate that is a conjunction of this predicate and another
     * predicate
     */
    public Predicate<T> and(final Predicate<T> conjunct_) {
        final Predicate<T> originalPredicate = this;
        return new Predicate<T>() {
            public boolean test(T obj_) {
                return originalPredicate.test(obj_) && conjunct_.test(obj_);
            }
        };
    }

    /**
     * Return a predicate that is a conjunction of this predicate and another
     * predicate
     */
    public Predicate<T> or(final Predicate<T> disjunct_) {
        final Predicate<T> originalPredicate = this;
        return new Predicate<T>() {
            public boolean test(T obj_) {
                return originalPredicate.test(obj_) || disjunct_.test(obj_);
            }
        };
    }
} // class Predicate

