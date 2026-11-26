package com.mandiconnect.dsa.benchmark;

/**
 * Holds the timing and correctness result of one benchmark comparison
 * between our custom ADS class and Java's built-in collection.
 */
public record BenchmarkResult(
        String algorithm,    // Name shown in table (e.g. "HashTable vs HashMap")
        int n,               // Input size tested (1000, 5000, 10000)
        long customNanos,    // Time taken by our custom structure (nanoseconds)
        long builtinNanos,   // Time taken by Java built-in structure (nanoseconds)
        boolean correct,     // True if custom output matches built-in output
        String complexity) { // Expected Big-O complexity
}
