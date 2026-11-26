package com.mandiconnect.dsa;

import java.util.Arrays;

/**
 * Array-based Binary Max-Heap ordered by bid amount.
 * Index 0 is always the highest bid, giving O(1) peekMax and O(log n) insert/extract.
 * Parent = (i - 1) / 2, Left child = 2 * i + 1, Right child = 2 * i + 2.
 */
public class MaxHeap<T> {

    /** Pairs a numeric bid amount (priority) with its payload object (Bid). */
    public static final class Item<T> {
        public final double amount;
        public final T data;

        public Item(double amount, T data) {
            this.amount = amount;
            this.data = data;
        }
    }

    private Item<T>[] items;   // Dynamic array storing the heap tree
    private int size;          // Number of items currently in the heap
    private long siftUpSwaps;   // Swap counter for console display
    private long siftDownSwaps; // Swap counter for console display

    @SuppressWarnings("unchecked")
    public MaxHeap() {
        this.items = new Item[16];
    }

    /** Adds an item at the end and sifts it up to restore max-heap property. Time: O(log n). */
    public void insert(double amount, T data) {
        if (size == items.length) {
            items = Arrays.copyOf(items, items.length * 2); // Double array capacity when full
        }
        items[size] = new Item<>(amount, data);
        siftUp(size);
        size++;
    }

    /** Returns the highest item (root at index 0) without removing it. Time: O(1). */
    public Item<T> peekMax() {
        return size == 0 ? null : items[0];
    }

    /** Removes and returns the highest item, then sifts the new root down. Time: O(log n). */
    public Item<T> extractMax() {
        if (size == 0) {
            return null;
        }
        Item<T> max = items[0];
        size--;
        items[0] = items[size]; // Move last element to root
        items[size] = null;
        if (size > 0) {
            siftDown(0); // Push root down until heap order is restored
        }
        return max;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public long getSiftUpSwaps() {
        return siftUpSwaps;
    }

    public long getSiftDownSwaps() {
        return siftDownSwaps;
    }

    /** Returns a copy of the heap amounts in array order for console display. */
    public double[] asArray() {
        double[] copy = new double[size];
        for (int i = 0; i < size; i++) {
            copy[i] = items[i].amount;
        }
        return copy;
    }

    /** Moves a newly inserted element upward while it is larger than its parent. */
    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (items[parent].amount >= items[index].amount) {
                break;
            }
            swap(parent, index);
            siftUpSwaps++;
            index = parent;
        }
    }

    /** Moves the root downward by swapping with its larger child until heap order holds. */
    private void siftDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int largest = index;

            if (left < size && items[left].amount > items[largest].amount) {
                largest = left;
            }
            if (right < size && items[right].amount > items[largest].amount) {
                largest = right;
            }
            if (largest == index) {
                return; // Both children are smaller or missing
            }
            swap(index, largest);
            siftDownSwaps++;
            index = largest;
        }
    }

    private void swap(int i, int j) {
        Item<T> temp = items[i];
        items[i] = items[j];
        items[j] = temp;
    }
}
