package com.inventory.service;

import com.inventory.model.User;

public interface Authenticatable {
    User login(String username, String password) throws Exception;
    boolean logout(User user);
}
