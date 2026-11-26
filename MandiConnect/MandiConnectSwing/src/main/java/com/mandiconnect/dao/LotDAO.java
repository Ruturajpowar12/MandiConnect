package com.mandiconnect.dao;

import com.mandiconnect.db.DBConnection;
import com.mandiconnect.model.Lot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for reading and writing the 'lots' table in MySQL.
 */
public class LotDAO {

    /** Inserts a new crop lot into MySQL and saves the generated ID. */
    public long insert(Lot lot) throws SQLException {
        String sql = "INSERT INTO lots (lot_code, crop_name, quantity, unit, quality_grade, location, "
                + "expected_price, description, farmer_id, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, lot.getLotCode());
            ps.setString(2, lot.getCropName());
            ps.setDouble(3, lot.getQuantity());
            ps.setString(4, lot.getUnit());
            ps.setString(5, lot.getQualityGrade());
            ps.setString(6, lot.getLocation());
            ps.setDouble(7, lot.getExpectedPrice());
            ps.setString(8, lot.getDescription());
            ps.setLong(9, lot.getFarmerId());
            ps.setString(10, lot.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    lot.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Lot insert did not return a key");
    }

    /** Returns every lot in the database. */
    public List<Lot> findAll() throws SQLException {
        return query("SELECT * FROM lots ORDER BY id");
    }

    /** Returns all lots created by a specific farmer. */
    public List<Lot> findByFarmer(long farmerId) throws SQLException {
        String sql = "SELECT * FROM lots WHERE farmer_id = ? ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, farmerId);
            return readAll(ps);
        }
    }

    /** Returns lots matching a specific status. */
    public List<Lot> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM lots WHERE status = ? ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            return readAll(ps);
        }
    }

    /** Returns lots that traders can bid on (AVAILABLE or IN_AUCTION). */
    public List<Lot> findAvailableForTrade() throws SQLException {
        return query("SELECT * FROM lots WHERE status IN ('AVAILABLE', 'IN_AUCTION') ORDER BY id");
    }

    /** Finds a single lot by its primary key ID. */
    public Lot findById(long id) throws SQLException {
        String sql = "SELECT * FROM lots WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    /** Updates a lot's status (AVAILABLE, IN_AUCTION, or SOLD). */
    public void updateStatus(long id, String status) throws SQLException {
        String sql = "UPDATE lots SET status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    /** Deletes a lot by ID (also removes its bids/auction via ON DELETE CASCADE). */
    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM lots WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private List<Lot> query(String sql) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return readAll(ps);
        }
    }

    private List<Lot> readAll(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<Lot> lots = new ArrayList<>();
            while (rs.next()) {
                lots.add(map(rs));
            }
            return lots;
        }
    }

    /** Converts a database ResultSet row into a Lot object. */
    private Lot map(ResultSet rs) throws SQLException {
        Lot lot = new Lot();
        lot.setId(rs.getLong("id"));
        lot.setLotCode(rs.getString("lot_code"));
        lot.setCropName(rs.getString("crop_name"));
        lot.setQuantity(rs.getDouble("quantity"));
        lot.setUnit(rs.getString("unit"));
        lot.setQualityGrade(rs.getString("quality_grade"));
        lot.setLocation(rs.getString("location"));
        lot.setExpectedPrice(rs.getDouble("expected_price"));
        lot.setDescription(rs.getString("description"));
        lot.setFarmerId(rs.getLong("farmer_id"));
        lot.setStatus(rs.getString("status"));
        return lot;
    }
}
