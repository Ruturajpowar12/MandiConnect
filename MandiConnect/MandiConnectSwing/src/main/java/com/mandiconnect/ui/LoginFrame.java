package com.mandiconnect.ui;

import com.mandiconnect.model.User;
import com.mandiconnect.service.UserService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Email and password gate. Farmers, traders, and the admin each get their own frame.
 */
public class LoginFrame extends JFrame {

    private final UserService users = new UserService();
    private final JTextField email = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);

    public LoginFrame() {
        super("MandiConnect — Login");
        setSize(420, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Email"));
        form.add(email);
        form.add(new JLabel("Password"));
        form.add(password);

        JButton login = new JButton("Login");
        JButton register = new JButton("Register");
        login.addActionListener(e -> onLogin());
        register.addActionListener(e -> new RegisterFrame(users).setVisible(true));
        password.addActionListener(e -> onLogin());

        JPanel actions = new JPanel();
        actions.add(login);
        actions.add(register);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.add(new JLabel("MandiConnect — Agricultural Auction"), BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setContentPane(root);
    }

    private void onLogin() {
        try {
            User user = users.authenticate(email.getText(), new String(password.getPassword()));
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login", JOptionPane.WARNING_MESSAGE);
                return;
            }
            switch (user.getRole()) {
                case "FARMER" -> new FarmerFrame(user).setVisible(true);
                case "TRADER" -> new TraderFrame(user).setVisible(true);
                case "ADMIN" -> new AdminFrame(user).setVisible(true);
                default -> {
                    JOptionPane.showMessageDialog(this, "Unknown role: " + user.getRole());
                    return;
                }
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
