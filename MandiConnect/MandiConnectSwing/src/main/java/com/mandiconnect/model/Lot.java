package com.mandiconnect.model;

/**
 * Model class representing a crop lot listed by a farmer.
 * Status can be AVAILABLE, IN_AUCTION, or SOLD.
 */
public class Lot {
    private long id;              // Primary key in lots table
    private String lotCode;       // Unique short code like LOT-A1B2C3D4 (HashTable key)
    private String cropName;      // Name of the crop (e.g., Wheat, Mango)
    private double quantity;      // Amount of crop
    private String unit;          // Unit (kg, quintal, tonne)
    private String qualityGrade;  // Quality grade (A, B, C)
    private String location;      // Mandi/farm location
    private double expectedPrice; // Farmer's expected price (BST key)
    private String description;   // Optional notes
    private long farmerId;        // Owner farmer's user ID
    private String status;        // AVAILABLE, IN_AUCTION, or SOLD

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLotCode() {
        return lotCode;
    }

    public void setLotCode(String lotCode) {
        this.lotCode = lotCode;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getExpectedPrice() {
        return expectedPrice;
    }

    public void setExpectedPrice(double expectedPrice) {
        this.expectedPrice = expectedPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(long farmerId) {
        this.farmerId = farmerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return lotCode + " " + cropName + " @" + expectedPrice + " [" + status + "]";
    }
}
