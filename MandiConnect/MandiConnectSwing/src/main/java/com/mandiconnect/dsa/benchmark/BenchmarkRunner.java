package com.mandiconnect.dsa.benchmark;

import com.mandiconnect.dsa.BidLinkedList;
import com.mandiconnect.dsa.BinarySearchTree;
import com.mandiconnect.dsa.HashTable;
import com.mandiconnect.dsa.MaxHeap;
import com.mandiconnect.dsa.QuickSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeMap;

/**
 * Benchmarks each custom ADS structure against Java's built-in equivalent
 * for input sizes N = 1000, 5000, and 10000, checking both speed and correctness.
 */
public class BenchmarkRunner {

    private static final int[] SIZES = {1000, 5000, 10000};

    /** Runs all five structure benchmarks across all three sizes. */
    public List<BenchmarkResult> runAll() {
        List<BenchmarkResult> results = new ArrayList<>();
        for (int n : SIZES) {
            results.add(benchmarkHashTable(n));
            results.add(benchmarkBst(n));
            results.add(benchmarkQuickSort(n));
            results.add(benchmarkMaxHeap(n));
            results.add(benchmarkLinkedList(n));
        }
        print(results);
        return results;
    }

    /** Prints the benchmark table to the console. */
    public static void print(List<BenchmarkResult> results) {
        System.out.println("Algorithm                  N     Custom ms  Built-in ms  Correct  Complexity");
        for (BenchmarkResult r : results) {
            System.out.printf("%-25s %5d %11.3f %12.3f %8s  %s%n",
                    r.algorithm(),
                    r.n(),
                    r.customNanos() / 1_000_000.0,
                    r.builtinNanos() / 1_000_000.0,
                    r.correct() ? "YES" : "NO",
                    r.complexity());
        }
    }

    /** Allows running benchmarks directly from the command line. */
    public static void main(String[] args) {
        new BenchmarkRunner().runAll();
    }

    /** Compares custom HashTable against Java HashMap for N inserts + 1000 lookups. */
    private BenchmarkResult benchmarkHashTable(int n) {
        // 1. Prepare N random LOT keys and 1000 random lookup indices
        Random random = new Random(1000L + n);
        String[] keys = new String[n];
        for (int i = 0; i < n; i++) {
            keys[i] = "LOT-" + Integer.toString(random.nextInt(Integer.MAX_VALUE), 36).toUpperCase();
        }
        int[] lookup = new int[1000];
        for (int i = 0; i < lookup.length; i++) {
            lookup[i] = random.nextInt(n);
        }

        // 2. Time custom HashTable
        HashTable<String> table = new HashTable<>(101);
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            table.put(keys[i], keys[i]);
        }
        String[] customHits = new String[lookup.length];
        for (int i = 0; i < lookup.length; i++) {
            customHits[i] = table.get(keys[lookup[i]]).orElse(null);
        }
        long customTime = System.nanoTime() - t0;

        // 3. Time Java HashMap
        HashMap<String, String> map = new HashMap<>();
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            map.put(keys[i], keys[i]);
        }
        String[] builtinHits = new String[lookup.length];
        for (int i = 0; i < lookup.length; i++) {
            builtinHits[i] = map.get(keys[lookup[i]]);
        }
        long builtinTime = System.nanoTime() - t0;

        // 4. Verify both returned the exact same values
        boolean correct = true;
        for (int i = 0; i < lookup.length; i++) {
            if (customHits[i] == null || !customHits[i].equals(builtinHits[i])) {
                correct = false;
                break;
            }
        }
        return new BenchmarkResult("HashTable vs HashMap", n, customTime, builtinTime, correct, "O(1) average");
    }

    /** Compares custom BinarySearchTree against Java TreeMap for N inserts + range queries. */
    private BenchmarkResult benchmarkBst(int n) {
        // 1. Prepare N shuffled prices
        double[] prices = new double[n];
        for (int i = 0; i < n; i++) {
            prices[i] = (10000.0 * i) / n;
        }
        shuffle(prices, new Random(2000L + n));

        // 2. Time custom BinarySearchTree inserts + 100 range queries
        BinarySearchTree<Double> tree = new BinarySearchTree<>();
        long t0 = System.nanoTime();
        for (double price : prices) {
            tree.insert(price, price);
        }
        List<Double> customRange = null;
        for (int q = 0; q < 100; q++) {
            customRange = tree.priceRange(2000, 5000);
        }
        long customTime = System.nanoTime() - t0;

        // 3. Time Java TreeMap inserts + 100 subMap range queries
        TreeMap<Double, Double> map = new TreeMap<>();
        t0 = System.nanoTime();
        for (double price : prices) {
            map.put(price, price);
        }
        List<Double> builtinRange = null;
        for (int q = 0; q < 100; q++) {
            builtinRange = new ArrayList<>(map.subMap(2000.0, true, 5000.0, true).values());
        }
        long builtinTime = System.nanoTime() - t0;

        // 4. Verify both range results match
        boolean correct = customRange != null && customRange.equals(builtinRange);
        return new BenchmarkResult("BST vs TreeMap", n, customTime, builtinTime, correct, "O(h + k) range");
    }

    /** Compares custom QuickSort against Java Collections.sort on N random numbers. */
    private BenchmarkResult benchmarkQuickSort(int n) {
        // 1. Prepare identical random lists
        Random random = new Random(3000L + n);
        List<Double> source = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            source.add(random.nextDouble() * 10000.0);
        }
        List<Double> customList = new ArrayList<>(source);
        List<Double> builtinList = new ArrayList<>(source);

        // 2. Time custom QuickSort
        long t0 = System.nanoTime();
        QuickSort.sort(customList, Comparator.naturalOrder());
        long customTime = System.nanoTime() - t0;

        // 3. Time Java Collections.sort
        t0 = System.nanoTime();
        Collections.sort(builtinList);
        long builtinTime = System.nanoTime() - t0;

        // 4. Verify both sorted lists match
        boolean correct = customList.equals(builtinList);
        return new BenchmarkResult("QuickSort vs sort", n, customTime, builtinTime, correct, "O(n log n) average");
    }

    /** Compares custom MaxHeap against Java PriorityQueue for N inserts + 100 max extractions. */
    private BenchmarkResult benchmarkMaxHeap(int n) {
        // 1. Prepare N random bid amounts
        Random random = new Random(4000L + n);
        double[] amounts = new double[n];
        for (int i = 0; i < n; i++) {
            amounts[i] = random.nextDouble() * 10000.0;
        }

        // 2. Time custom MaxHeap insert + top 100 extractions
        MaxHeap<Integer> heap = new MaxHeap<>();
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            heap.insert(amounts[i], i);
        }
        double[] customTop = new double[100];
        for (int i = 0; i < customTop.length; i++) {
            customTop[i] = heap.extractMax().amount;
        }
        long customTime = System.nanoTime() - t0;

        // 3. Time Java PriorityQueue (reversed so largest comes out first)
        PriorityQueue<Double> pq = new PriorityQueue<>(Comparator.reverseOrder());
        t0 = System.nanoTime();
        for (double amount : amounts) {
            pq.add(amount);
        }
        double[] builtinTop = new double[100];
        for (int i = 0; i < builtinTop.length; i++) {
            builtinTop[i] = pq.remove();
        }
        long builtinTime = System.nanoTime() - t0;

        // 4. Verify top 100 extracted values match
        boolean correct = true;
        for (int i = 0; i < customTop.length; i++) {
            if (Double.compare(customTop[i], builtinTop[i]) != 0) {
                correct = false;
                break;
            }
        }
        return new BenchmarkResult("MaxHeap vs PriorityQueue", n, customTime, builtinTime, correct, "O(log n) extract");
    }

    /** Compares custom BidLinkedList against Java LinkedList for N appends + traversal. */
    private BenchmarkResult benchmarkLinkedList(int n) {
        // 1. Time custom BidLinkedList
        BidLinkedList<Integer> customList = new BidLinkedList<>();
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            customList.add(i);
        }
        List<Integer> customOut = customList.toList();
        long customTime = System.nanoTime() - t0;

        // 2. Time Java LinkedList
        LinkedList<Integer> builtinList = new LinkedList<>();
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            builtinList.add(i);
        }
        List<Integer> builtinOut = new ArrayList<>(builtinList);
        long builtinTime = System.nanoTime() - t0;

        // 3. Verify both lists contain identical elements in order
        return new BenchmarkResult(
                "BidLinkedList vs LinkedList", n, customTime, builtinTime,
                customOut.equals(builtinOut), "O(1) append");
    }

    /** Fisher-Yates shuffle so BST receives randomized insertion order. */
    private static void shuffle(double[] values, Random random) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            double temp = values[i];
            values[i] = values[j];
            values[j] = temp;
        }
    }
}
