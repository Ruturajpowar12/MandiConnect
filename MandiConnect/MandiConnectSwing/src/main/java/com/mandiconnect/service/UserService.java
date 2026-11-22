package com.mandiconnect.service;

import com.mandiconnect.dao.UserDAO;
import com.mandiconnect.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

/**
 * Registration, login, and the one-time admin seed.
 * Passwords are stored as {@code base64(salt):base64(SHA-256(salt || password))}.
 */
public class UserService {

    public static final String ADMIN_EMAIL = "admin@mandiconnect.com";
    public static final String ADMIN_PASSWORD = "admin123";

    private final UserDAO dao = new UserDAO();
    private final SecureRandom random = new SecureRandom();

    /**
     * 16-byte random salt, then SHA-256. The stored form is {@code base64(salt):base64(hash)}.
     */
    public String hash(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        byte[] digest = sha256(salt, password);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(digest);
    }

    /**
     * Checks {@code password} against a value produced by {@link #hash(String)}.
     */
    public boolean verify(String password, String stored) {
        if (password == null || stored == null || !stored.contains(":")) {
            return false;
        }
        String[] parts = stored.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        byte[] actual = sha256(salt, password);
        return MessageDigest.isEqual(expected, actual);
    }

    public User register(String name, String email, String password, String phone, String location, String role)
            throws SQLException {
        String cleanEmail = email == null ? "" : email.trim();
        if (name == null || name.isBlank() || cleanEmail.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Name, email, and password are required.");
        }
        if (!"FARMER".equals(role) && !"TRADER".equals(role)) {
            throw new IllegalArgumentException("Role must be FARMER or TRADER.");
        }
        if (dao.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email already registered.");
        }
        User user = new User();
        user.setName(name.trim());
        user.setEmail(cleanEmail);
        user.setPassword(hash(password));
        user.setPhone(phone == null ? "" : phone.trim());
        user.setLocation(location == null ? "" : location.trim());
        user.setRole(role);
        user.setActive(true);
        dao.insert(user);
        return user;
    }

    /**
     * @return the user when the password matches and the account is active; otherwise null
     */
    public User authenticate(String email, String password) throws SQLException {
        if (email == null || password == null) {
            return null;
        }
        User user = dao.findByEmail(email.trim());
        if (user == null || !user.isActive()) {
            return null;
        }
        if (!verify(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    public List<User> findAll() throws SQLException {
        return dao.findAll();
    }

    public void toggleActive(long id) throws SQLException {
        dao.toggleActive(id);
    }

    /**
     * Inserts {@code admin@mandiconnect.com} / {@code admin123} when that email is missing.
     */
    public void ensureAdmin() throws SQLException {
        if (dao.existsByEmail(ADMIN_EMAIL)) {
            return;
        }
        User admin = new User();
        admin.setName("MandiConnect Admin");
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(hash(ADMIN_PASSWORD));
        admin.setPhone("");
        admin.setLocation("Mandi");
        admin.setRole("ADMIN");
        admin.setActive(true);
        dao.insert(admin);
        System.out.println(">> Default admin created: admin@mandiconnect.com / admin123");
    }

    private static byte[] sha256(byte[] salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
