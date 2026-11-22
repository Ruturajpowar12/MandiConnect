package com.mandiconnect.ui;

import com.mandiconnect.model.User;
import com.mandiconnect.service.AuctionService;
import com.mandiconnect.service.BidService;
import com.mandiconnect.service.LotService;
import com.mandiconnect.service.UserService;

import javax.swing.JButton;
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
 * Admin dashboard: enable or disable accounts, delete a lot, and open the algorithm console.
 */
public class AdminFrame extends JFrame {

    private final UserService userService = new UserService();
    private final LotService lotService = new LotService();
    private final BidService bidService = new BidService();
    private final AuctionService auctionService;
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel stats = new JLabel(" ");

    public AdminFrame(User user) {
        super("MandiConnect — Admin — " + user.getName());
        this.auctionService = new AuctionService(lotService, bidService);
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Location", "Role", "Active"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(button("Refresh", this::reload));
        bar.add(button("Toggle Active", this::toggleActive));
        bar.add(button("Delete Lot", this::deleteLot));
        bar.add(button("Algorithm Console", this::openConsole));
        bar.add(button("Logout", this::logout));

        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(stats, BorderLayout.SOUTH);

        try {
            lotService.warmUp();
            bidService.warmUp();
        } catch (Exception ex) {
            error(ex);
        }
        reload();
    }

    private void reload() {
        model.setRowCount(0);
        try {
            for (User user : userService.findAll()) {
                model.addRow(new Object[]{
                        user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                        user.getLocation(), user.getRole(), user.isActive() ? "yes" : "no"
                });
            }
            stats.setText(String.format("   Users: %d    Lots: %d    Active auctions: %d",
                    userService.findAll().size(), lotService.all().size(), auctionService.activeCount()));
        } catch (Exception ex) {
            error(ex);
        }
    }

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
            error(ex);
        }
    }

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
            error(ex);
        }
    }

    private void openConsole() {
        new AlgorithmConsoleFrame(lotService, bidService).setVisible(true);
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    private JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "MandiConnect", JOptionPane.ERROR_MESSAGE);
    }
}
