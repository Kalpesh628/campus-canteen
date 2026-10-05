package com.canteen.dao;

import com.canteen.model.MenuItem;
import com.canteen.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** JDBC access for the menu_items table. */
public class MenuItemDAO {

    /**
     * Student-facing search. Any filter may be null/blank (= no filter).
     * Only available items are returned to students.
     */
    public List<MenuItem> search(String q, String category, String veg) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, name, description, price, category, veg, available, image_url "
              + "FROM menu_items WHERE available = TRUE");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + q.trim() + "%");
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }
        if ("veg".equals(veg)) {
            sql.append(" AND veg = TRUE");
        } else if ("nonveg".equals(veg)) {
            sql.append(" AND veg = FALSE");
        }
        sql.append(" ORDER BY category, name");

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<MenuItem> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    /** All items including unavailable ones, for the admin menu page. */
    public List<MenuItem> findAll() throws SQLException {
        String sql = "SELECT id, name, description, price, category, veg, available, image_url "
                   + "FROM menu_items ORDER BY category, name";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<MenuItem> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public List<String> distinctCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM menu_items ORDER BY category";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> out = new ArrayList<>();
            while (rs.next()) out.add(rs.getString(1));
            return out;
        }
    }

    public MenuItem findById(int id) throws SQLException {
        String sql = "SELECT id, name, description, price, category, veg, available, image_url "
                   + "FROM menu_items WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int insert(MenuItem m) throws SQLException {
        String sql = "INSERT INTO menu_items (name, description, price, category, veg, available, image_url) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getDescription());
            ps.setBigDecimal(3, m.getPrice());
            ps.setString(4, m.getCategory());
            ps.setBoolean(5, m.isVeg());
            ps.setBoolean(6, m.isAvailable());
            ps.setString(7, m.getImageUrl());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void update(MenuItem m) throws SQLException {
        String sql = "UPDATE menu_items SET name=?, description=?, price=?, category=?, "
                   + "veg=?, available=?, image_url=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getDescription());
            ps.setBigDecimal(3, m.getPrice());
            ps.setString(4, m.getCategory());
            ps.setBoolean(5, m.isVeg());
            ps.setBoolean(6, m.isAvailable());
            ps.setString(7, m.getImageUrl());
            ps.setInt(8, m.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // Items referenced by old orders are protected by the FK; the admin
        // should use the "available" flag instead of deleting history items.
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Number of items currently marked unavailable (dashboard "low stock" card). */
    public int countUnavailable() throws SQLException {
        String sql = "SELECT COUNT(*) FROM menu_items WHERE available = FALSE";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private MenuItem map(ResultSet rs) throws SQLException {
        MenuItem m = new MenuItem();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        m.setDescription(rs.getString("description"));
        m.setPrice(rs.getBigDecimal("price"));
        m.setCategory(rs.getString("category"));
        m.setVeg(rs.getBoolean("veg"));
        m.setAvailable(rs.getBoolean("available"));
        m.setImageUrl(rs.getString("image_url"));
        return m;
    }
}
