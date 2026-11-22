package com.mandiconnect.dsa;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickSortTest {

    @Test
    void randomFiveHundredMatchesCollectionsSort() {
        List<Integer> data = new ArrayList<>();
        Random random = new Random(7);
        for (int i = 0; i < 500; i++) {
            data.add(random.nextInt(10_000) - 5_000);
        }
        List<Integer> expected = new ArrayList<>(data);
        Collections.sort(expected);
        QuickSort.sort(data, Comparator.naturalOrder());
        assertEquals(expected, data);
    }

    @Test
    void alreadySortedStaysSorted() {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            data.add(i);
        }
        QuickSort.sort(data, Comparator.naturalOrder());
        for (int i = 0; i < 100; i++) {
            assertEquals(i, data.get(i));
        }
    }

    @Test
    void emptyAndSingleElementAreNoOps() {
        List<Integer> empty = new ArrayList<>();
        List<Integer> one = new ArrayList<>(List.of(42));
        QuickSort.sort(empty, Comparator.naturalOrder());
        QuickSort.sort(one, Comparator.naturalOrder());
        assertTrue(empty.isEmpty());
        assertEquals(List.of(42), one);
        assertEquals(0L, QuickSort.sortAndCount(empty, Comparator.naturalOrder()));
        assertEquals(0L, QuickSort.sortAndCount(new ArrayList<>(List.of(1)), Comparator.naturalOrder()));
    }

    @Test
    void sortAndCountIsPositive() {
        List<Integer> data = new ArrayList<>(List.of(3, 1, 2, 5, 4));
        long comparisons = QuickSort.sortAndCount(data, Comparator.naturalOrder());
        assertTrue(comparisons > 0);
        assertEquals(List.of(1, 2, 3, 4, 5), data);
    }
}
