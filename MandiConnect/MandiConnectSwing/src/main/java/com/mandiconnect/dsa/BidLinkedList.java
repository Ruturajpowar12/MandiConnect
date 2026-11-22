package com.mandiconnect.dsa;

import java.util.ArrayList;
import java.util.List;

/**
 * Singly linked list with head and tail pointers.
 *
 * <p>MandiConnect keeps the bid history of each lot in insertion order. A tail
 * pointer makes every new bid an O(1) append, which an {@code ArrayList} also
 * offers on average but not as a visible node chain. The Algorithm Console walks
 * {@link #toList()} and prints {@code [₹2600] → [₹2700] → null}. Nodes are a
 * private raw class. This list does not use {@code java.util.LinkedList}.</p>
 *
 * <pre>
 * Operation    Time     Extra space
 * add          O(1)     O(1)
 * toList       O(n)     O(n)
 * reversed     O(n)     O(n)
 * size         O(1)     O(1)
 * Structure             O(n) nodes
 * </pre>
 *
 * @param <T> element type, typically {@code Bid}
 */
public class BidLinkedList<T> {

    private static final class Node<T> {
        final T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /**
     * Appends {@code data} at the tail. Time O(1), space O(1).
     */
    public void add(T data) {
        Node<T> node = new Node<>(data);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    /**
     * Elements from head to tail, which is insertion order. Time O(n), space O(n).
     */
    public List<T> toList() {
        List<T> out = new ArrayList<>(size);
        for (Node<T> node = head; node != null; node = node.next) {
            out.add(node.data);
        }
        return out;
    }

    /**
     * Elements from tail to head. The links themselves are not mutated.
     * Time O(n), space O(n).
     */
    public List<T> reversed() {
        List<T> forward = toList();
        List<T> out = new ArrayList<>(forward.size());
        for (int i = forward.size() - 1; i >= 0; i--) {
            out.add(forward.get(i));
        }
        return out;
    }

    /**
     * Number of nodes. Time O(1), space O(1).
     */
    public int size() {
        return size;
    }
}
