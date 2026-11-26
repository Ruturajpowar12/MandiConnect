package com.mandiconnect.dsa;

import java.util.Optional;

/**
 * Custom Hash Table using open addressing (linear probing) and tombstones.
 * Used to look up crop lots by their LOT-code in O(1) average time.
 */
public class HashTable<V> {

    /** Stores one key-value pair inside the table array. */
    private static final class Entry<V> {
        final String key;
        V value;
        boolean deleted; // Tombstone flag so search does not stop at deleted slots

        Entry(String key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Entry<V>[] table; // Underlying array of buckets
    private int capacity;     // Total slots in the array
    private int size;         // Number of active (non-deleted) entries
    private long totalProbes; // Counts how many slots were checked (shown in console)

    /** Creates an empty hash table with the given starting size. */
    @SuppressWarnings("unchecked")
    public HashTable(int initialCapacity) {
        if (initialCapacity < 2) {
            throw new IllegalArgumentException("initialCapacity must be >= 2");
        }
        this.capacity = initialCapacity;
        this.table = new Entry[capacity];
    }

    /** Inserts or updates a key-value pair. Resizes when load factor >= 0.7. */
    public void put(String key, V value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        // Grow table when 70% full to keep probing fast
        if ((size + 1.0) / capacity >= 0.7) {
            resize();
        }

        int index = hash(key);
        int firstDeleted = -1; // Remember first tombstone slot in case we need to insert

        // Linear probing: check index, then index+1, index+2, wrapping around
        for (int step = 0; step < capacity; step++) {
            totalProbes++;
            Entry<V> entry = table[index];

            if (entry == null) {
                // Empty slot found: reuse earlier tombstone if we saw one, else use this slot
                int target = (firstDeleted != -1) ? firstDeleted : index;
                table[target] = new Entry<>(key, value);
                size++;
                return;
            }
            if (entry.deleted) {
                // Save first tombstone position, keep searching in case key already exists later
                if (firstDeleted == -1) {
                    firstDeleted = index;
                }
            } else if (entry.key.equals(key)) {
                // Key already exists: update its value
                entry.value = value;
                return;
            }
            index = (index + 1) % capacity;
        }

        // Fallback if table had tombstones in every slot
        if (firstDeleted != -1) {
            table[firstDeleted] = new Entry<>(key, value);
            size++;
        }
    }

    /** Searches for a key using linear probing and returns its value if found. */
    public Optional<V> get(String key) {
        if (key == null) {
            return Optional.empty();
        }
        int index = hash(key);
        for (int step = 0; step < capacity; step++) {
            totalProbes++;
            Entry<V> entry = table[index];
            if (entry == null) {
                // Null means key was never inserted here -> stop search
                return Optional.empty();
            }
            if (!entry.deleted && entry.key.equals(key)) {
                return Optional.ofNullable(entry.value);
            }
            index = (index + 1) % capacity;
        }
        return Optional.empty();
    }

    /** Marks the key's slot as deleted (tombstone) so probing chains stay intact. */
    public boolean remove(String key) {
        if (key == null) {
            return false;
        }
        int index = hash(key);
        for (int step = 0; step < capacity; step++) {
            totalProbes++;
            Entry<V> entry = table[index];
            if (entry == null) {
                return false;
            }
            if (!entry.deleted && entry.key.equals(key)) {
                entry.deleted = true; // Leave tombstone instead of setting null
                entry.value = null;
                size--;
                return true;
            }
            index = (index + 1) % capacity;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    /** Ratio of stored items to total slots. */
    public double loadFactor() {
        return (double) size / capacity;
    }

    public long getTotalProbes() {
        return totalProbes;
    }

    /** Converts a string key into an array index: h = (h * 31 + char) mod capacity. */
    private int hash(String key) {
        long h = 0;
        for (int i = 0; i < key.length(); i++) {
            h = (h * 31 + key.charAt(i)) % capacity;
            if (h < 0) {
                h += capacity;
            }
        }
        return (int) h;
    }

    /** Doubles capacity to the next prime number and re-inserts all active entries. */
    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<V>[] oldTable = table;
        capacity = nextPrime(capacity * 2);
        table = new Entry[capacity];
        size = 0;

        // Re-hash every active item into the bigger table (tombstones are dropped)
        for (Entry<V> entry : oldTable) {
            if (entry != null && !entry.deleted) {
                int index = hash(entry.key);
                while (table[index] != null) {
                    totalProbes++;
                    index = (index + 1) % capacity;
                }
                totalProbes++;
                table[index] = new Entry<>(entry.key, entry.value);
                size++;
            }
        }
    }

    /** Finds the smallest prime >= n (prime sizes reduce hash collisions). */
    private static int nextPrime(int n) {
        int candidate = (n <= 2) ? 2 : (n % 2 == 0 ? n + 1 : n);
        while (!isPrime(candidate)) {
            candidate += 2;
        }
        return candidate;
    }

    private static boolean isPrime(int n) {
        if (n < 2) {
            return false;
        }
        if (n % 2 == 0) {
            return n == 2;
        }
        for (int d = 3; (long) d * d <= n; d += 2) {
            if (n % d == 0) {
                return false;
            }
        }
        return true;
    }
}
