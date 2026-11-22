package com.mandiconnect;

import com.mandiconnect.service.UserService;
import com.mandiconnect.ui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Starts MandiConnect on the Swing event thread after seeding the admin account.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new UserService().ensureAdmin();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Database error: " + e.getMessage() +
                                "\n\nMake sure XAMPP MySQL is running and mandiconnect DB exists " +
                                "(run database/mandiconnect_schema.sql).",
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new LoginFrame().setVisible(true);
        });
    }
}
