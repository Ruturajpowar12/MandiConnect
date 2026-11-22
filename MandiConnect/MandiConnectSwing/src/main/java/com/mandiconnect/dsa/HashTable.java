package com.mandiconnect.dsa;

import java.util.Optional;

/**
 * Open-addressed hash table with <strong>linear probing</strong> and tombstones.
 *
 * <p>MandiConnect stores every crop lot under its {@code LOT-} code. Traders and the
 * Algorithm Console resolve a code with {@link #get(String)} instead of scanning the
 * lot table. The polynomial hash {@code h = (h * 31 + c) mod capacity} spreads short
 * alphanumeric codes across the buckets.</p>
 *
 * <p>When the live load factor reaches 0.7 the table rehashes into
 * {@code nextPrime(capacity * 2)} so the average probe length stays constant.
 * Deleted slots become tombstones: search walks past them, and the next insert
 * reuses the first tombstone it saw. The core storage is a raw array. This class
 * does not use {@code java.util.HashMap}.</p>
 *
 * <pre>
 * Operation   Average    Worst     Extra space
 * put         O(1)       O(n)      O(1)  (resize is O(n) time and space)
 * get         O(1)       O(n)      O(1)
 * remove      O(1)       O(n)      O(1)
 * resize      O(n)       O(n)      O(n)
 * Structure                         O(n) slots
 * </pre>
 *
 * @param <V> payload type, typically {@code Lot}
 */
public class HashTable<V> {

    private static final class Entry<V> {
        final String key;
        V value;
        boolean tombstone;

        Entry(String key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Entry<V>[] table;
    private int capacity;
    private int size;
    private long totalProbes;

    /**
     * Allocates a table of the requested capacity.
     * Time O(capacity), space O(capacity).
     *
     * @param initialCapacity number of slots, at least 2
     */
    @SuppressWarnings("unchecked")
    public HashTable(int initialCapacity) {
        if (initialCapacity < 2) {
            throw new IllegalArgumentException("initialCapacity must be >= 2");
        }
        this.capacity = initialCapacity;
        this.table = new Entry[capacity];
    }

    /**
     * Inserts or replaces the value stored under {@code key}.
     * Average O(1), worst O(n) when every slot is probed. Resize is O(n).
     *
     * @param key   non-null lot code or other string key
     * @param value payload; may be null
     */
    public void put(String key, V value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if ((size + 1.0) / capacity >= 0.7) {
            resize();
        }
        int idx = hash(key);
        int firstTombstone = -1;
        for (int n = 0; n < capacity; n++) {
            totalProbes++;
            Entry<V> entry = table[idx];
            if (entry == null) {
                int slot = firstTombstone >= 0 ? firstTombstone : idx;
                table[slot] = new Entry<>(key, value);
                size++;
                return;
            }
            if (entry.tombstone) {
                if (firstTombstone < 0) {
                    firstTombstone = idx;
                }
            } else if (entry.key.equals(key)) {
                entry.value = value;
                return;
            }
            idx = (idx + 1) % capacity;
        }
        if (firstTombstone >= 0) {
            table[firstTombstone] = new Entry<>(key, value);
            size++;
            return;
        }
        resize();
        put(key, value);
    }

    /**
     * Looks the key up with linear probing.
     * Average O(1), worst O(n). Returns an empty Optional when the key is absent.
     * Each examined slot increments {@link #getTotalProbes()}.
     */
    public Optional<V> get(String key) {
        if (key == null) {
            return Optional.empty();
        }
        int idx = hash(key);
        for (int n = 0; n < capacity; n++) {
            totalProbes++;
            Entry<V> entry = table[idx];
            if (entry == null) {
                return Optional.empty();
            }
            if (!entry.tombstone && entry.key.equals(key)) {
                return Optional.ofNullable(entry.value);
            }
            idx = (idx + 1) % capacity;
        }
        return Optional.empty();
    }

    /**
     * Marks the key's slot as a tombstone so later probes still terminate correctly.
     * Average O(1), worst O(n).
     *
     * @return {@code true} when a live entry was removed
     */
    public boolean remove(String key) {
        if (key == null) {
            return false;
        }
        int idx = hash(key);
        for (int n = 0; n < capacity; n++) {
            totalProbes++;
            Entry<V> entry = table[idx];
            if (entry == null) {
                return false;
            }
            if (!entry.tombstone && entry.key.equals(key)) {
                entry.tombstone = true;
                entry.value = null;
                size--;
                return true;
            }
            idx = (idx + 1) % capacity;
        }
        return false;
    }

    /**
     * Live entries, excluding tombstones. Time O(1), space O(1).
     */
    public int size() {
        return size;
    }

    /**
     * Current slot count. Time O(1), space O(1).
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Live size divided by capacity. Time O(1), space O(1).
     * A value of 0.7 or more triggers {@link #resize()} on the next insert.
     */
    public double loadFactor() {
        return (double) size / capacity;
    }

    /**
     * Cumulative slots examined by {@code put}, {@code get}, and {@code remove}
     * since construction, including rehash probes. Time O(1), space O(1).
     */
    public long getTotalProbes() {
        return totalProbes;
    }

    /**
     * Polynomial rolling hash reduced into the current capacity.
     * Time O(key length), space O(1).
     */
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

    /**
     * Time O(n) to re-insert every live entry. Space O(new capacity).
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<V>[] old = table;
        int grown = nextPrime(capacity * 2);
        if (grown <= capacity) {
            grown = nextPrime(capacity + 2);
        }
        capacity = grown;
        table = new Entry[capacity];
        size = 0;
        for (Entry<V> entry : old) {
            if (entry != null && !entry.tombstone) {
                putDuringResize(entry.key, entry.value);
            }
        }
    }

    private void putDuringResize(String key, V value) {
        int idx = hash(key);
        while (table[idx] != null) {
            totalProbes++;
            idx = (idx + 1) % capacity;
        }
        totalProbes++;
        table[idx] = new Entry<>(key, value);
        size++;
    }

    /**
     * Smallest prime greater than or equal to {@code n}. Time O(n sqrt n) worst, space O(1).
     */
    private static int nextPrime(int n) {
        if (n <= 2) {
            return 2;
        }
        int candidate = (n % 2 == 0) ? n + 1 : n;
        while (!isPrime(candidate)) {
            candidate += 2;
        }
        return candidate;
    }

    /**
     * Trial division. Time O(sqrt n), space O(1).
     */
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
