package com.mandiconnect.dsa;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * FIFO queue of incoming bids.
 *
 * <p>MandiConnect enqueues a bid as soon as the trader submits it and dequeues it
 * for persistence. First-come-first-served is the fairness rule when two traders
 * bid in the same moment: the queue, not hash iteration order, decides who is
 * recorded first. A custom circular array would only re-implement {@code add}/{@code remove}
 * with the same O(1) bounds, so this class is a thin wrapper over
 * {@code java.util.LinkedList}. That wrapper is the one place a library list is
 * permitted in the DSA layer.</p>
 *
 * <pre>
 * Operation    Time     Extra space
 * enqueue      O(1)     O(1)
 * dequeue      O(1)     O(1)
 * peek         O(1)     O(1)
 * isEmpty      O(1)     O(1)
 * size         O(1)     O(1)
 * snapshot     O(n)     O(n)
 * Structure             O(n)
 * </pre>
 *
 * @param <T> queued element, typically {@code Bid}
 */
public class BidQueue<T> {

    private final LinkedList<T> items = new LinkedList<>();

    /**
     * Adds {@code item} at the tail. Time O(1), space O(1).
     */
    public void enqueue(T item) {
        items.addLast(item);
    }

    /**
     * Removes and returns the head, or {@code null} when the queue is empty.
     * Time O(1), space O(1).
     */
    public T dequeue() {
        if (items.isEmpty()) {
            return null;
        }
        return items.removeFirst();
    }

    /**
     * Returns the head without removing it, or {@code null} when the queue is empty.
     * Time O(1), space O(1).
     */
    public T peek() {
        if (items.isEmpty()) {
            return null;
        }
        return items.peekFirst();
    }

    /**
     * True when no bids are waiting. Time O(1), space O(1).
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Number of waiting bids. Time O(1), space O(1).
     */
    public int size() {
        return items.size();
    }

    /**
     * Copy of the waiting bids from head to tail. Time O(n), space O(n).
     * The returned list is detached from the queue.
     */
    public List<T> snapshot() {
        return new ArrayList<>(items);
    }
}
