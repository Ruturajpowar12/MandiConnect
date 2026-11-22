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
 * Times each custom structure against the JDK collection students already know.
 *
 * <p>For every n in {1000, 5000, 10000} the runner measures HashTable vs HashMap,
 * BST vs TreeMap, QuickSort vs {@code Collections.sort}, MaxHeap vs PriorityQueue,
 * and BidLinkedList vs LinkedList. Input arrays are built before the clock starts.
 * {@code correct} is true only when the custom output matches the JDK output
 * (for sorting, that output is exactly {@code Collections.sort}).</p>
 *
 * <p>{@link #runAll()} is O(sum of the five algorithms across three sizes), dominated
 * by the n log n sorts and the BST inserts. Extra space is O(n) for the working copies.</p>
 */
public class BenchmarkRunner {

    private static final int[] SIZES = {1000, 5000, 10000};

    /**
     * Runs the full matrix and prints a text table to stdout.
     * Time is the sum of the individual benchmarks, space O(n).
     */
    public List<BenchmarkResult> runAll() {
        List<BenchmarkResult> results = new ArrayList<>();
        for (int n : SIZES) {
            results.add(hashTable(n));
            results.add(bst(n));
            results.add(quickSort(n));
            results.add(maxHeap(n));
            results.add(linkedList(n));
        }
        print(results);
        return results;
    }

    /**
     * Prints the same columns the Benchmarks tab shows. Time O(rows), space O(1).
     */
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

    /**
     * Entry point so {@code mvn exec:java} can dump the benchmark table.
     * Time dominated by {@link #runAll()}.
     */
    public static void main(String[] args) {
        new BenchmarkRunner().runAll();
    }

    private BenchmarkResult hashTable(int n) {
        String[] keys = new String[n];
        int[] lookup = new int[1000];
        Random random = new Random(1000L + n);
        for (int i = 0; i < n; i++) {
            keys[i] = "LOT-" + Long.toString(random.nextLong() & Long.MAX_VALUE, 36).toUpperCase();
        }
        for (int i = 0; i < lookup.length; i++) {
            lookup[i] = random.nextInt(n);
        }

        HashTable<String> table = new HashTable<>(101);
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            table.put(keys[i], keys[i]);
        }
        String[] customHits = new String[lookup.length];
        for (int i = 0; i < lookup.length; i++) {
            customHits[i] = table.get(keys[lookup[i]]).orElse(null);
        }
        long custom = System.nanoTime() - t0;

        HashMap<String, String> map = new HashMap<>();
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            map.put(keys[i], keys[i]);
        }
        String[] builtinHits = new String[lookup.length];
        for (int i = 0; i < lookup.length; i++) {
            builtinHits[i] = map.get(keys[lookup[i]]);
        }
        long builtin = System.nanoTime() - t0;

        boolean correct = true;
        for (int i = 0; i < lookup.length; i++) {
            if (customHits[i] == null || !customHits[i].equals(builtinHits[i])) {
                correct = false;
                break;
            }
        }
        return new BenchmarkResult("HashTable vs HashMap", n, custom, builtin, correct, "O(1) average");
    }

    private BenchmarkResult bst(int n) {
        double[] prices = new double[n];
        for (int i = 0; i < n; i++) {
            prices[i] = (10000.0 * i) / n;
        }
        shuffle(prices, new Random(2000L + n));

        BinarySearchTree<Double> tree = new BinarySearchTree<>();
        long t0 = System.nanoTime();
        for (double price : prices) {
            tree.insert(price, price);
        }
        List<Double> customRange = null;
        for (int q = 0; q < 100; q++) {
            customRange = tree.priceRange(2000, 5000);
        }
        long custom = System.nanoTime() - t0;

        TreeMap<Double, Double> map = new TreeMap<>();
        t0 = System.nanoTime();
        for (double price : prices) {
            map.put(price, price);
        }
        List<Double> builtinRange = null;
        for (int q = 0; q < 100; q++) {
            builtinRange = new ArrayList<>(map.subMap(2000.0, true, 5000.0, true).values());
        }
        long builtin = System.nanoTime() - t0;

        boolean correct = customRange != null && customRange.equals(builtinRange);
        return new BenchmarkResult("BST vs TreeMap", n, custom, builtin, correct, "O(h + k) range");
    }

    private BenchmarkResult quickSort(int n) {
        List<Double> source = new ArrayList<>(n);
        Random random = new Random(3000L + n);
        for (int i = 0; i < n; i++) {
            source.add(random.nextDouble() * 10000.0);
        }
        List<Double> customList = new ArrayList<>(source);
        List<Double> builtinList = new ArrayList<>(source);

        long t0 = System.nanoTime();
        QuickSort.sort(customList, Comparator.naturalOrder());
        long custom = System.nanoTime() - t0;

        t0 = System.nanoTime();
        Collections.sort(builtinList);
        long builtin = System.nanoTime() - t0;

        boolean correct = customList.equals(builtinList);
        return new BenchmarkResult("QuickSort vs sort", n, custom, builtin, correct, "O(n log n) average");
    }

    private BenchmarkResult maxHeap(int n) {
        double[] amounts = new double[n];
        Random random = new Random(4000L + n);
        for (int i = 0; i < n; i++) {
            amounts[i] = random.nextDouble() * 10000.0;
        }

        MaxHeap<Integer> heap = new MaxHeap<>();
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            heap.insert(amounts[i], i);
        }
        double[] customTop = new double[100];
        for (int i = 0; i < customTop.length; i++) {
            customTop[i] = heap.extractMax().amount;
        }
        long custom = System.nanoTime() - t0;

        PriorityQueue<Double> pq = new PriorityQueue<>(Comparator.reverseOrder());
        t0 = System.nanoTime();
        for (double amount : amounts) {
            pq.add(amount);
        }
        double[] builtinTop = new double[100];
        for (int i = 0; i < builtinTop.length; i++) {
            builtinTop[i] = pq.remove();
        }
        long builtin = System.nanoTime() - t0;

        boolean correct = true;
        for (int i = 0; i < customTop.length; i++) {
            if (Double.compare(customTop[i], builtinTop[i]) != 0) {
                correct = false;
                break;
            }
        }
        return new BenchmarkResult("MaxHeap vs PriorityQueue", n, custom, builtin, correct, "O(log n) extract");
    }

    private BenchmarkResult linkedList(int n) {
        BidLinkedList<Integer> customList = new BidLinkedList<>();
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            customList.add(i);
        }
        List<Integer> customOut = customList.toList();
        long custom = System.nanoTime() - t0;

        LinkedList<Integer> builtinList = new LinkedList<>();
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            builtinList.add(i);
        }
        List<Integer> builtinOut = new ArrayList<>(builtinList);
        long builtin = System.nanoTime() - t0;

        return new BenchmarkResult(
                "BidLinkedList vs LinkedList", n, custom, builtin, customOut.equals(builtinOut), "O(1) append");
    }

    private static void shuffle(double[] values, Random random) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            double tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
    }
}
