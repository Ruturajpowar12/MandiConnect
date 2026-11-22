package com.mandiconnect.service;

import com.mandiconnect.dao.LotDAO;
import com.mandiconnect.dsa.BinarySearchTree;
import com.mandiconnect.dsa.HashTable;
import com.mandiconnect.dsa.QuickSort;
import com.mandiconnect.model.Lot;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Lot persistence plus the hash table (lookup by code), the price BST (range
 * queries), and QuickSort (price order for the trader table).
 */
public class LotService {

    private final LotDAO dao = new LotDAO();
    private HashTable<Lot> lotHash = new HashTable<>(101);
    private BinarySearchTree<Lot> lotBST = new BinarySearchTree<>();

    public void warmUp() throws SQLException {
        lotHash = new HashTable<>(101);
        lotBST = new BinarySearchTree<>();
        for (Lot lot : dao.findAll()) {
            index(lot);
        }
    }

    public Lot create(long farmerId, String cropName, double quantity, String unit, String grade,
                      String location, double expectedPrice, String description) throws SQLException {
        if (cropName == null || cropName.isBlank()) {
            throw new IllegalArgumentException("Crop name is required.");
        }
        if (quantity <= 0 || expectedPrice <= 0) {
            throw new IllegalArgumentException("Quantity and expected price must be positive.");
        }
        Lot lot = new Lot();
        lot.setLotCode("LOT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        lot.setCropName(cropName.trim());
        lot.setQuantity(quantity);
        lot.setUnit(unit == null || unit.isBlank() ? "kg" : unit.trim());
        lot.setQualityGrade(grade == null || grade.isBlank() ? "A" : grade.trim());
        lot.setLocation(location == null ? "" : location.trim());
        lot.setExpectedPrice(expectedPrice);
        lot.setDescription(description == null ? "" : description.trim());
        lot.setFarmerId(farmerId);
        lot.setStatus("AVAILABLE");
        dao.insert(lot);
        index(lot);
        return lot;
    }

    /** Hash-table lookup by lot code. Returns null when the code is not indexed. */
    public Lot findByCode(String code) {
        if (code == null) {
            return null;
        }
        return lotHash.get(code.trim()).orElse(null);
    }

    /** Available lots ordered by expected price using {@link QuickSort}. */
    public List<Lot> availableSortedByPrice() throws SQLException {
        List<Lot> lots = new ArrayList<>(available());
        QuickSort.sort(lots, Comparator.comparingDouble(Lot::getExpectedPrice));
        return lots;
    }

    /** Inclusive price window answered by the BST, with subtrees outside the window pruned. */
    public List<Lot> priceRange(double min, double max) {
        List<Lot> hits = lotBST.priceRange(min, max);
        List<Lot> active = new ArrayList<>();
        for (Lot lot : hits) {
            if (!"SOLD".equalsIgnoreCase(lot.getStatus())) {
                active.add(lot);
            }
        }
        return active;
    }

    public List<Lot> available() throws SQLException {
        return dao.findAvailableForTrade();
    }

    public List<Lot> byFarmer(long farmerId) throws SQLException {
        return dao.findByFarmer(farmerId);
    }

    public List<Lot> all() throws SQLException {
        return dao.findAll();
    }

    public Lot findById(long id) throws SQLException {
        return dao.findById(id);
    }

    public void updateStatus(long id, String status) throws SQLException {
        dao.updateStatus(id, status);
        for (Lot lot : lotBST.inOrder()) {
            if (lot.getId() == id) {
                lot.setStatus(status);
            }
        }
    }

    public void delete(long id) throws SQLException {
        Lot existing = dao.findById(id);
        dao.delete(id);
        if (existing != null) {
            lotHash.remove(existing.getLotCode());
        }
        warmUp();
    }

    public HashTable<Lot> getHash() {
        return lotHash;
    }

    public BinarySearchTree<Lot> getBST() {
        return lotBST;
    }

    private void index(Lot lot) {
        lotHash.put(lot.getLotCode(), lot);
        lotBST.insert(lot.getExpectedPrice(), lot);
    }
}
