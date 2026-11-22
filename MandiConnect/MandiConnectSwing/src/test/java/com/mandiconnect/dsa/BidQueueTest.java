package com.mandiconnect.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BidQueueTest {

    @Test
    void fifoOrderAcrossOneHundredPairs() {
        BidQueue<Integer> queue = new BidQueue<>();
        for (int i = 0; i < 100; i++) {
            queue.enqueue(i);
        }
        assertEquals(100, queue.size());
        assertEquals(0, queue.peek());
        assertEquals(100, queue.snapshot().size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i, queue.dequeue());
        }
    }

    @Test
    void emptyQueueReturnsNull() {
        BidQueue<String> queue = new BidQueue<>();
        assertTrue(queue.isEmpty());
        assertNull(queue.dequeue());
        assertNull(queue.peek());
        assertTrue(queue.snapshot().isEmpty());
    }
}
