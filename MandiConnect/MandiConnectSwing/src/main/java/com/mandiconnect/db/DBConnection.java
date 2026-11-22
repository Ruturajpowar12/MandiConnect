package com.mandiconnect.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens JDBC connections to the local XAMPP MySQL server.
 * Username {@code root} and an empty password are the XAMPP defaults.
 */
public final class DBConnection {

    public static final String URL =
            "jdbc:mysql://localhost:3306/mandiconnect?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DBConnection() {
    }

    /**
     * @return a new connection; the caller must close it
     * @throws SQLException if MySQL is down or the schema has not been imported
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
