package com.mandiconnect.service;

import com.mandiconnect.dao.BidDAO;
import com.mandiconnect.dsa.BidLinkedList;
import com.mandiconnect.dsa.BidQueue;
import com.mandiconnect.dsa.MaxHeap;
import com.mandiconnect.dsa.QuickSort;
import com.mandiconnect.model.Bid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bid persistence plus the FIFO queue (intake order), a max-heap per lot
 * (highest offer), and a linked list per lot (history in arrival order).
 */
public class BidService {

    private final BidDAO dao = new BidDAO();
    private final BidQueue<Bid> pending = new BidQueue<>();
    private final Map<Long, MaxHeap<Bid>> heapByLot = new HashMap<>();
    private final Map<Long, BidLinkedList<Bid>> historyByLot = new HashMap<>();
    private final BidLinkedList<Bid> processedFifo = new BidLinkedList<>();

    public void warmUp() throws SQLException {
        heapByLot.clear();
        historyByLot.clear();
        for (Bid bid : dao.findAll()) {
            index(bid);
        }
    }

    /**
     * Enqueues the bid, immediately dequeues it (FIFO), stores it, then indexes
     * it in the lot heap and the lot history list.
     */
    public Bid placeBid(long lotId, long traderId, double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bid amount must be positive.");
        }
        Bid bid = new Bid();
        bid.setLotId(lotId);
        bid.setTraderId(traderId);
        bid.setAmount(amount);
        bid.setStatus("PENDING");
        pending.enqueue(bid);
        Bid next = pending.dequeue();
        if (next == null) {
            throw new IllegalStateException("Bid queue lost the bid it just accepted.");
        }
        dao.insert(next);
        index(next);
        processedFifo.add(next);
        return next;
    }

    /** Root of the lot's max-heap, or null when the lot has no indexed bids. */
    public Bid highestBid(long lotId) {
        MaxHeap<Bid> heap = heapByLot.get(lotId);
        if (heap == null || heap.isEmpty()) {
            return null;
        }
        return heap.peekMax().data;
    }

    /**
     * History newest-first from the linked list. Falls back to the database
     * when this service has not indexed the lot.
     */
    public List<Bid> history(long lotId) throws SQLException {
        BidLinkedList<Bid> list = historyByLot.get(lotId);
        if (list == null || list.size() == 0) {
            return dao.findByLot(lotId);
        }
        return list.reversed();
    }

    /** Bids on a lot, highest amount first, ordered by {@link QuickSort}. */
    public List<Bid> sortedByAmountDesc(long lotId) throws SQLException {
        BidLinkedList<Bid> list = historyByLot.get(lotId);
        List<Bid> bids = list == null ? dao.findByLot(lotId) : list.toList();
        List<Bid> copy = new ArrayList<>(bids);
        QuickSort.sort(copy, Comparator.comparingDouble(Bid::getAmount).reversed());
        return copy;
    }

    public List<Bid> byTrader(long traderId) throws SQLException {
        return dao.findByTrader(traderId);
    }

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

    /** Bids this session has already dequeued, in FIFO order. */
    public BidLinkedList<Bid> getProcessedFifo() {
        return processedFifo;
    }

    private void index(Bid bid) {
        heapByLot.computeIfAbsent(bid.getLotId(), id -> new MaxHeap<>()).insert(bid.getAmount(), bid);
        historyByLot.computeIfAbsent(bid.getLotId(), id -> new BidLinkedList<>()).add(bid);
    }
}
