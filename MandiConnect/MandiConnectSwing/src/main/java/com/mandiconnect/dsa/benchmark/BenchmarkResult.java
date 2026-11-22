package com.mandiconnect.dsa.benchmark;

/**
 * One timed comparison between a MandiConnect structure and the JDK collection
 * that implements the same idea.
 *
 * <p>Accessors are all O(1) time and O(1) extra space:</p>
 * <ul>
 *   <li>{@link #algorithm()} — label shown in the Benchmarks tab</li>
 *   <li>{@link #n()} — input size</li>
 *   <li>{@link #customNanos()} — wall time of the hand-written structure</li>
 *   <li>{@link #builtinNanos()} — wall time of the JDK equivalent</li>
 *   <li>{@link #correct()} — whether the outputs matched</li>
 *   <li>{@link #complexity()} — the bound the run is meant to illustrate</li>
 * </ul>
 *
 * @param algorithm    short name, for example {@code HashTable vs HashMap}
 * @param n            input size
 * @param customNanos  elapsed nanoseconds for the custom structure
 * @param builtinNanos elapsed nanoseconds for the JDK structure
 * @param correct      true when both sides produced the same answer
 * @param complexity   asymptotic claim, for example {@code O(1) average}
 */
public record BenchmarkResult(
        String algorithm,
        int n,
        long customNanos,
        long builtinNanos,
        boolean correct,
        String complexity) {
}
