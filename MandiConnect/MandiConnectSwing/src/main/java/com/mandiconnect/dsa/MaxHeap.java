package com.mandiconnect.dsa;

/**
 * 0-indexed array binary max-heap ordered by a {@code double} amount.
 *
 * <p>Each lot in MandiConnect has a heap of bids. The root is the highest offer,
 * so closing an auction reads {@link #peekMax()} instead of sorting the bid list.
 * Parent of {@code i} is {@code (i - 1) / 2}, left child is {@code 2i + 1}, right
 * child is {@code 2i + 2}. The backing store is a raw array that doubles when full.
 * This class does not use {@code java.util.PriorityQueue}.</p>
 *
 * <pre>
 * Operation      Best / Average / Worst     Extra space
 * insert         O(log n)                   O(1)
 * peekMax        O(1)                       O(1)
 * extractMax     O(log n)                   O(1)
 * asArray        O(n)                       O(n)
 * Structure                                 O(n)
 * </pre>
 *
 * {@link #getSiftUpSwaps()} and {@link #getSiftDownSwaps()} count element exchanges
 * so the Algorithm Console can show the heap repairing itself.
 *
 * @param <T> payload type, typically {@code Bid}
 */
public class MaxHeap<T> {

    /**
     * One heap entry. {@code amount} is the priority; larger amounts sit closer to the root.
     *
     * @param <T> payload type
     */
    public static final class Item<T> {
        /** Priority. Higher wins. */
        public final double amount;
        /** Application payload stored with the priority. */
        public final T data;

        /**
         * Time O(1), space O(1).
         */
        public Item(double amount, T data) {
            this.amount = amount;
            this.data = data;
        }
    }

    private Item<T>[] items;
    private int size;
    private long siftUpSwaps;
    private long siftDownSwaps;

    /**
     * Empty heap with room for 16 entries before the first doubling.
     * Time O(1), space O(1).
     */
    @SuppressWarnings("unchecked")
    public MaxHeap() {
        this.items = new Item[16];
    }

    /**
     * Inserts {@code data} with priority {@code amount} and sifts it up.
     * Time O(log n), space O(1) aside from an occasional O(n) array doubling.
     */
    public void insert(double amount, T data) {
        if (size == items.length) {
            grow();
        }
        items[size] = new Item<>(amount, data);
        siftUp(size);
        size++;
    }

    /**
     * Root item, or {@code null} when the heap is empty. Does not remove it.
     * Time O(1), space O(1).
     */
    public Item<T> peekMax() {
        if (size == 0) {
            return null;
        }
        return items[0];
    }

    /**
     * Removes and returns the maximum, or {@code null} when the heap is empty.
     * Time O(log n), space O(1).
     */
    public Item<T> extractMax() {
        if (size == 0) {
            return null;
        }
        Item<T> max = items[0];
        size--;
        items[0] = items[size];
        items[size] = null;
        if (size > 0) {
            siftDown(0);
        }
        return max;
    }

    /**
     * Number of entries. Time O(1), space O(1).
     */
    public int size() {
        return size;
    }

    /**
     * True when {@link #size()} is 0. Time O(1), space O(1).
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Swaps performed by sift-up since construction. Time O(1), space O(1).
     */
    public long getSiftUpSwaps() {
        return siftUpSwaps;
    }

    /**
     * Swaps performed by sift-down since construction. Time O(1), space O(1).
     */
    public long getSiftDownSwaps() {
        return siftDownSwaps;
    }

    /**
     * Heap-order copy of the priorities, index 0 at the root.
     * Time O(n), space O(n). Used by the Algorithm Console.
     */
    public double[] asArray() {
        double[] copy = new double[size];
        for (int i = 0; i < size; i++) {
            copy[i] = items[i].amount;
        }
        return copy;
    }

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
                return;
            }
            swap(index, largest);
            siftDownSwaps++;
            index = largest;
        }
    }

    private void swap(int i, int j) {
        Item<T> tmp = items[i];
        items[i] = items[j];
        items[j] = tmp;
    }

    @SuppressWarnings("unchecked")
    private void grow() {
        Item<T>[] bigger = new Item[items.length * 2];
        System.arraycopy(items, 0, bigger, 0, items.length);
        items = bigger;
    }
}
