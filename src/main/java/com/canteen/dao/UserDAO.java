package com.canteen.dao;

import com.canteen.model.User;
import com.canteen.util.DBUtil;
import com.canteen.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC access for the users table.
 * Every query uses PreparedStatement - user input is NEVER concatenated
 * into SQL strings (this is what stops SQL injection).
 */
public class UserDAO {

    /** Inserts a new student; returns the generated id. */
    public int create(User user, String plainPassword) throws SQLException {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(plainPassword, salt);
        String sql = "INSERT INTO users (name, email, password_hash, salt, phone, role) "
                   + "VALUES (?, ?, ?, ?, ?, 'STUDENT')";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.setString(5, user.getPhone());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Looks up a user by email (for login). Returns null when not found. */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, password_hash, salt, phone, role, email_verified, created_at "
                   + "FROM users WHERE email = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, name, email, password_hash, salt, phone, role, email_verified, created_at "
                   + "FROM users WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Stores (or refreshes) the 6-digit email verification code. */
    public void setVerificationCode(String email, String code, java.time.LocalDateTime expires)
            throws SQLException {
        String sql = "UPDATE users SET verify_code = ?, verify_expires = ? WHERE email = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(expires));
            ps.setString(3, email);
            ps.executeUpdate();
        }
    }

    /** Returns the live (unexpired) verification code for an email, or null. */
    public String getLiveVerificationCode(String email) throws SQLException {
        String sql = "SELECT verify_code FROM users WHERE email = ? "
                   + "AND verify_expires > NOW()";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** Marks the email verified and clears the one-time code. */
    public void markEmailVerified(String email) throws SQLException {
        String sql = "UPDATE users SET email_verified = TRUE, verify_code = NULL, "
                   + "verify_expires = NULL WHERE email = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    /** Replaces the password (fresh salt + hash). Used by the change-password screen. */
    public void updatePassword(int userId, String plainPassword) throws SQLException {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(plainPassword, salt);
        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        try { u.setEmailVerified(rs.getBoolean("email_verified")); }
        catch (SQLException ignored) { u.setEmailVerified(false); }
        if (rs.getTimestamp("created_at") != null) {
            u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return u;
    }
}
