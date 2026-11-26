package com.mandiconnect.model;

import java.time.LocalDateTime;

/**
 * Model class representing an auction for a crop lot.
 * Status can be ACTIVE or CLOSED.
 */
public class Auction {
    private long id;                 // Primary key in auctions table
    private long lotId;              // ID of the crop lot being auctioned
    private LocalDateTime startTime; // When auction started
    private LocalDateTime endTime;   // Scheduled auction end time
    private String status;           // ACTIVE or CLOSED
    private Long winnerBidId;        // Winning bid ID (null until closed with a bid)

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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getWinnerBidId() {
        return winnerBidId;
    }

    public void setWinnerBidId(Long winnerBidId) {
        this.winnerBidId = winnerBidId;
    }
}
