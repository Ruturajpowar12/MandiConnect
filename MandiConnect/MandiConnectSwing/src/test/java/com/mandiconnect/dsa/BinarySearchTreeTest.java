package com.mandiconnect.dsa;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BinarySearchTreeTest {

    @Test
    void inOrderOfSevenValuesIsSorted() {
        BinarySearchTree<Double> tree = new BinarySearchTree<>();
        double[] values = {40, 20, 60, 10, 30, 50, 70};
        for (double value : values) {
            tree.insert(value, value);
        }
        List<Double> inOrder = tree.inOrder();
        List<Double> sorted = new ArrayList<>(inOrder);
        sorted.sort(Double::compare);
        assertEquals(sorted, inOrder);
        assertEquals(7, inOrder.size());
        assertTrue(tree.visualize().contains("├──") || tree.visualize().contains("└──"));
    }

    @Test
    void rangeQueryReturnsElevenItems() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        for (int price = 0; price <= 99; price++) {
            tree.insert(price, price);
        }
        List<Integer> hits = tree.priceRange(20, 30);
        assertEquals(11, hits.size());
        assertEquals(20, hits.get(0));
        assertEquals(30, hits.get(hits.size() - 1));
    }

    @Test
    void emptyTreeHeightIsZero() {
        BinarySearchTree<String> tree = new BinarySearchTree<>();
        assertEquals(0, tree.height());
        assertEquals(0, tree.size());
        assertEquals("(empty tree)", tree.visualize());
    }

    @Test
    void insertComparisonsArePositive() {
        BinarySearchTree<String> tree = new BinarySearchTree<>();
        tree.insert(10, "a");
        tree.insert(5, "b");
        tree.insert(15, "c");
        assertTrue(tree.getInsertComparisons() > 0);
        assertTrue(tree.height() > 0);
    }
}
