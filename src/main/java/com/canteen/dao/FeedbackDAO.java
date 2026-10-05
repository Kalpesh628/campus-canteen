package com.canteen.dao;

import com.canteen.model.Feedback;
import com.canteen.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** JDBC access for the feedback table. */
public class FeedbackDAO {

    /**
     * Saves feedback. The order must belong to the user, be PICKED_UP,
     * and have no feedback yet - enforced here so a crafted POST can't cheat.
     */
    public void add(int orderId, int userId, int rating, String comment) throws SQLException {
        String check = "SELECT status FROM orders WHERE id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(check)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Order not found.");
                if (!"PICKED_UP".equals(rs.getString("status"))) {
                    throw new SQLException("Feedback is allowed only for completed orders.");
                }
            }
        }
        String sql = "INSERT INTO feedback (order_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.executeUpdate(); // UNIQUE(order_id) rejects a second feedback for the same order
        }
    }

    public boolean existsForOrder(int orderId) throws SQLException {
        String sql = "SELECT 1 FROM feedback WHERE order_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** All feedback with student names, newest first, for the admin page. */
    public List<Feedback> findAll() throws SQLException {
        String sql = "SELECT f.id, f.order_id, f.user_id, u.name AS user_name, "
                   + "       f.rating, f.comment, f.created_at "
                   + "FROM feedback f JOIN users u ON u.id = f.user_id ORDER BY f.id DESC";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Feedback> out = new ArrayList<>();
            while (rs.next()) {
                Feedback f = new Feedback();
                f.setId(rs.getInt("id"));
                f.setOrderId(rs.getInt("order_id"));
                f.setUserId(rs.getInt("user_id"));
                f.setUserName(rs.getString("user_name"));
                f.setRating(rs.getInt("rating"));
                f.setComment(rs.getString("comment"));
                if (rs.getTimestamp("created_at") != null) {
                    f.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
                out.add(f);
            }
            return out;
        }
    }
}
