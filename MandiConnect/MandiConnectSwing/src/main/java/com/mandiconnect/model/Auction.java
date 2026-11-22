package com.mandiconnect.model;

import java.time.LocalDateTime;

/**
 * One auction per lot. {@code status} is {@code ACTIVE} or {@code CLOSED}.
 * {@code winnerBidId} is null until the auction closes with a winning bid.
 */
public class Auction {
    private long id;
    private long lotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Long winnerBidId;

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
