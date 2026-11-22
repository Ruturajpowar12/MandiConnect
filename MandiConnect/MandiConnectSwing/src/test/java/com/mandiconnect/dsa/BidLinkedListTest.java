package com.mandiconnect.dsa;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BidLinkedListTest {

    @Test
    void toListMatchesInsertionOrder() {
        BidLinkedList<Integer> list = new BidLinkedList<>();
        List<Integer> expected = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
            expected.add(i);
        }
        assertEquals(100, list.size());
        assertEquals(expected, list.toList());
    }

    @Test
    void reversedMatchesReverseOrder() {
        BidLinkedList<Integer> list = new BidLinkedList<>();
        for (int i = 1; i <= 10; i++) {
            list.add(i);
        }
        List<Integer> expected = new ArrayList<>();
        for (int i = 10; i >= 1; i--) {
            expected.add(i);
        }
        assertEquals(expected, list.reversed());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), list.toList());
    }
}
