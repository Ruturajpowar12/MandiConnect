package com.mandiconnect.dsa;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxHeapTest {

    @Test
    void extractSequenceIsNonIncreasing() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        Random random = new Random(11);
        for (int i = 0; i < 200; i++) {
            heap.insert(random.nextDouble() * 1000.0, i);
        }
        double previous = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 200; i++) {
            double amount = heap.extractMax().amount;
            assertTrue(amount <= previous);
            previous = amount;
        }
        assertTrue(heap.isEmpty());
    }

    @Test
    void peekEqualsExtractOnTheSameTop() {
        MaxHeap<String> heap = new MaxHeap<>();
        heap.insert(10, "low");
        heap.insert(50, "top");
        heap.insert(25, "mid");
        MaxHeap.Item<String> peeked = heap.peekMax();
        MaxHeap.Item<String> extracted = heap.extractMax();
        assertEquals(peeked.amount, extracted.amount);
        assertEquals(peeked.data, extracted.data);
        assertEquals(2, heap.size());
    }

    @Test
    void emptyHeapReturnsNull() {
        MaxHeap<String> heap = new MaxHeap<>();
        assertTrue(heap.isEmpty());
        assertNull(heap.peekMax());
        assertNull(heap.extractMax());
        assertEquals(0, heap.asArray().length);
    }
}
