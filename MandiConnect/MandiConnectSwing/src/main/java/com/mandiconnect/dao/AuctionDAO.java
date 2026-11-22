package com.mandiconnect.dao;

import com.mandiconnect.db.DBConnection;
import com.mandiconnect.model.Auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Plain JDBC access to the {@code auctions} table.
 */
public class AuctionDAO {

    public long insert(Auction auction) throws SQLException {
        String sql = "INSERT INTO auctions (lot_id, start_time, end_time, status, winner_bid_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, auction.getLotId());
            ps.setTimestamp(2, Timestamp.valueOf(auction.getStartTime()));
            ps.setTimestamp(3, Timestamp.valueOf(auction.getEndTime()));
            ps.setString(4, auction.getStatus());
            if (auction.getWinnerBidId() == null) {
                ps.setNull(5, java.sql.Types.BIGINT);
            } else {
                ps.setLong(5, auction.getWinnerBidId());
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    auction.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Auction insert did not return a key");
    }

    public Auction findByLot(long lotId) throws SQLException {
        String sql = "SELECT * FROM auctions WHERE lot_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, lotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public List<Auction> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM auctions WHERE status = ? ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                List<Auction> auctions = new ArrayList<>();
                while (rs.next()) {
                    auctions.add(map(rs));
                }
                return auctions;
            }
        }
    }

    public void close(long lotId, Long winnerBidId) throws SQLException {
        String sql = "UPDATE auctions SET status = 'CLOSED', winner_bid_id = ? WHERE lot_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (winnerBidId == null) {
                ps.setNull(1, java.sql.Types.BIGINT);
            } else {
                ps.setLong(1, winnerBidId);
            }
            ps.setLong(2, lotId);
            ps.executeUpdate();
        }
    }

    private Auction map(ResultSet rs) throws SQLException {
        Auction auction = new Auction();
        auction.setId(rs.getLong("id"));
        auction.setLotId(rs.getLong("lot_id"));
        Timestamp start = rs.getTimestamp("start_time");
        Timestamp end = rs.getTimestamp("end_time");
        if (start != null) {
            auction.setStartTime(start.toLocalDateTime());
        }
        if (end != null) {
            auction.setEndTime(end.toLocalDateTime());
        }
        auction.setStatus(rs.getString("status"));
        long winner = rs.getLong("winner_bid_id");
        auction.setWinnerBidId(rs.wasNull() ? null : winner);
        return auction;
    }
}
