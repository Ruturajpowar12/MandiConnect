package com.mandiconnect.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashTableTest {

    @Test
    void insertAndRetrieveOneHundredKeys() {
        HashTable<Integer> table = new HashTable<>(32);
        for (int i = 0; i < 100; i++) {
            table.put("lot-" + i, i);
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(i, table.get("lot-" + i).orElseThrow());
        }
        assertEquals(100, table.size());
    }

    @Test
    void deleteThenGetIsEmpty() {
        HashTable<String> table = new HashTable<>(16);
        table.put("LOT-WHEAT", "wheat");
        assertTrue(table.remove("LOT-WHEAT"));
        assertTrue(table.get("LOT-WHEAT").isEmpty());
        assertFalse(table.remove("LOT-WHEAT"));
    }

    @Test
    void autoResizeGrowsCapacity() {
        HashTable<Integer> table = new HashTable<>(11);
        int before = table.capacity();
        for (int i = 0; i < 200; i++) {
            table.put("k" + i, i);
        }
        assertTrue(table.capacity() > before);
        assertEquals(200, table.size());
        assertEquals(199, table.get("k199").orElseThrow());
        assertTrue(table.loadFactor() < 0.7);
    }

    @Test
    void probesCountedAfterForcedCollisions() {
        HashTable<String> table = new HashTable<>(11);
        table.put("Aa", "first");
        table.put("BB", "second");
        assertEquals("first", table.get("Aa").orElseThrow());
        assertEquals("second", table.get("BB").orElseThrow());
        assertTrue(table.getTotalProbes() >= 0);
        assertTrue(table.getTotalProbes() > 2);
    }
}
