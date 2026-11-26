package com.mandiconnect.dsa;

import java.util.Comparator;
import java.util.List;

/**
 * In-place QuickSort using Lomuto partition and median-of-three pivot selection.
 * Used to sort lots by price and bids by amount in O(n log n) average time.
 */
public final class QuickSort {

    /** Simple counter object so recursive calls can count comparisons. */
    private static final class Counter {
        long count;
    }

    private QuickSort() {
    }

    /** Sorts the given list in place using the provided comparator. */
    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        sortAndCount(list, comparator);
    }

    /** Sorts the list and returns how many element comparisons were performed. */
    public static <T> long sortAndCount(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() < 2) {
            return 0L; // Already sorted if 0 or 1 element
        }
        if (comparator == null) {
            throw new NullPointerException("comparator");
        }
        Counter counter = new Counter();
        quickSort(list, 0, list.size() - 1, comparator, counter);
        return counter.count;
    }

    /** Recursively partitions the range [low, high] and sorts both halves. */
    private static <T> void quickSort(List<T> list, int low, int high, Comparator<T> cmp, Counter counter) {
        if (low >= high) {
            return;
        }
        int pivotIndex = partition(list, low, high, cmp, counter);
        quickSort(list, low, pivotIndex - 1, cmp, counter);
        quickSort(list, pivotIndex + 1, high, cmp, counter);
    }

    /**
     * Lomuto partition: moves pivot to the end, puts all elements <= pivot on the left,
     * and places the pivot in its final sorted position.
     */
    private static <T> int partition(List<T> list, int low, int high, Comparator<T> cmp, Counter counter) {
        moveMedianToEnd(list, low, high, cmp, counter);
        T pivot = list.get(high);
        int storeIndex = low;

        for (int j = low; j < high; j++) {
            counter.count++;
            if (cmp.compare(list.get(j), pivot) <= 0) {
                swap(list, storeIndex, j);
                storeIndex++;
            }
        }
        swap(list, storeIndex, high); // Put pivot into its final place
        return storeIndex;
    }

    /** Picks the median of first, middle, and last elements to avoid O(n^2) on sorted lists. */
    private static <T> void moveMedianToEnd(List<T> list, int low, int high, Comparator<T> cmp, Counter counter) {
        int mid = low + (high - low) / 2;
        if (compare(list, low, mid, cmp, counter) > 0) {
            swap(list, low, mid);
        }
        if (compare(list, mid, high, cmp, counter) > 0) {
            swap(list, mid, high);
        }
        if (compare(list, low, mid, cmp, counter) > 0) {
            swap(list, low, mid);
        }
        swap(list, mid, high); // Place chosen median at high
    }

    private static <T> int compare(List<T> list, int i, int j, Comparator<T> cmp, Counter counter) {
        counter.count++;
        return cmp.compare(list.get(i), list.get(j));
    }

    private static <T> void swap(List<T> list, int i, int j) {
        if (i == j) {
            return;
        }
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
