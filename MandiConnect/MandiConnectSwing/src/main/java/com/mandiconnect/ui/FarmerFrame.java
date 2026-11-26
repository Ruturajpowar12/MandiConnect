package com.mandiconnect.ui;

import com.mandiconnect.model.Bid;
import com.mandiconnect.model.Lot;
import com.mandiconnect.model.User;
import com.mandiconnect.service.AuctionService;
import com.mandiconnect.service.BidService;
import com.mandiconnect.service.LotService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

/**
 * Farmer dashboard: add crop lots, view incoming bids (MaxHeap & BidLinkedList),
 * accept/reject bids, and start/close auctions.
 */
public class FarmerFrame extends JFrame {

    private final User user;
    private final LotService lotService = new LotService();
    private final BidService bidService = new BidService();
    private final AuctionService auctionService = new AuctionService(lotService, bidService);
    private final DefaultTableModel model;
    private final JTable table;

    public FarmerFrame(User user) {
        super("MandiConnect — Farmer — " + user.getName());
        this.user = user;
        setSize(1100, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Farmer's lots table
        model = UiText.readOnlyModel("ID", "Code", "Crop", "Qty", "Grade", "Location", "Price", "Status");
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Top toolbar buttons
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(UiText.button("+ Add New Lot", e -> addLot()));
        bar.add(UiText.button("View Bids", e -> viewBids()));
        bar.add(UiText.button("Start Auction", e -> startAuction()));
        bar.add(UiText.button("Close Auction", e -> closeAuction()));
        bar.add(UiText.button("Refresh", e -> reload()));
        bar.add(UiText.button("Algorithm Console", e -> new AlgorithmConsoleFrame(lotService, bidService).setVisible(true)));
        bar.add(UiText.button("Logout", e -> logout()));

        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new JLabel("  Lots are indexed in the hash table and the price BST as soon as they are saved."), BorderLayout.SOUTH);

        warmUpServices();
        reload();
    }

    /** Shows a popup form to create a new lot and indexes it in HashTable + BST. */
    private void addLot() {
        JTextField crop = new JTextField();
        JTextField qty = new JTextField("100");
        JComboBox<String> unit = new JComboBox<>(new String[]{"kg", "quintal", "tonne"});
        JComboBox<String> grade = new JComboBox<>(new String[]{"A", "B", "C"});
        JTextField location = new JTextField(user.getLocation() == null ? "" : user.getLocation());
        JTextField price = new JTextField("2500");
        JTextArea description = new JTextArea(3, 20);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Crop"));
        form.add(crop);
        form.add(new JLabel("Quantity"));
        form.add(qty);
        form.add(new JLabel("Unit"));
        form.add(unit);
        form.add(new JLabel("Grade"));
        form.add(grade);
        form.add(new JLabel("Location"));
        form.add(location);
        form.add(new JLabel("Expected price"));
        form.add(price);
        form.add(new JLabel("Description"));
        form.add(new JScrollPane(description));

        int choice = JOptionPane.showConfirmDialog(this, form, "Add New Lot", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Lot lot = lotService.create(
                    user.getId(),
                    crop.getText(),
                    Double.parseDouble(qty.getText().trim()),
                    (String) unit.getSelectedItem(),
                    (String) grade.getSelectedItem(),
                    location.getText(),
                    Double.parseDouble(price.getText().trim()),
                    description.getText());
            reload();
            JOptionPane.showMessageDialog(this, "Saved " + lot.getLotCode());
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Opens a dialog showing the MaxHeap highest bid and full BidLinkedList history. */
    private void viewBids() {
        Lot lot = selectedLot();
        if (lot == null) {
            return;
        }
        warmUpServices();

        JDialog dialog = new JDialog(this, "Bids for " + lot.getLotCode() + " — " + lot.getCropName(), true);
        dialog.setSize(680, 480);
        dialog.setLocationRelativeTo(this);

        JLabel lotInfo = new JLabel(formatLotSummary(lot));
        lotInfo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        // Peek highest bid from MaxHeap in O(1)
        Bid top = bidService.highestBid(lot.getId());
        String topText = (top == null)
                ? "Highest Offer (MaxHeap Peek): None yet"
                : "Highest Offer (MaxHeap Peek): ₹" + UiText.money(top.getAmount())
                        + " by Trader #" + top.getTraderId() + " [" + top.getStatus() + "]";
        JLabel heapLabel = new JLabel(topText);
        heapLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

        JPanel topPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        topPanel.add(lotInfo);
        topPanel.add(heapLabel);

        // Table showing all bids from BidLinkedList
        DefaultTableModel bidModel = UiText.readOnlyModel("Bid ID", "Trader", "Bid Amount (₹)", "Status");
        JTable bidTable = new JTable(bidModel);
        bidTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bidTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        loadLotBids(lot.getId(), bidModel);

        // Action buttons for accepting/rejecting a selected bid
        JButton acceptBtn = UiText.button("✔ Accept Selected Bid",
                e -> acceptSelectedBid(dialog, lot, bidTable, bidModel, lotInfo));
        JButton rejectBtn = UiText.button("✖ Reject Selected Bid",
                e -> rejectSelectedBid(dialog, lot, bidTable, bidModel));
        JButton closeBtn = UiText.button("Close", e -> dialog.dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(acceptBtn);
        actions.add(rejectBtn);
        actions.add(closeBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(bidTable), BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /** Fills the bid dialog table using BidLinkedList history (newest first). */
    private void loadLotBids(long lotId, DefaultTableModel bidModel) {
        bidModel.setRowCount(0);
        try {
            for (Bid b : bidService.history(lotId)) {
                bidModel.addRow(new Object[]{
                        b.getId(), "Trader #" + b.getTraderId(), UiText.money(b.getAmount()), b.getStatus()
                });
            }
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Marks the chosen bid ACCEPTED, rejects other pending bids, and marks lot SOLD. */
    private void acceptSelectedBid(JDialog dialog, Lot lot, JTable bidTable,
                                   DefaultTableModel bidModel, JLabel lotInfo) {
        int row = bidTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(dialog, "Please select a bid from the table to accept.", "Select Bid", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long bidId = ((Number) bidModel.getValueAt(row, 0)).longValue();
        String trader = String.valueOf(bidModel.getValueAt(row, 1));
        String amount = String.valueOf(bidModel.getValueAt(row, 2));
        String status = String.valueOf(bidModel.getValueAt(row, 3));

        if ("ACCEPTED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(dialog, "This bid is already ACCEPTED.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(dialog,
                "Accept Bid #" + bidId + " of ₹" + amount + " from " + trader + "?\n\n"
                        + "• This bid will become ACCEPTED.\n"
                        + "• Other pending bids will become REJECTED.\n"
                        + "• The lot status will be set to SOLD.",
                "Confirm Accept Bid", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                auctionService.acceptBid(lot.getId(), bidId);
                warmUpServices();
                loadLotBids(lot.getId(), bidModel);
                reload();
                lot.setStatus("SOLD");
                lotInfo.setText(formatLotSummary(lot));
                JOptionPane.showMessageDialog(dialog,
                        "Bid #" + bidId + " ACCEPTED!\n"
                                + "The trader's status has been updated to ACCEPTED.\n"
                                + "Lot " + lot.getLotCode() + " is now SOLD.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                UiText.showError(this, ex);
            }
        }
    }

    /** Marks the selected bid as REJECTED. */
    private void rejectSelectedBid(JDialog dialog, Lot lot, JTable bidTable, DefaultTableModel bidModel) {
        int row = bidTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(dialog, "Please select a bid from the table to reject.", "Select Bid", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long bidId = ((Number) bidModel.getValueAt(row, 0)).longValue();
        int choice = JOptionPane.showConfirmDialog(dialog,
                "Are you sure you want to REJECT Bid #" + bidId + "?",
                "Confirm Reject Bid", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                auctionService.rejectBid(bidId);
                warmUpServices();
                loadLotBids(lot.getId(), bidModel);
                JOptionPane.showMessageDialog(dialog, "Bid #" + bidId + " marked as REJECTED.", "Bid Rejected", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                UiText.showError(this, ex);
            }
        }
    }

    private static String formatLotSummary(Lot lot) {
        return "Lot: " + lot.getLotCode() + " | " + lot.getCropName()
                + " | Qty: " + lot.getQuantity() + " " + lot.getUnit()
                + " | Expected: ₹" + UiText.money(lot.getExpectedPrice())
                + " | Lot Status: " + lot.getStatus();
    }

    /** Opens an auction for the selected lot for a given number of minutes. */
    private void startAuction() {
        Lot lot = selectedLot();
        if (lot == null) {
            return;
        }
        String minutes = JOptionPane.showInputDialog(this, "Auction duration (minutes)", "30");
        if (minutes == null) {
            return;
        }
        try {
            auctionService.start(lot.getId(), Integer.parseInt(minutes.trim()));
            reload();
            JOptionPane.showMessageDialog(this, lot.getLotCode() + " is now IN_AUCTION.");
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Closes the auction and awards the lot to the MaxHeap winner. */
    private void closeAuction() {
        Lot lot = selectedLot();
        if (lot == null) {
            return;
        }
        try {
            auctionService.close(lot.getId());
            reload();
            JOptionPane.showMessageDialog(this, lot.getLotCode() + " auction closed. Status follows the heap winner.");
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Reloads the farmer's lots from MySQL into the table. */
    private void reload() {
        model.setRowCount(0);
        try {
            for (Lot lot : lotService.byFarmer(user.getId())) {
                model.addRow(new Object[]{
                        lot.getId(), lot.getLotCode(), lot.getCropName(),
                        lot.getQuantity() + " " + lot.getUnit(), lot.getQualityGrade(),
                        lot.getLocation(), UiText.money(lot.getExpectedPrice()), lot.getStatus()
                });
            }
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    /** Returns the Lot currently selected in the table, or null with a prompt. */
    private Lot selectedLot() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a lot first.");
            return null;
        }
        long id = ((Number) model.getValueAt(row, 0)).longValue();
        try {
            return lotService.findById(id);
        } catch (Exception ex) {
            UiText.showError(this, ex);
            return null;
        }
    }

    /** Refreshes in-memory HashTable, BST, MaxHeap, and LinkedList from MySQL. */
    private void warmUpServices() {
        try {
            lotService.warmUp();
            bidService.warmUp();
        } catch (Exception ex) {
            UiText.showError(this, ex);
        }
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
