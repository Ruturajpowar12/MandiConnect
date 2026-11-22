package com.mandiconnect.ui;

import com.mandiconnect.model.Bid;
import com.mandiconnect.model.Lot;
import com.mandiconnect.model.User;
import com.mandiconnect.service.AuctionService;
import com.mandiconnect.service.BidService;
import com.mandiconnect.service.LotService;

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
import java.util.List;

/**
 * Farmer dashboard: create lots, read the heap and the bid history, run the auction.
 */
public class FarmerFrame extends JFrame {

    private final User user;
    private final LotService lotService = new LotService();
    private final BidService bidService = new BidService();
    private final AuctionService auctionService;
    private final DefaultTableModel model;
    private final JTable table;

    public FarmerFrame(User user) {
        super("MandiConnect — Farmer — " + user.getName());
        this.user = user;
        this.auctionService = new AuctionService(lotService, bidService);
        setSize(1100, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"ID", "Code", "Crop", "Qty", "Grade", "Location", "Price", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(button("+ Add New Lot", this::addLot));
        bar.add(button("View Bids", this::viewBids));
        bar.add(button("Start Auction", this::startAuction));
        bar.add(button("Close Auction", this::closeAuction));
        bar.add(button("Refresh", this::reload));
        bar.add(button("Algorithm Console", this::openConsole));
        bar.add(button("Logout", this::logout));

        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new JLabel("  Lots are indexed in the hash table and the price BST as soon as they are saved."), BorderLayout.SOUTH);

        try {
            lotService.warmUp();
            bidService.warmUp();
        } catch (Exception ex) {
            error(ex);
        }
        reload();
    }

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
            Lot lot = lotService.create(user.getId(), crop.getText(), Double.parseDouble(qty.getText().trim()),
                    (String) unit.getSelectedItem(), (String) grade.getSelectedItem(), location.getText(),
                    Double.parseDouble(price.getText().trim()), description.getText());
            reload();
            JOptionPane.showMessageDialog(this, "Saved " + lot.getLotCode());
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void viewBids() {
        Lot lot = selectedLot();
        if (lot == null) {
            return;
        }
        try {
            lotService.warmUp();
            bidService.warmUp();

            JDialog dialog = new JDialog(this, "Bids for " + lot.getLotCode() + " — " + lot.getCropName(), true);
            dialog.setSize(680, 480);
            dialog.setLocationRelativeTo(this);

            JPanel topPanel = new JPanel(new GridLayout(0, 1, 4, 4));
            topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));

            Bid top = bidService.highestBid(lot.getId());
            JLabel lotInfo = new JLabel("Lot: " + lot.getLotCode() + " | " + lot.getCropName()
                    + " | Qty: " + lot.getQuantity() + " " + lot.getUnit()
                    + " | Expected: ₹" + UiText.money(lot.getExpectedPrice())
                    + " | Lot Status: " + lot.getStatus());
            lotInfo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

            String topText = (top == null) ? "Highest Offer (MaxHeap Peek): None yet" :
                    "Highest Offer (MaxHeap Peek): ₹" + UiText.money(top.getAmount())
                            + " by Trader #" + top.getTraderId() + " [" + top.getStatus() + "]";
            JLabel heapLabel = new JLabel(topText);
            heapLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

            topPanel.add(lotInfo);
            topPanel.add(heapLabel);

            DefaultTableModel bidModel = new DefaultTableModel(
                    new String[]{"Bid ID", "Trader", "Bid Amount (₹)", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            JTable bidTable = new JTable(bidModel);
            bidTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            bidTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

            Runnable refreshBids = () -> {
                bidModel.setRowCount(0);
                try {
                    List<Bid> history = bidService.history(lot.getId());
                    for (Bid b : history) {
                        bidModel.addRow(new Object[]{
                                b.getId(),
                                "Trader #" + b.getTraderId(),
                                UiText.money(b.getAmount()),
                                b.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    error(ex);
                }
            };
            refreshBids.run();

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton acceptBtn = new JButton("✔ Accept Selected Bid");
            JButton rejectBtn = new JButton("✖ Reject Selected Bid");
            JButton closeBtn = new JButton("Close");

            acceptBtn.addActionListener(e -> {
                int row = bidTable.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Please select a bid from the table to accept.", "Select Bid", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                long bidId = ((Number) bidModel.getValueAt(row, 0)).longValue();
                String trader = String.valueOf(bidModel.getValueAt(row, 1));
                String amount = String.valueOf(bidModel.getValueAt(row, 2));
                String currentStatus = String.valueOf(bidModel.getValueAt(row, 3));

                if ("ACCEPTED".equalsIgnoreCase(currentStatus)) {
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
                        lotService.warmUp();
                        bidService.warmUp();
                        refreshBids.run();
                        reload(); // update farmer main table with SOLD status
                        lot.setStatus("SOLD");
                        lotInfo.setText("Lot: " + lot.getLotCode() + " | " + lot.getCropName()
                                + " | Qty: " + lot.getQuantity() + " " + lot.getUnit()
                                + " | Expected: ₹" + UiText.money(lot.getExpectedPrice())
                                + " | Lot Status: SOLD");
                        JOptionPane.showMessageDialog(dialog,
                                "Bid #" + bidId + " ACCEPTED!\n"
                                        + "The trader's status has been updated to ACCEPTED.\n"
                                        + "Lot " + lot.getLotCode() + " is now SOLD.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        error(ex);
                    }
                }
            });

            rejectBtn.addActionListener(e -> {
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
                        lotService.warmUp();
                        bidService.warmUp();
                        refreshBids.run();
                        JOptionPane.showMessageDialog(dialog, "Bid #" + bidId + " marked as REJECTED.", "Bid Rejected", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        error(ex);
                    }
                }
            });

            closeBtn.addActionListener(e -> dialog.dispose());

            actions.add(acceptBtn);
            actions.add(rejectBtn);
            actions.add(closeBtn);

            dialog.setLayout(new BorderLayout());
            dialog.add(topPanel, BorderLayout.NORTH);
            dialog.add(new JScrollPane(bidTable), BorderLayout.CENTER);
            dialog.add(actions, BorderLayout.SOUTH);
            dialog.setVisible(true);

        } catch (Exception ex) {
            error(ex);
        }
    }

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
            error(ex);
        }
    }

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
            error(ex);
        }
    }

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
            error(ex);
        }
    }

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
            error(ex);
            return null;
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
