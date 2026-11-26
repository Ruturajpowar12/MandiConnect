package com.mandiconnect.ui;

import com.mandiconnect.model.Bid;
import com.mandiconnect.model.Lot;
import com.mandiconnect.model.User;
import com.mandiconnect.service.BidService;
import com.mandiconnect.service.LotService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
 * Trader dashboard:
 * - Search by LOT-code (HashTable) or crop name
 * - Sort lots by price (QuickSort)
 * - Filter lots by price window (BinarySearchTree)
 * - Place bids (BidQueue FIFO)
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

        // Available lots table
        model = UiText.readOnlyModel("ID", "Code", "Crop", "Qty", "Grade", "Location", "Price", "Status");
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cropSearch.setToolTipText("Enter crop name (e.g. Wheat, Mango) or Lot Code");
        cropSearch.addActionListener(e -> searchCrop());

        // Top toolbar buttons
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(cropSearch);
        bar.add(UiText.button("Search Crop", e -> searchCrop()));
        bar.add(UiText.button("Sort by Price", e -> sortByPrice()));
        bar.add(UiText.button("Price Range (BST)", e -> priceRange()));
        bar.add(UiText.button("Show All", e -> showAvailable()));
        bar.add(UiText.button("Refresh", e -> showAvailable()));
        bar.add(UiText.button("Place Bid", e -> placeBid()));
        bar.add(UiText.button("My Bids", e -> myBids()));
        bar.add(UiText.button("Algorithm Console", e -> new AlgorithmConsoleFrame(lotService, bidService).setVisible(true)));
        bar.add(UiText.button("Logout", e -> logout()));

        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new JLabel("  Available lots. Sort by Price runs QuickSort. Price Range runs the BST."), BorderLayout.SOUTH);

        // Load ADS structures from MySQL on startup
        try {
            lotService.warmUp();
            bidService.warmUp();
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
        showAvailable();
    }

    /** Searches first by exact LOT-code in HashTable (O(1)), else by crop substring. */
    private void searchCrop() {
        String query = cropSearch.getText().trim();
        if (query.isEmpty()) {
            showAvailable();
            return;
        }
        try {
            lotService.warmUp();
            Lot byCode = lotService.findByCode(query.toUpperCase(Locale.ROOT));
            if (byCode != null && !"SOLD".equalsIgnoreCase(byCode.getStatus())) {
                showLots(List.of(byCode));
                return;
            }
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
            showLots(hits);
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Sorts available lots by expected price using QuickSort. */
    private void sortByPrice() {
        try {
            lotService.warmUp();
            showLots(lotService.availableSortedByPrice());
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Filters lots within [min, max] price using BinarySearchTree. */
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
                JOptionPane.showMessageDialog(this,
                        "No lots found in price range ₹" + UiText.money(minVal) + " to ₹" + UiText.money(maxVal),
                        "Price Range (BST)", JOptionPane.INFORMATION_MESSAGE);
            }
            showLots(hits);
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Loads all available/in-auction lots into the table. */
    private void showAvailable() {
        try {
            lotService.warmUp();
            bidService.warmUp();
            showLots(lotService.available());
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Prompts for a bid amount and sends it through the FIFO BidQueue. */
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
            UiText.showError(this, ex);
        }
    }

    /** Opens a dialog showing all bids placed by this trader and their current status. */
    private void myBids() {
        try {
            bidService.warmUp();
            lotService.warmUp();

            DefaultTableModel myBidsModel = UiText.readOnlyModel("Bid ID", "Lot Code", "Crop", "Bid Amount (₹)", "Status");
            JTable myBidsTable = new JTable(myBidsModel);
            myBidsTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

            List<Bid> bids = bidService.byTrader(user.getId());
            for (Bid bid : bids) {
                Lot lot = lotService.findById(bid.getLotId());
                String code = (lot != null) ? lot.getLotCode() : ("#" + bid.getLotId());
                String crop = (lot != null) ? lot.getCropName() : "-";
                myBidsModel.addRow(new Object[]{bid.getId(), code, crop, UiText.money(bid.getAmount()), bid.getStatus()});
            }

            JDialog dialog = new JDialog(this, "My Bids — " + user.getName(), true);
            dialog.setSize(620, 380);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(8, 8));

            String summary = bids.isEmpty()
                    ? "  You have not placed any bids yet."
                    : "  Total Bids: " + bids.size() + "  (Status updates when Farmer accepts or rejects)";
            JLabel title = new JLabel(summary);
            title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            title.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));

            JButton closeBtn = UiText.button("Close", e -> dialog.dispose());
            JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            south.add(closeBtn);

            dialog.add(title, BorderLayout.NORTH);
            dialog.add(new JScrollPane(myBidsTable), BorderLayout.CENTER);
            dialog.add(south, BorderLayout.SOUTH);
            dialog.setVisible(true);
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Replaces table rows with the given list of lots. */
    private void showLots(List<Lot> lots) {
        model.setRowCount(0);
        for (Lot lot : lots) {
            model.addRow(new Object[]{
                    lot.getId(), lot.getLotCode(), lot.getCropName(),
                    lot.getQuantity() + " " + lot.getUnit(), lot.getQualityGrade(),
                    lot.getLocation(), UiText.money(lot.getExpectedPrice()), lot.getStatus()
            });
        }
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
