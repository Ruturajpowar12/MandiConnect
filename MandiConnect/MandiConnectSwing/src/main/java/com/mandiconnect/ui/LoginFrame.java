package com.mandiconnect.ui;

import com.mandiconnect.model.User;
import com.mandiconnect.service.UserService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Login window that opens Farmer, Trader, or Admin dashboard based on user role.
 */
public class LoginFrame extends JFrame {

    private final UserService users = new UserService();
    private final JTextField email = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);

    public LoginFrame() {
        super("MandiConnect — Login");
        setSize(460, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        UiText.styleField(email);
        UiText.styleField(password);
        password.addActionListener(e -> onLogin()); // Press Enter to log in

        // Input fields inside a clean white card
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 12));
        form.setBackground(UiText.CARD_BG);
        JLabel emailLbl = new JLabel("Email");
        emailLbl.setFont(UiText.FONT_BOLD);
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(UiText.FONT_BOLD);
        form.add(emailLbl);
        form.add(email);
        form.add(passLbl);
        form.add(password);

        // Login and Register buttons
        JButton login = UiText.primaryButton("Login", e -> onLogin());
        JButton register = UiText.button("Register", e -> new RegisterFrame(users).setVisible(true));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(UiText.CARD_BG);
        actions.add(register);
        actions.add(login);

        JPanel card = UiText.card(20);
        card.add(form, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setBackground(UiText.BG_LIGHT);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        centerWrap.add(card, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiText.BG_LIGHT);
        root.add(UiText.header("MandiConnect", "Smart Agricultural Auction & Bidding System"), BorderLayout.NORTH);
        root.add(centerWrap, BorderLayout.CENTER);
        setContentPane(root);
    }

    /** Verifies email/password and opens the matching role dashboard. */
    private void onLogin() {
        try {
            User user = users.authenticate(email.getText(), new String(password.getPassword()));
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String role = user.getRole();
            if ("FARMER".equals(role)) {
                new FarmerFrame(user).setVisible(true);
            } else if ("TRADER".equals(role)) {
                new TraderFrame(user).setVisible(true);
            } else if ("ADMIN".equals(role)) {
                new AdminFrame(user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Unknown role: " + role);
                return;
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
