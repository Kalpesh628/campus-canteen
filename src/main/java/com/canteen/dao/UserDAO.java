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
        String sql = "SELECT id, name, email, password_hash, salt, phone, role, created_at "
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
        String sql = "SELECT id, name, email, password_hash, salt, phone, role, created_at "
                   + "FROM users WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
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
        if (rs.getTimestamp("created_at") != null) {
            u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return u;
    }
}
