package com.mandiconnect.ui;

import com.mandiconnect.model.User;
import com.mandiconnect.service.AuctionService;
import com.mandiconnect.service.BidService;
import com.mandiconnect.service.LotService;
import com.mandiconnect.service.UserService;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Admin dashboard: manage user accounts, delete lots, view system stats,
 * and open the Algorithm Console.
 */
public class AdminFrame extends JFrame {

    private final UserService userService = new UserService();
    private final LotService lotService = new LotService();
    private final BidService bidService = new BidService();
    private final AuctionService auctionService = new AuctionService(lotService, bidService);
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel stats = new JLabel(" ");

    public AdminFrame(User user) {
        super("MandiConnect — Admin — " + user.getName());
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // User list table
        model = UiText.readOnlyModel("ID", "Name", "Email", "Phone", "Location", "Role", "Active");
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Top toolbar buttons
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(UiText.button("Refresh", e -> reload()));
        bar.add(UiText.button("Toggle Active", e -> toggleActive()));
        bar.add(UiText.button("Delete Lot", e -> deleteLot()));
        bar.add(UiText.button("Algorithm Console", e -> new AlgorithmConsoleFrame(lotService, bidService).setVisible(true)));
        bar.add(UiText.button("Logout", e -> logout()));

        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(stats, BorderLayout.SOUTH);

        // Load ADS structures from MySQL before showing data
        try {
            lotService.warmUp();
            bidService.warmUp();
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
        reload();
    }

    /** Reloads user rows and updates bottom summary counts. */
    private void reload() {
        model.setRowCount(0);
        try {
            for (User u : userService.findAll()) {
                model.addRow(new Object[]{
                        u.getId(), u.getName(), u.getEmail(), u.getPhone(),
                        u.getLocation(), u.getRole(), u.isActive() ? "yes" : "no"
                });
            }
            stats.setText(String.format("   Users: %d    Lots: %d    Active auctions: %d",
                    userService.findAll().size(), lotService.all().size(), auctionService.activeCount()));
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Enables or disables the selected user account. */
    private void toggleActive() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        long id = ((Number) model.getValueAt(row, 0)).longValue();
        try {
            userService.toggleActive(id);
            reload();
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Prompts for a lot ID, deletes it from MySQL, and updates ADS indexes. */
    private void deleteLot() {
        String idText = JOptionPane.showInputDialog(this, "Lot id to delete");
        if (idText == null || idText.isBlank()) {
            return;
        }
        try {
            lotService.delete(Long.parseLong(idText.trim()));
            bidService.warmUp();
            reload();
            JOptionPane.showMessageDialog(this, "Lot deleted.");
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
