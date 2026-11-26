package com.mandiconnect.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides JDBC database connections to the local MySQL (XAMPP) database.
 */
public final class DBConnection {

    // Database connection URL pointing to local 'mandiconnect' database
    public static final String URL =
            "jdbc:mysql://localhost:3306/mandiconnect?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    // Default XAMPP MySQL credentials
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DBConnection() {
    }

    /** Opens and returns a new MySQL connection (caller closes it with try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
