package com.inventory.service;

import com.inventory.dao.UserDAO;
import com.inventory.model.User;

import java.sql.SQLException;

public class AuthenticationService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return user;
    }
}
