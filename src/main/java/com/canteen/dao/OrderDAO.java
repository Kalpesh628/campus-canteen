package com.canteen.dao;

import com.canteen.model.CartItem;
import com.canteen.model.Order;
import com.canteen.model.OrderItem;
import com.canteen.model.PickupSlot;
import com.canteen.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JDBC access for orders + order_items.
 *
 * RACE SAFETY (viva favourite): two students checking out for the last slot
 * at the same moment must not both succeed. placeOrder() runs in ONE database
 * transaction:
 *   1. SELECT the slot row ... FOR UPDATE  -> locks that row; the second
 *      concurrent transaction waits here until the first commits/rolls back.
 *   2. COUNT existing (non-rejected) orders for the slot inside the same txn.
 *   3. Only if count < max_orders, INSERT the order + its items, then COMMIT.
 * Because the capacity check and the insert are atomic, overselling is
 * impossible even under concurrency.
 */
public class OrderDAO {

    public int placeOrder(int userId, int slotId, Map<Integer, CartItem> cart, String note)
            throws SQLException {
        if (cart == null || cart.isEmpty()) {
            throw new SQLException("Cart is empty");
        }
        // Recompute the total server-side from the cart snapshot; never trust
        // a total submitted by the browser.
        BigDecimal total = cart.values().stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Connection c = DBUtil.getConnection();
        try {
            c.setAutoCommit(false); // ---- transaction begins ----

            // 1+2. Lock slot row and verify capacity atomically.
            try (PreparedStatement lock = c.prepareStatement(
                    "SELECT max_orders, active FROM pickup_slots WHERE id = ? FOR UPDATE")) {
                lock.setInt(1, slotId);
                try (ResultSet rs = lock.executeQuery()) {
                    if (!rs.next() || !rs.getBoolean("active")) {
                        c.rollback();
                        throw new SQLException("This pickup slot is no longer available.");
                    }
                    int max = rs.getInt("max_orders");
                    try (PreparedStatement cnt = c.prepareStatement(
                            "SELECT COUNT(*) FROM orders WHERE slot_id = ? AND status <> 'REJECTED'")) {
                        cnt.setInt(1, slotId);
                        try (ResultSet rs2 = cnt.executeQuery()) {
                            rs2.next();
                            if (rs2.getInt(1) >= max) {
                                c.rollback();
                                throw new SQLException("Sorry, this pickup slot just filled up.");
                            }
                        }
                    }
                }
            }

            // 3a. Insert the order header.
            int orderId;
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO orders (user_id, slot_id, status, total, payment_mode, note) "
                  + "VALUES (?, ?, 'PLACED', ?, 'PAY_AT_CANTEEN', ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ins.setInt(1, userId);
                ins.setInt(2, slotId);
                ins.setBigDecimal(3, total);
                ins.setString(4, note);
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    keys.next();
                    orderId = keys.getInt(1);
                }
            }

            // 3b. Insert the line items as a batch (one round-trip).
            try (PreparedStatement item = c.prepareStatement(
                    "INSERT INTO order_items (order_id, menu_item_id, qty, price_at_order) "
                  + "VALUES (?, ?, ?, ?)")) {
                for (CartItem ci : cart.values()) {
                    item.setInt(1, orderId);
                    item.setInt(2, ci.getMenuItemId());
                    item.setInt(3, ci.getQty());
                    item.setBigDecimal(4, ci.getPrice()); // price frozen at order time
                    item.addBatch();
                }
                item.executeBatch();
            }

            c.commit(); // ---- transaction ends ----
            return orderId;
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(true); // restore default for the pooled/closed connection
            c.close();
        }
    }

    /** Full order with items + slot, or null. */
    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT o.id, o.user_id, u.name AS user_name, o.slot_id, o.status, o.total, "
                   + "       o.payment_mode, o.note, o.reject_reason, o.created_at, "
                   + "       s.slot_date, s.start_time, s.end_time "
                   + "FROM orders o JOIN users u ON u.id = o.user_id "
                   + "JOIN pickup_slots s ON s.id = o.slot_id WHERE o.id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Order o = mapHeader(rs);
                o.setItems(findItems(c, orderId));
                return o;
            }
        }
    }

    /** Order headers for one student, newest first. */
    public List<Order> findByUser(int userId) throws SQLException {
        String sql = "SELECT o.id, o.user_id, u.name AS user_name, o.slot_id, o.status, o.total, "
                   + "       o.payment_mode, o.note, o.reject_reason, o.created_at, "
                   + "       s.slot_date, s.start_time, s.end_time "
                   + "FROM orders o JOIN users u ON u.id = o.user_id "
                   + "JOIN pickup_slots s ON s.id = o.slot_id "
                   + "WHERE o.user_id = ? ORDER BY o.id DESC";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> out = new ArrayList<>();
                while (rs.next()) out.add(mapHeader(rs));
                return out;
            }
        }
    }

    /** All orders for admin, optionally filtered by status, newest first. */
    public List<Order> findAll(String statusFilter) throws SQLException {
        String sql = "SELECT o.id, o.user_id, u.name AS user_name, o.slot_id, o.status, o.total, "
                   + "       o.payment_mode, o.note, o.reject_reason, o.created_at, "
                   + "       s.slot_date, s.start_time, s.end_time "
                   + "FROM orders o JOIN users u ON u.id = o.user_id "
                   + "JOIN pickup_slots s ON s.id = o.slot_id "
                   + (statusFilter != null && !statusFilter.isBlank() ? "WHERE o.status = ? " : "")
                   + "ORDER BY o.id DESC";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (statusFilter != null && !statusFilter.isBlank()) ps.setString(1, statusFilter);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> out = new ArrayList<>();
                while (rs.next()) out.add(mapHeader(rs));
                return out;
            }
        }
    }

    /**
     * Advances an order's status. The transition is validated here as a second
     * line of defence (the servlet validates first for friendly messages).
     */
    public void updateStatus(int orderId, String newStatus, String rejectReason) throws SQLException {
        Order current = findById(orderId);
        if (current == null) throw new SQLException("Order not found: " + orderId);
        if (!Order.allowedNext(current.getStatus()).contains(newStatus)) {
            throw new SQLException("Illegal status change: " + current.getStatus() + " -> " + newStatus);
        }
        String sql = "UPDATE orders SET status = ?, reject_reason = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            // reason only kept for REJECTED; cleared otherwise
            ps.setString(2, Order.REJECTED.equals(newStatus) ? rejectReason : null);
            ps.setInt(3, orderId);
            ps.executeUpdate();
        }
    }

    // ---------------- dashboard / stats ----------------

    public int countToday() throws SQLException {
        return scalarInt("SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE() "
                      + "AND status <> 'REJECTED'");
    }

    public BigDecimal revenueToday() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total),0) FROM orders "
                   + "WHERE DATE(created_at) = CURDATE() AND status <> 'REJECTED'";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    public int countPending() throws SQLException {
        return scalarInt("SELECT COUNT(*) FROM orders WHERE status IN ('PLACED','ACCEPTED','PREPARING')");
    }

    /** Last 7 days: each row is [day(String), count(int)]. */
    public List<String[]> ordersPerDay() throws SQLException {
        String sql = "SELECT DATE(created_at) AS d, COUNT(*) AS n FROM orders "
                   + "WHERE created_at >= CURDATE() - INTERVAL 6 DAY "
                   + "GROUP BY DATE(created_at) ORDER BY d";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String[]> out = new ArrayList<>();
            while (rs.next()) out.add(new String[]{rs.getString("d"), String.valueOf(rs.getInt("n"))});
            return out;
        }
    }

    /** Top 5 items by quantity sold (all time). Each row: [name, qty]. */
    public List<String[]> topItems() throws SQLException {
        String sql = "SELECT m.name, SUM(oi.qty) AS q FROM order_items oi "
                   + "JOIN menu_items m ON m.id = oi.menu_item_id "
                   + "GROUP BY m.name ORDER BY q DESC LIMIT 5";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String[]> out = new ArrayList<>();
            while (rs.next()) out.add(new String[]{rs.getString(1), String.valueOf(rs.getInt(2))});
            return out;
        }
    }

    // ---------------- helpers ----------------

    private int scalarInt(String sql) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private List<OrderItem> findItems(Connection c, int orderId) throws SQLException {
        String sql = "SELECT oi.id, oi.order_id, oi.menu_item_id, m.name AS item_name, "
                   + "       oi.qty, oi.price_at_order "
                   + "FROM order_items oi JOIN menu_items m ON m.id = oi.menu_item_id "
                   + "WHERE oi.order_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> out = new ArrayList<>();
                while (rs.next()) {
                    OrderItem i = new OrderItem();
                    i.setId(rs.getInt("id"));
                    i.setOrderId(rs.getInt("order_id"));
                    i.setMenuItemId(rs.getInt("menu_item_id"));
                    i.setItemName(rs.getString("item_name"));
                    i.setQty(rs.getInt("qty"));
                    i.setPriceAtOrder(rs.getBigDecimal("price_at_order"));
                    out.add(i);
                }
                return out;
            }
        }
    }

    private Order mapHeader(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setUserName(rs.getString("user_name"));
        o.setSlotId(rs.getInt("slot_id"));
        o.setStatus(rs.getString("status"));
        o.setTotal(rs.getBigDecimal("total"));
        o.setPaymentMode(rs.getString("payment_mode"));
        o.setNote(rs.getString("note"));
        o.setRejectReason(rs.getString("reject_reason"));
        if (rs.getTimestamp("created_at") != null) {
            o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        PickupSlot s = new PickupSlot();
        s.setId(o.getSlotId());
        s.setSlotDate(rs.getDate("slot_date").toLocalDate());
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        o.setSlot(s);
        return o;
    }
}
