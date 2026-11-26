package com.mandiconnect.service;

import com.mandiconnect.dao.BidDAO;
import com.mandiconnect.dsa.BidLinkedList;
import com.mandiconnect.dsa.BidQueue;
import com.mandiconnect.dsa.MaxHeap;
import com.mandiconnect.dsa.QuickSort;
import com.mandiconnect.model.Bid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages bids in MySQL and in three ADS structures:
 * - BidQueue: processes incoming bids in FIFO order
 * - MaxHeap (one per lot): gives the highest bid in O(1) time
 * - BidLinkedList (one per lot): stores bid history in arrival order
 */
public class BidService {

    private final BidDAO dao = new BidDAO();
    private final BidQueue<Bid> pending = new BidQueue<>();
    private final Map<Long, MaxHeap<Bid>> heapByLot = new HashMap<>();
    private final Map<Long, BidLinkedList<Bid>> historyByLot = new HashMap<>();
    private final BidLinkedList<Bid> processedFifo = new BidLinkedList<>();

    /** Loads all bids from MySQL and builds each lot's MaxHeap and BidLinkedList. */
    public void warmUp() throws SQLException {
        heapByLot.clear();
        historyByLot.clear();
        for (Bid bid : dao.findAll()) {
            index(bid);
        }
    }

    /** Enqueues bid (FIFO), dequeues it, saves to MySQL, and indexes in heap + linked list. */
    public Bid placeBid(long lotId, long traderId, double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bid amount must be positive.");
        }
        Bid bid = new Bid();
        bid.setLotId(lotId);
        bid.setTraderId(traderId);
        bid.setAmount(amount);
        bid.setStatus("PENDING");

        // 1. Place in FIFO queue so arrival order is respected
        pending.enqueue(bid);
        Bid next = pending.dequeue();
        if (next == null) {
            throw new IllegalStateException("Bid queue lost the bid it just accepted.");
        }

        // 2. Save to MySQL and update MaxHeap + BidLinkedList
        dao.insert(next);
        index(next);
        processedFifo.add(next);
        return next;
    }

    /** Returns the highest bid for a lot using MaxHeap.peekMax() in O(1) time. */
    public Bid highestBid(long lotId) {
        MaxHeap<Bid> heap = heapByLot.get(lotId);
        if (heap == null || heap.isEmpty()) {
            return null;
        }
        return heap.peekMax().data;
    }

    /** Returns bid history newest-first using BidLinkedList.reversed(). */
    public List<Bid> history(long lotId) throws SQLException {
        BidLinkedList<Bid> list = historyByLot.get(lotId);
        if (list == null || list.size() == 0) {
            return dao.findByLot(lotId);
        }
        return list.reversed();
    }

    /** Returns bids sorted from highest to lowest amount using QuickSort. */
    public List<Bid> sortedByAmountDesc(long lotId) throws SQLException {
        BidLinkedList<Bid> list = historyByLot.get(lotId);
        List<Bid> bids = (list == null) ? dao.findByLot(lotId) : list.toList();
        List<Bid> copy = new ArrayList<>(bids);
        QuickSort.sort(copy, (a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        return copy;
    }

    public List<Bid> byTrader(long traderId) throws SQLException {
        return dao.findByTrader(traderId);
    }

    /** Updates bid status in MySQL and inside the in-memory linked list. */
    public void updateStatus(long bidId, String status) throws SQLException {
        dao.updateStatus(bidId, status);
        for (BidLinkedList<Bid> list : historyByLot.values()) {
            for (Bid bid : list.toList()) {
                if (bid.getId() == bidId) {
                    bid.setStatus(status);
                }
            }
        }
    }

    public BidQueue<Bid> getQueue() {
        return pending;
    }

    public MaxHeap<Bid> getHeap(long lotId) {
        return heapByLot.get(lotId);
    }

    public BidLinkedList<Bid> getList(long lotId) {
        return historyByLot.get(lotId);
    }

    public BidLinkedList<Bid> getProcessedFifo() {
        return processedFifo;
    }

    /** Adds a bid into the lot's MaxHeap and BidLinkedList. */
    private void index(Bid bid) {
        long lotId = bid.getLotId();

        MaxHeap<Bid> heap = heapByLot.get(lotId);
        if (heap == null) {
            heap = new MaxHeap<>();
            heapByLot.put(lotId, heap);
        }
        heap.insert(bid.getAmount(), bid);

        BidLinkedList<Bid> list = historyByLot.get(lotId);
        if (list == null) {
            list = new BidLinkedList<>();
            historyByLot.put(lotId, list);
        }
        list.add(bid);
    }
}
