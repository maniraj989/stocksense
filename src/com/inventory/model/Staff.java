package com.inventory.model;

import java.time.LocalDateTime;

public class Staff extends User {
    public Staff() {
        super();
        setRole("STAFF");
    }

    public Staff(int id, String name, String username, String password, String email, LocalDateTime createdAt) {
        super(id, name, username, password, "STAFF", email, createdAt);
    }
}
