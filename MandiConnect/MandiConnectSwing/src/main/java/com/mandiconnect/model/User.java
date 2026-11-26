package com.mandiconnect.model;

/**
 * Model class representing a user account.
 * Role can be FARMER, TRADER, or ADMIN.
 */
public class User {
    private long id;         // Primary key in users table
    private String name;     // Full name
    private String email;    // Unique login email
    private String password; // Salted SHA-256 password hash
    private String phone;    // Contact number
    private String location; // City/village
    private String role;     // FARMER, TRADER, or ADMIN
    private boolean active;  // Whether the account is enabled

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email
                + "', role='" + role + "', active=" + active + "}";
    }
}
