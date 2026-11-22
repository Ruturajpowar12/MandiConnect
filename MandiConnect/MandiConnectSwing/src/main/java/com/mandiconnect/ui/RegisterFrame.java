package com.mandiconnect.ui;

import com.mandiconnect.service.UserService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Creates a farmer or trader account. The admin account is seeded at startup, not here.
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
        setSize(460, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Name"));
        form.add(name);
        form.add(new JLabel("Email"));
        form.add(email);
        form.add(new JLabel("Password"));
        form.add(password);
        form.add(new JLabel("Phone"));
        form.add(phone);
        form.add(new JLabel("Location"));
        form.add(location);
        form.add(new JLabel("Role"));
        form.add(role);

        JButton submit = new JButton("Create account");
        submit.addActionListener(e -> onSubmit());

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.add(form, BorderLayout.CENTER);
        root.add(submit, BorderLayout.SOUTH);
        setContentPane(root);
    }

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
