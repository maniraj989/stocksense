package com.inventory.model;

import java.time.LocalDateTime;

public class Admin extends User {
    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int id, String name, String username, String password, String email, LocalDateTime createdAt) {
        super(id, name, username, password, "ADMIN", email, createdAt);
    }
}
