package com.mandiconnect.model;

/**
 * A trader's offer on a lot. {@code status} is {@code PENDING} or {@code WINNING}.
 */
public class Bid {
    private long id;
    private long lotId;
    private long traderId;
    private double amount;
    private String status;

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
