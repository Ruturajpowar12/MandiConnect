package com.mandiconnect.dsa;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Singly Linked List with head and tail pointers.
 * Stores bid history in insertion order with O(1) append at the tail.
 */
public class BidLinkedList<T> {

    /** Single node holding data and a pointer to the next node. */
    private static final class Node<T> {
        final T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    private Node<T> head; // First (oldest) node
    private Node<T> tail; // Last (newest) node
    private int size;     // Total node count

    /** Appends a new node at the tail in O(1) time. */
    public void add(T data) {
        Node<T> node = new Node<>(data);
        if (tail == null) {
            // List is empty: both head and tail point to the new node
            head = node;
            tail = node;
        } else {
            // Link new node after tail and advance tail
            tail.next = node;
            tail = node;
        }
        size++;
    }

    /** Walks from head to tail and returns elements oldest-to-newest. Time: O(n). */
    public List<T> toList() {
        List<T> list = new ArrayList<>(size);
        for (Node<T> cur = head; cur != null; cur = cur.next) {
            list.add(cur.data);
        }
        return list;
    }

    /** Returns elements newest-to-oldest without modifying node links. Time: O(n). */
    public List<T> reversed() {
        List<T> forward = toList();
        List<T> reversed = new ArrayList<>(forward.size());
        for (int i = forward.size() - 1; i >= 0; i--) {
            reversed.add(forward.get(i));
        }
        return reversed;
    }

    public int size() {
        return size;
    }
}
