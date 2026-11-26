package com.mandiconnect.model;

/**
 * Model class representing a bid placed by a trader on a lot.
 * Status can be PENDING, WINNING, ACCEPTED, or REJECTED.
 */
public class Bid {
    private long id;        // Primary key in bids table
    private long lotId;     // Lot on which bid was placed
    private long traderId;  // Trader user ID who placed the bid
    private double amount;  // Bid price in rupees
    private String status;  // Current bid status

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLotId() {
        return lotId;
    }

    public void setLotId(long lotId) {
        this.lotId = lotId;
    }

    public long getTraderId() {
        return traderId;
    }

    public void setTraderId(long traderId) {
        this.traderId = traderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Bid{id=" + id + ", lotId=" + lotId + ", traderId=" + traderId
                + ", amount=" + amount + ", status='" + status + "'}";
    }
}
