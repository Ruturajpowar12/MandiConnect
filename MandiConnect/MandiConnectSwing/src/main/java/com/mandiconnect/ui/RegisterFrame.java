package com.mandiconnect.ui;

import com.mandiconnect.service.UserService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
 * Registration window for creating a new Farmer or Trader account.
 */
public class RegisterFrame extends JFrame {

    private final UserService users;
    private final JTextField name = new JTextField();
    private final JTextField email = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JTextField phone = new JTextField();
    private final JTextField location = new JTextField();
    private final JComboBox<String> role = new JComboBox<>(new String[]{"FARMER", "TRADER"});

    public RegisterFrame(UserService users) {
        super("MandiConnect — Register");
        this.users = users;
        setSize(480, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        UiText.styleField(name);
        UiText.styleField(email);
        UiText.styleField(password);
        UiText.styleField(phone);
        UiText.styleField(location);

        // Build registration form inside a white card
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBackground(UiText.CARD_BG);
        form.add(boldLabel("Name"));
        form.add(name);
        form.add(boldLabel("Email"));
        form.add(email);
        form.add(boldLabel("Password"));
        form.add(password);
        form.add(boldLabel("Phone"));
        form.add(phone);
        form.add(boldLabel("Location"));
        form.add(location);
        form.add(boldLabel("Role"));
        form.add(role);

        JButton submit = UiText.primaryButton("Create account", e -> onSubmit());
        JButton cancel = UiText.button("Cancel", e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(UiText.CARD_BG);
        actions.add(cancel);
        actions.add(submit);

        JPanel card = UiText.card(18);
        card.add(form, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setBackground(UiText.BG_LIGHT);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        centerWrap.add(card, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiText.BG_LIGHT);
        root.add(UiText.header("Create Account", "Register as a Farmer or Trader"), BorderLayout.NORTH);
        root.add(centerWrap, BorderLayout.CENTER);
        setContentPane(root);
    }

    private static JLabel boldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiText.FONT_BOLD);
        return lbl;
    }

    /** Saves the new user account in MySQL and closes the window. */
    private void onSubmit() {
        try {
            users.register(name.getText(), email.getText(), new String(password.getPassword()),
                    phone.getText(), location.getText(), (String) role.getSelectedItem());
            JOptionPane.showMessageDialog(this, "Registration successful. Please login.");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration", JOptionPane.ERROR_MESSAGE);
        }
    }
}
