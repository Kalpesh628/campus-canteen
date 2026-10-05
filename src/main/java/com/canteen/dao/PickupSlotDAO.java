package com.canteen.dao;

import com.canteen.model.PickupSlot;
import com.canteen.util.DBUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** JDBC access for the pickup_slots table. */
public class PickupSlotDAO {

    /**
     * Active slots for one date with live remaining capacity.
     * REJECTED orders never consumed a pickup, so they don't count.
     */
    public List<PickupSlot> findByDateWithRemaining(LocalDate date) throws SQLException {
        String sql = "SELECT s.id, s.slot_date, s.start_time, s.end_time, s.max_orders, s.active, "
                   + "       (s.max_orders - COUNT(o.id)) AS remaining "
                   + "FROM pickup_slots s "
                   + "LEFT JOIN orders o ON o.slot_id = s.id AND o.status <> 'REJECTED' "
                   + "WHERE s.slot_date = ? AND s.active = TRUE "
                   + "GROUP BY s.id ORDER BY s.start_time";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                List<PickupSlot> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    /** All slots (active or not) for the admin page, newest first. */
    public List<PickupSlot> findAll() throws SQLException {
        String sql = "SELECT s.id, s.slot_date, s.start_time, s.end_time, s.max_orders, s.active, "
                   + "       (s.max_orders - COUNT(o.id)) AS remaining "
                   + "FROM pickup_slots s "
                   + "LEFT JOIN orders o ON o.slot_id = s.id AND o.status <> 'REJECTED' "
                   + "GROUP BY s.id ORDER BY s.slot_date DESC, s.start_time";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PickupSlot> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public PickupSlot findById(int id) throws SQLException {
        String sql = "SELECT id, slot_date, start_time, end_time, max_orders, active, max_orders AS remaining "
                   + "FROM pickup_slots WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int insert(PickupSlot s) throws SQLException {
        String sql = "INSERT INTO pickup_slots (slot_date, start_time, end_time, max_orders, active) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(s.getSlotDate()));
            ps.setTime(2, Time.valueOf(s.getStartTime()));
            ps.setTime(3, Time.valueOf(s.getEndTime()));
            ps.setInt(4, s.getMaxOrders());
            ps.setBoolean(5, s.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void setActive(int id, boolean active) throws SQLException {
        String sql = "UPDATE pickup_slots SET active = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM pickup_slots WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private PickupSlot map(ResultSet rs) throws SQLException {
        PickupSlot s = new PickupSlot();
        s.setId(rs.getInt("id"));
        s.setSlotDate(rs.getDate("slot_date").toLocalDate());
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        s.setMaxOrders(rs.getInt("max_orders"));
        s.setActive(rs.getBoolean("active"));
        s.setRemaining(rs.getInt("remaining"));
        return s;
    }
}
