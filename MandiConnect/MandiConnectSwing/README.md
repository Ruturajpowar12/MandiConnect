# MandiConnect — Smart Agricultural Auction & Bidding System

Advanced Data Structures project. The graded core is six hand-written structures. Swing and MySQL demonstrate them.

| Algorithm | File | Application | Complexity |
| --- | --- | --- | --- |
| Hash table, linear probing, tombstones | `src/main/java/com/mandiconnect/dsa/HashTable.java` | Lot lookup by `LOT-` code in `LotService.findByCode` | O(1) average, O(n) worst |
| Binary search tree (duplicates to the right) | `src/main/java/com/mandiconnect/dsa/BinarySearchTree.java` | Expected-price window in `LotService.priceRange` | O(h + k) range, O(n) worst height |
| QuickSort, Lomuto, median-of-three | `src/main/java/com/mandiconnect/dsa/QuickSort.java` | Sort lots by price and bids by amount | O(n log n) average, O(n²) worst |
| Binary max-heap | `src/main/java/com/mandiconnect/dsa/MaxHeap.java` | Highest bid per lot in `BidService.highestBid` | O(log n) insert and extract, O(1) peek |
| FIFO queue | `src/main/java/com/mandiconnect/dsa/BidQueue.java` | Bid intake order in `BidService.placeBid` | O(1) enqueue and dequeue |
| Singly linked list, head and tail | `src/main/java/com/mandiconnect/dsa/BidLinkedList.java` | Bid history chain in `BidService.history` | O(1) append, O(n) walk and reverse |

The **Algorithm Console** (toolbar button on every dashboard) is the primary ADS demonstration page. It prints probe counts, the BST ASCII tree, QuickSort comparison counts, the heap array, the queue snapshot, and the linked-list node chain. The Benchmarks tab times each structure against the JDK equivalent.

## Prerequisites

- JDK 22
- Maven 3.9+
- XAMPP with MySQL running (root, empty password)

## Setup

1. Start Apache and MySQL from the XAMPP control panel.
2. Import `database/mandiconnect_schema.sql` in phpMyAdmin (or `mysql -u root < database/mandiconnect_schema.sql`). The script creates the `mandiconnect` database. It does not insert the admin user.
3. Run the DSA tests:

```
mvn clean test
```

4. Start the desktop app (the Swing equivalent of a server run):

```
mvn exec:java -Dexec.mainClass=com.mandiconnect.Main
```

Or package a runnable JAR and launch it:

```
mvn clean package
java -jar target/mandiconnect-swing-1.0.0.jar
```

On the first successful database connection the console prints:

```
>> Default admin created: admin@mandiconnect.com / admin123
```

## Default credentials

`admin@mandiconnect.com` / `admin123`

## Test flow

1. Register a Farmer.
2. Log in as that farmer and add a lot. The table shows a code of the form `LOT-XXXXXXXX`.
3. Register a Trader, log in, and confirm the lot is listed.
4. Place two bids, ₹2600 and ₹2700.
5. Log in as the farmer, select the lot, and choose View Bids. The heap peek is ₹2700 and the history lists both bids.
6. Start Auction. The lot status becomes `IN_AUCTION`.
7. Close Auction. With bids present the status becomes `SOLD`.
8. Log in as admin. The bottom label shows user, lot, and active-auction counts.
9. Open Algorithm Console from any dashboard and refresh every tab:
   - Hash Table shows a non-zero probe total after the sample lookups.
   - BST (Price) shows an ASCII tree and a height above 0.
   - QuickSort shows a comparison count near `n·log₂n`.
   - MaxHeap shows the array and the peek maximum for the bid lot.
   - Bid Queue shows the pending snapshot (empty once `placeBid` has drained it) and the FIFO note.
   - Bid LinkedList shows `[₹2600] → [₹2700] → null` after those two bids.
   - Benchmarks finishes in under a minute and every row is Correct ✔.

Design notes for the six structures are in `ALGORITHM_REPORT.md`.
