package com.mandiconnect.service;

import com.mandiconnect.dao.AuctionDAO;
import com.mandiconnect.model.Auction;
import com.mandiconnect.model.Bid;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Handles starting, closing, and accepting/rejecting bids for lot auctions.
 * Uses BidService.highestBid() (MaxHeap root) to pick the auction winner.
 */
public class AuctionService {

    private final AuctionDAO dao = new AuctionDAO();
    private final LotService lots;
    private final BidService bids;

    public AuctionService(LotService lots, BidService bids) {
        this.lots = lots;
        this.bids = bids;
    }

    /** Starts an auction for the lot and sets lot status to IN_AUCTION. */
    public Auction start(long lotId, int minutes) throws SQLException {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Duration must be at least 1 minute.");
        }
        if (dao.findByLot(lotId) != null) {
            throw new IllegalStateException("An auction already exists for this lot.");
        }
        Auction auction = new Auction();
        auction.setLotId(lotId);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusMinutes(minutes));
        auction.setStatus("ACTIVE");
        dao.insert(auction);
        lots.updateStatus(lotId, "IN_AUCTION");
        return auction;
    }

    /** Closes an auction and picks the highest bid from MaxHeap as the winner. */
    public void close(long lotId) throws SQLException {
        Auction auction = dao.findByLot(lotId);
        if (auction == null) {
            throw new IllegalStateException("This lot has no auction.");
        }
        if ("CLOSED".equals(auction.getStatus())) {
            throw new IllegalStateException("This auction is already closed.");
        }
        // Get highest offer in O(1) from MaxHeap instead of scanning DB
        Bid winner = bids.highestBid(lotId);
        Long winnerId = null;
        if (winner != null) {
            bids.updateStatus(winner.getId(), "WINNING");
            lots.updateStatus(lotId, "SOLD");
            winnerId = winner.getId();
        } else {
            // No bids placed -> lot goes back to AVAILABLE
            lots.updateStatus(lotId, "AVAILABLE");
        }
        dao.close(lotId, winnerId);
    }

    /** Lets the farmer accept one bid, reject other pending bids, and mark lot SOLD. */
    public void acceptBid(long lotId, long selectedBidId) throws SQLException {
        bids.updateStatus(selectedBidId, "ACCEPTED");
        for (Bid b : bids.history(lotId)) {
            if (b.getId() != selectedBidId && "PENDING".equalsIgnoreCase(b.getStatus())) {
                bids.updateStatus(b.getId(), "REJECTED");
            }
        }
        lots.updateStatus(lotId, "SOLD");
        Auction auction = dao.findByLot(lotId);
        if (auction != null && !"CLOSED".equals(auction.getStatus())) {
            dao.close(lotId, selectedBidId);
        }
    }

    /** Lets the farmer reject a specific bid. */
    public void rejectBid(long bidId) throws SQLException {
        bids.updateStatus(bidId, "REJECTED");
    }

    public Auction findByLot(long lotId) throws SQLException {
        return dao.findByLot(lotId);
    }

    /** Returns count of currently active auctions (shown in Admin dashboard). */
    public int activeCount() throws SQLException {
        return dao.findByStatus("ACTIVE").size();
    }
}
