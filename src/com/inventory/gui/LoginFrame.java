package com.inventory.gui;

import com.inventory.model.User;
import com.inventory.service.AuthenticationService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthenticationService authenticationService = new AuthenticationService();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame() {
        super("Smart Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        formPanel.add(new JLabel("Username"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password"));
        formPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton clearButton = new JButton("Clear");
        formPanel.add(loginButton);
        formPanel.add(clearButton);

        JLabel title = new JLabel("Smart Inventory System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> handleLogin());
        clearButton.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
        });
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            User user = authenticationService.login(username, password);
            JOptionPane.showMessageDialog(this, "Welcome, " + user.getName() + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
            DashboardFrame dashboard = new DashboardFrame(user);
            dashboard.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
