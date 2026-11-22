package com.mandiconnect.service;

import com.mandiconnect.dao.AuctionDAO;
import com.mandiconnect.model.Auction;
import com.mandiconnect.model.Bid;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Opens and closes an auction for a lot. The winner is the max-heap root
 * from {@link BidService#highestBid(long)}, not a SQL {@code MAX(amount)}.
 */
public class AuctionService {

    private final AuctionDAO dao = new AuctionDAO();
    private final LotService lots;
    private final BidService bids;

    public AuctionService(LotService lots, BidService bids) {
        this.lots = lots;
        this.bids = bids;
    }

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

    public void close(long lotId) throws SQLException {
        Auction auction = dao.findByLot(lotId);
        if (auction == null) {
            throw new IllegalStateException("This lot has no auction.");
        }
        if ("CLOSED".equals(auction.getStatus())) {
            throw new IllegalStateException("This auction is already closed.");
        }
        Bid winner = bids.highestBid(lotId);
        Long winnerId = null;
        if (winner != null) {
            bids.updateStatus(winner.getId(), "WINNING");
            lots.updateStatus(lotId, "SOLD");
            winnerId = winner.getId();
        } else {
            lots.updateStatus(lotId, "AVAILABLE");
        }
        dao.close(lotId, winnerId);
    }

    /**
     * Lets the farmer explicitly accept a chosen bid.
     * Marks the chosen bid as ACCEPTED, marks other pending bids on this lot as REJECTED,
     * updates the lot status to SOLD, and closes the auction.
     */
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

    /**
     * Lets the farmer explicitly reject a specific bid.
     */
    public void rejectBid(long bidId) throws SQLException {
        bids.updateStatus(bidId, "REJECTED");
    }

    public Auction findByLot(long lotId) throws SQLException {
        return dao.findByLot(lotId);
    }

    public int activeCount() throws SQLException {
        return dao.findByStatus("ACTIVE").size();
    }
}
