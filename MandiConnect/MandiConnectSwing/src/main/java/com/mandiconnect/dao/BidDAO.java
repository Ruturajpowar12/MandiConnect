package com.mandiconnect.dao;

import com.mandiconnect.db.DBConnection;
import com.mandiconnect.model.Bid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Plain JDBC access to the {@code bids} table.
 */
public class BidDAO {

    public long insert(Bid bid) throws SQLException {
        String sql = "INSERT INTO bids (lot_id, trader_id, amount, status) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, bid.getLotId());
            ps.setLong(2, bid.getTraderId());
            ps.setDouble(3, bid.getAmount());
            ps.setString(4, bid.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    bid.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Bid insert did not return a key");
    }

    public List<Bid> findAll() throws SQLException {
        return query("SELECT * FROM bids ORDER BY id");
    }

    public List<Bid> findByLot(long lotId) throws SQLException {
        String sql = "SELECT * FROM bids WHERE lot_id = ? ORDER BY amount DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, lotId);
            return readAll(ps);
        }
    }

    public List<Bid> findByTrader(long traderId) throws SQLException {
        String sql = "SELECT * FROM bids WHERE trader_id = ? ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, traderId);
            return readAll(ps);
        }
    }

    public void updateStatus(long id, String status) throws SQLException {
        String sql = "UPDATE bids SET status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private List<Bid> query(String sql) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return readAll(ps);
        }
    }

    private List<Bid> readAll(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<Bid> bids = new ArrayList<>();
            while (rs.next()) {
                bids.add(map(rs));
            }
            return bids;
        }
    }

    private Bid map(ResultSet rs) throws SQLException {
        Bid bid = new Bid();
        bid.setId(rs.getLong("id"));
        bid.setLotId(rs.getLong("lot_id"));
        bid.setTraderId(rs.getLong("trader_id"));
        bid.setAmount(rs.getDouble("amount"));
        bid.setStatus(rs.getString("status"));
        return bid;
    }
}
