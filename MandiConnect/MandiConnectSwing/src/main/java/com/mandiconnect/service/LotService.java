package com.mandiconnect.service;

import com.mandiconnect.dao.LotDAO;
import com.mandiconnect.dsa.BinarySearchTree;
import com.mandiconnect.dsa.HashTable;
import com.mandiconnect.dsa.QuickSort;
import com.mandiconnect.model.Lot;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages crop lots in MySQL and indexes them in:
 * - HashTable: fast lookup by LOT-code
 * - BinarySearchTree: price-range search
 * - QuickSort: sorting available lots by expected price
 */
public class LotService {

    private final LotDAO dao = new LotDAO();
    private HashTable<Lot> lotHash = new HashTable<>(101);
    private BinarySearchTree<Lot> lotBST = new BinarySearchTree<>();

    /** Reloads all lots from MySQL into the HashTable and BST. */
    public void warmUp() throws SQLException {
        lotHash = new HashTable<>(101);
        lotBST = new BinarySearchTree<>();
        for (Lot lot : dao.findAll()) {
            index(lot);
        }
    }

    /** Creates a new crop lot, saves it in MySQL, and indexes it in HashTable + BST. */
    public Lot create(long farmerId, String cropName, double quantity, String unit, String grade,
                      String location, double expectedPrice, String description) throws SQLException {
        if (cropName == null || cropName.isBlank()) {
            throw new IllegalArgumentException("Crop name is required.");
        }
        if (quantity <= 0 || expectedPrice <= 0) {
            throw new IllegalArgumentException("Quantity and expected price must be positive.");
        }

        // Generate an 8-character unique code like LOT-A1B2C3D4
        String code = "LOT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        Lot lot = new Lot();
        lot.setLotCode(code);
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

    /** Looks up a lot by code in O(1) average time using HashTable. */
    public Lot findByCode(String code) {
        if (code == null) {
            return null;
        }
        return lotHash.get(code.trim()).orElse(null);
    }

    /** Returns available lots sorted ascending by expected price using QuickSort. */
    public List<Lot> availableSortedByPrice() throws SQLException {
        List<Lot> lots = new ArrayList<>(available());
        QuickSort.sort(lots, (a, b) -> Double.compare(a.getExpectedPrice(), b.getExpectedPrice()));
        return lots;
    }

    /** Returns non-sold lots whose expected price lies in [min, max] using BST pruning. */
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

    /** Updates lot status in MySQL and inside the in-memory BST. */
    public void updateStatus(long id, String status) throws SQLException {
        dao.updateStatus(id, status);
        for (Lot lot : lotBST.inOrder()) {
            if (lot.getId() == id) {
                lot.setStatus(status);
            }
        }
    }

    /** Deletes a lot from MySQL, removes it from HashTable, and refreshes BST. */
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

    /** Adds a lot into both the HashTable (by code) and BST (by expected price). */
    private void index(Lot lot) {
        lotHash.put(lot.getLotCode(), lot);
        lotBST.insert(lot.getExpectedPrice(), lot);
    }
}
