package com.mandiconnect;

import com.mandiconnect.service.UserService;
import com.mandiconnect.ui.LoginFrame;
import com.mandiconnect.ui.UiText;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Entry point of the MandiConnect desktop application.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Apply global colors and fonts before creating any window
        UiText.initTheme();

        // Start Swing UI safely on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Create default admin user in MySQL if it does not exist yet
                new UserService().ensureAdmin();
            } catch (Exception e) {
                // Show error popup if MySQL/XAMPP is not running or DB is missing
                JOptionPane.showMessageDialog(null,
                        "Database error: " + e.getMessage() +
                                "\n\nMake sure XAMPP MySQL is running and mandiconnect DB exists " +
                                "(run database/mandiconnect_schema.sql).",
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            // Open the Login window
            new LoginFrame().setVisible(true);
        });
    }
}
