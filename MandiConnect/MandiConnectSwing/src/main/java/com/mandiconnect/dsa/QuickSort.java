package com.mandiconnect.dsa;

import java.util.Comparator;
import java.util.List;

/**
 * In-place QuickSort with Lomuto partition and a <strong>median-of-three</strong> pivot.
 *
 * <p>MandiConnect sorts available lots by expected price and the bids on a lot by
 * amount. Median-of-three (first, middle, last) avoids the classic O(n²) collapse
 * on an already sorted price list, which is exactly what a BST in-order walk
 * would hand the sorter. The algorithm writes back into the caller's list and
 * allocates only the recursion stack, so it is cache-friendlier than MergeSort
 * for the lot tables the UI shows.</p>
 *
 * <pre>
 * Operation     Best         Average      Worst        Extra space
 * sort          O(n log n)   O(n log n)   O(n²)        O(log n) stack, O(1) heap
 * </pre>
 *
 * The worst case is still quadratic on adversarial input. Sorted and random
 * inputs used by the app and the benchmark stay in the n log n band.
 * Comparisons are counted by {@link #sortAndCount(List, Comparator)}.
 */
public final class QuickSort {

    private QuickSort() {
    }

    /**
     * Sorts {@code list} in ascending order of {@code comparator}.
     * Average time O(n log n), worst O(n²), extra space O(log n) stack.
     * Empty and single-element lists are left unchanged.
     * The list must be mutable (index set/get).
     */
    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        sortAndCount(list, comparator);
    }

    /**
     * Sorts {@code list} and returns how many comparator calls the sort performed.
     * Average time O(n log n), worst O(n²), extra space O(log n).
     * Returns 0 for a null, empty, or single-element list.
     */
    public static <T> long sortAndCount(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() < 2) {
            return 0L;
        }
        if (comparator == null) {
            throw new NullPointerException("comparator");
        }
        long[] comparisons = new long[1];
        sort(list, 0, list.size() - 1, comparator, comparisons);
        return comparisons[0];
    }

    private static <T> void sort(List<T> list, int lo, int hi, Comparator<T> comparator, long[] comparisons) {
        if (lo >= hi) {
            return;
        }
        int pivot = partition(list, lo, hi, comparator, comparisons);
        sort(list, lo, pivot - 1, comparator, comparisons);
        sort(list, pivot + 1, hi, comparator, comparisons);
    }

    /**
     * Places the median of lo/mid/hi at the end, then runs Lomuto partition.
     * Time O(hi - lo), space O(1).
     */
    private static <T> int partition(List<T> list, int lo, int hi, Comparator<T> comparator, long[] comparisons) {
        medianOfThree(list, lo, hi, comparator, comparisons);
        T pivot = list.get(hi);
        int store = lo;
        for (int j = lo; j < hi; j++) {
            comparisons[0]++;
            if (comparator.compare(list.get(j), pivot) <= 0) {
                swap(list, store, j);
                store++;
            }
        }
        swap(list, store, hi);
        return store;
    }

    private static <T> void medianOfThree(List<T> list, int lo, int hi, Comparator<T> comparator, long[] comparisons) {
        int mid = lo + (hi - lo) / 2;
        if (compare(list, lo, mid, comparator, comparisons) > 0) {
            swap(list, lo, mid);
        }
        if (compare(list, mid, hi, comparator, comparisons) > 0) {
            swap(list, mid, hi);
        }
        if (compare(list, lo, mid, comparator, comparisons) > 0) {
            swap(list, lo, mid);
        }
        swap(list, mid, hi);
    }

    private static <T> int compare(List<T> list, int i, int j, Comparator<T> comparator, long[] comparisons) {
        comparisons[0]++;
        return comparator.compare(list.get(i), list.get(j));
    }

    private static <T> void swap(List<T> list, int i, int j) {
        if (i == j) {
            return;
        }
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }
}
