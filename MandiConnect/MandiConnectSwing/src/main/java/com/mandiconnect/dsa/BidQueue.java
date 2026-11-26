package com.mandiconnect.dsa;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * First-In-First-Out (FIFO) queue for incoming bids.
 * Ensures bids are processed in the exact order traders submit them.
 */
public class BidQueue<T> {

    private final LinkedList<T> items = new LinkedList<>();

    /** Adds an item to the back (tail) of the queue. Time: O(1). */
    public void enqueue(T item) {
        items.addLast(item);
    }

    /** Removes and returns the front (head) item, or null if empty. Time: O(1). */
    public T dequeue() {
        return items.isEmpty() ? null : items.removeFirst();
    }

    /** Returns the front item without removing it, or null if empty. Time: O(1). */
    public T peek() {
        return items.isEmpty() ? null : items.peekFirst();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    /** Returns a copy of waiting items from front to back. Time: O(n). */
    public List<T> snapshot() {
        return new ArrayList<>(items);
    }
}
