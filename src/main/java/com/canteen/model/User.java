package com.canteen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A registered user. Stored in the HTTP session after login
 * (session attribute "user"), so it must be Serializable.
 */
public class User implements Serializable {

    private int id;
    private String name;
    private String email;
    private String passwordHash; // SHA-256(salt + password); never the plaintext
    private String salt;
    private String phone;
    private String role;         // "STUDENT" or "ADMIN"
    private boolean emailVerified;
    private LocalDateTime createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() { return "ADMIN".equals(role); }
}
