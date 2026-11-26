package com.mandiconnect.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shared UI theme and helper methods so all windows have a clean, consistent look.
 */
public final class UiText {

    // Agricultural MandiConnect color palette
    public static final Color PRIMARY = new Color(27, 94, 32);        // Deep mandi green
    public static final Color PRIMARY_HOVER = new Color(46, 125, 50); // Lighter green on hover
    public static final Color ACCENT = new Color(217, 119, 6);        // Harvest amber (Algorithm Console)
    public static final Color ACCENT_HOVER = new Color(245, 158, 11);
    public static final Color DANGER = new Color(185, 28, 28);        // Red (Logout / Reject)
    public static final Color DANGER_HOVER = new Color(220, 38, 38);
    public static final Color BG_LIGHT = new Color(244, 247, 244);    // Soft mint-gray window background
    public static final Color CARD_BG = Color.WHITE;                  // Clean white card background
    public static final Color ROW_ALT = new Color(247, 250, 247);     // Subtle zebra row tint
    public static final Color SELECTION_BG = new Color(209, 235, 212);// Soft green row highlight
    public static final Color BORDER_COLOR = new Color(214, 224, 214);
    public static final Color TEXT_DARK = new Color(31, 41, 55);
    public static final Color TEXT_MUTED = new Color(90, 104, 92);

    // Shared fonts
    public static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font FONT_BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private UiText() {
    }

    /** Sets clean Swing defaults across the application (called once at startup). */
    public static void initTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("Panel.background", BG_LIGHT);
        UIManager.put("OptionPane.background", BG_LIGHT);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Label.foreground", TEXT_DARK);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("PasswordField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("TabbedPane.font", FONT_BOLD);
    }

    /** Formats whole numbers without decimals (e.g. 2500) and decimals with 2 digits. */
    static String money(double value) {
        if (Double.isFinite(value) && value == Math.rint(value)) {
            return String.format("%.0f", value);
        }
        return String.format("%.2f", value);
    }

    /** Creates a JTable model whose cells cannot be edited by the user. */
    static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Creates a green header banner with title and subtitle for the top of a window. */
    static JPanel header(String title, String subtitle) {
        JPanel banner = new JPanel(new GridLayout(2, 1, 0, 2));
        banner.setBackground(PRIMARY);
        banner.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(FONT_SUBTITLE);
        subLbl.setForeground(new Color(216, 240, 218));

        banner.add(titleLbl);
        banner.add(subLbl);
        return banner;
    }

    /** Builds a padded white card panel with a subtle border. */
    static JPanel card(int padding) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        Border line = BorderFactory.createLineBorder(BORDER_COLOR, 1);
        Border pad = BorderFactory.createEmptyBorder(padding, padding, padding, padding);
        card.setBorder(BorderFactory.createCompoundBorder(line, pad));
        return card;
    }

    /** Creates a styled bottom status bar label. */
    static JLabel statusBar(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(TEXT_MUTED);
        label.setOpaque(true);
        label.setBackground(new Color(235, 241, 235));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        return label;
    }

    /** Applies padding and a clean border to text fields. */
    static void styleField(JComponent field) {
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    /** Creates a secondary (white/bordered) button. */
    static JButton button(String label, ActionListener action) {
        return styledButton(label, Color.WHITE, TEXT_DARK, new Color(236, 243, 236), true, action);
    }

    /** Creates a primary green action button. */
    static JButton primaryButton(String label, ActionListener action) {
        return styledButton(label, PRIMARY, Color.WHITE, PRIMARY_HOVER, false, action);
    }

    /** Creates an amber accent button (used for Algorithm Console). */
    static JButton accentButton(String label, ActionListener action) {
        return styledButton(label, ACCENT, Color.WHITE, ACCENT_HOVER, false, action);
    }

    /** Creates a red danger button (used for Logout / Reject). */
    static JButton dangerButton(String label, ActionListener action) {
        return styledButton(label, DANGER, Color.WHITE, DANGER_HOVER, false, action);
    }

    private static JButton styledButton(String label, Color bg, Color fg, Color hoverBg,
                                        boolean bordered, ActionListener action) {
        JButton btn = new JButton(label);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Border pad = BorderFactory.createEmptyBorder(7, 13, 7, 13);
        if (bordered) {
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1), pad));
        } else {
            btn.setBorder(pad);
        }
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(hoverBg);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        btn.addActionListener(action);
        return btn;
    }

    /** Styles a JTable with clean headers, row height, zebra stripes, and status colors. */
    static void styleTable(JTable table, int statusColumnIndex) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(232, 238, 232));
        table.setIntercellSpacing(new Dimension(8, 1));
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(232, 240, 232));
        header.setForeground(PRIMARY);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        header.setReorderingAllowed(false);

        // Zebra-stripe rows and color-code Status/Active values
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                }
                c.setForeground(TEXT_DARK);
                setFont(FONT_BODY);

                // Highlight status column with semantic colors
                if (col == statusColumnIndex && val != null) {
                    String s = val.toString().toUpperCase();
                    setFont(FONT_BOLD);
                    if (s.equals("AVAILABLE") || s.equals("ACCEPTED") || s.equals("WINNING") || s.equals("YES") || s.equals("✔")) {
                        c.setForeground(new Color(21, 128, 61));
                    } else if (s.equals("IN_AUCTION") || s.equals("PENDING")) {
                        c.setForeground(new Color(180, 83, 9));
                    } else if (s.equals("SOLD") || s.equals("REJECTED") || s.equals("NO") || s.equals("✘")) {
                        c.setForeground(new Color(185, 28, 28));
                    }
                }
                return c;
            }
        });
    }

    /** Wraps a table in a clean bordered scroll pane with outer padding. */
    static JScrollPane tablePane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    /** Styles a search text field with placeholder tooltip and padding. */
    static JTextField searchField(int columns, String tooltip) {
        JTextField field = new JTextField(columns);
        field.setToolTipText(tooltip);
        styleField(field);
        return field;
    }

    /** Shows an error popup dialog with the exception message. */
    static void showError(Component parent, Exception ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "MandiConnect", JOptionPane.ERROR_MESSAGE);
    }
}
