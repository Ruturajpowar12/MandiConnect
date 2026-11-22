package com.mandiconnect.ui;

import com.mandiconnect.model.Bid;
import com.mandiconnect.model.Lot;
import com.mandiconnect.model.User;
import com.mandiconnect.service.BidService;
import com.mandiconnect.service.LotService;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Trader dashboard. Sorting goes through QuickSort and price windows go through the BST.
 */
public class TraderFrame extends JFrame {

    private final User user;
    private final LotService lotService = new LotService();
    private final BidService bidService = new BidService();
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField cropSearch = new JTextField(12);

    public TraderFrame(User user) {
        super("MandiConnect — Trader — " + user.getName());
        this.user = user;
        setSize(1150, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new DefaultTableModel(
                new String[]{"ID", "Code", "Crop", "Qty", "Grade", "Location", "Price", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cropSearch.setToolTipText("Enter crop name (e.g. Wheat, Mango) or Lot Code");
        cropSearch.addActionListener(e -> searchCrop());
        bar.add(cropSearch);
        bar.add(button("Search Crop", this::searchCrop));
        bar.add(button("Sort by Price", this::sortByPrice));
        bar.add(button("Price Range (BST)", this::priceRange));
        bar.add(button("Show All", this::showAvailable));
        bar.add(button("Refresh", this::showAvailable));
        bar.add(button("Place Bid", this::placeBid));
        bar.add(button("My Bids", this::myBids));
        bar.add(button("Algorithm Console", this::openConsole));
        bar.add(button("Logout", this::logout));

        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new JLabel("  Available lots. Sort by Price runs QuickSort. Price Range runs the BST."), BorderLayout.SOUTH);

        try {
            lotService.warmUp();
            bidService.warmUp();
        } catch (Exception ex) {
            error(ex);
        }
        showAvailable();
    }

    private void searchCrop() {
        String query = cropSearch.getText().trim();
        if (query.isEmpty()) {
            showAvailable();
            return;
        }
        try {
            lotService.warmUp();
            // First check if query matches an exact Lot Code via the custom HashTable
            Lot byCode = lotService.findByCode(query.toUpperCase(Locale.ROOT));
            if (byCode != null && !"SOLD".equalsIgnoreCase(byCode.getStatus())) {
                show(List.of(byCode));
                return;
            }
            // Otherwise search by crop name or lot code substring
            String lower = query.toLowerCase(Locale.ROOT);
            List<Lot> hits = new ArrayList<>();
            for (Lot lot : lotService.available()) {
                if (lot.getCropName().toLowerCase(Locale.ROOT).contains(lower)
                        || lot.getLotCode().toLowerCase(Locale.ROOT).contains(lower)) {
                    hits.add(lot);
                }
            }
            if (hits.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No active lots found matching \"" + query + "\"", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
            show(hits);
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void sortByPrice() {
        try {
            lotService.warmUp();
            show(lotService.availableSortedByPrice());
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void priceRange() {
        JTextField min = new JTextField("2000");
        JTextField max = new JTextField("5000");
        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Min price (₹)"));
        form.add(min);
        form.add(new JLabel("Max price (₹)"));
        form.add(max);
        int choice = JOptionPane.showConfirmDialog(this, form, "Price Range (BST)", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            lotService.warmUp();
            double minVal = Double.parseDouble(min.getText().trim());
            double maxVal = Double.parseDouble(max.getText().trim());
            List<Lot> hits = lotService.priceRange(minVal, maxVal);
            if (hits.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No lots found in price range ₹" + UiText.money(minVal) + " to ₹" + UiText.money(maxVal), "Price Range (BST)", JOptionPane.INFORMATION_MESSAGE);
            }
            show(hits);
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void showAvailable() {
        try {
            lotService.warmUp();
            bidService.warmUp();
            show(lotService.available());
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void placeBid() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a lot first.");
            return;
        }
        long lotId = ((Number) model.getValueAt(row, 0)).longValue();
        String code = String.valueOf(model.getValueAt(row, 1));
        String amountText = JOptionPane.showInputDialog(this, "Bid amount for " + code + " (₹)", "2600");
        if (amountText == null) {
            return;
        }
        try {
            Bid bid = bidService.placeBid(lotId, user.getId(), Double.parseDouble(amountText.trim()));
            JOptionPane.showMessageDialog(this, "Bid #" + bid.getId() + " of ₹" + UiText.money(bid.getAmount())
                    + " accepted through the FIFO queue.");
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void myBids() {
        try {
            bidService.warmUp();
            lotService.warmUp();

            DefaultTableModel myBidsModel = new DefaultTableModel(
                    new String[]{"Bid ID", "Lot Code", "Crop", "Bid Amount (₹)", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            JTable myBidsTable = new JTable(myBidsModel);
            myBidsTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

            List<Bid> bids = bidService.byTrader(user.getId());
            for (Bid bid : bids) {
                Lot lot = lotService.findById(bid.getLotId());
                String code = lot != null ? lot.getLotCode() : ("#" + bid.getLotId());
                String crop = lot != null ? lot.getCropName() : "-";
                myBidsModel.addRow(new Object[]{
                        bid.getId(),
                        code,
                        crop,
                        UiText.money(bid.getAmount()),
                        bid.getStatus()
                });
            }

            JDialog dialog = new JDialog(this, "My Bids — " + user.getName(), true);
            dialog.setSize(620, 380);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(8, 8));

            String summary = bids.isEmpty() ? "  You have not placed any bids yet." :
                    "  Total Bids: " + bids.size() + "  (Status updates when Farmer accepts or rejects)";
            JLabel title = new JLabel(summary);
            title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            title.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 4, 12));
            dialog.add(title, BorderLayout.NORTH);
            dialog.add(new JScrollPane(myBidsTable), BorderLayout.CENTER);

            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(e -> dialog.dispose());
            JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            south.add(closeBtn);
            dialog.add(south, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void show(List<Lot> lots) {
        model.setRowCount(0);
        for (Lot lot : lots) {
            model.addRow(new Object[]{
                    lot.getId(), lot.getLotCode(), lot.getCropName(),
                    lot.getQuantity() + " " + lot.getUnit(), lot.getQualityGrade(),
                    lot.getLocation(), UiText.money(lot.getExpectedPrice()), lot.getStatus()
            });
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
