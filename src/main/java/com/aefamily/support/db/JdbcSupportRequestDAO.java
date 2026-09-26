package com.aefamily.support.db;

import javax.sql.DataSource;

import com.aefamily.support.Priority;
import com.aefamily.support.Status;
import com.aefamily.support.SupportRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcSupportRequestDAO implements SupportRequestDAO {
    private final DataSource ds;

    public JdbcSupportRequestDAO(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public int create(SupportRequest req) throws SQLException {
        String sql = "INSERT INTO support_requests " +
                "(player,message,time,status,category,priority,assigned_staff,last_updated) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            mapStatement(req, ps);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    @Override
    public SupportRequest findById(int id) throws SQLException {
        String sql = "SELECT * FROM support_requests WHERE id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<SupportRequest> findAll() throws SQLException {
        List<SupportRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM support_requests ORDER BY id DESC";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<SupportRequest> findByPlayer(String player) throws SQLException {
        List<SupportRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM support_requests WHERE player = ? ORDER BY id DESC";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean update(SupportRequest req) throws SQLException {
        String sql = "UPDATE support_requests SET status=?, category=?, priority=?, assigned_staff=?, last_updated=? WHERE id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, req.getStatus().name());
            ps.setString(2, req.getCategory());
            ps.setString(3, req.getPriority().name());
            if (req.getAssignedStaff() == null || req.getAssignedStaff().trim().isEmpty()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, req.getAssignedStaff());
            }
            ps.setString(5, req.getLastUpdated());
            ps.setInt(6, req.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE support_requests SET status = ? WHERE id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM support_requests WHERE id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private SupportRequest mapRow(ResultSet rs) throws SQLException {
        String assigned = rs.getString("assigned_staff");
        String category = rs.getString("category");
        String priorityValue = rs.getString("priority");
        String lastUpdated = rs.getString("last_updated");
        Priority priority = Priority.NORMAL;
        if (priorityValue != null) {
            try {
                priority = Priority.valueOf(priorityValue);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return new SupportRequest(
                rs.getInt("id"),
                rs.getString("player"),
                rs.getString("message"),
                rs.getString("time"),
                Status.valueOf(rs.getString("status")),
                category == null ? "GENERAL" : category,
                priority,
                assigned,
                lastUpdated == null ? rs.getString("time") : lastUpdated
        );
    }

    private void mapStatement(SupportRequest req, PreparedStatement ps) throws SQLException {
        ps.setString(1, req.getPlayer());
        ps.setString(2, req.getMessage());
        ps.setString(3, req.getTime());
        ps.setString(4, req.getStatus().name());
        ps.setString(5, req.getCategory());
        ps.setString(6, req.getPriority().name());
        if (req.getAssignedStaff() == null || req.getAssignedStaff().trim().isEmpty()) {
            ps.setNull(7, Types.VARCHAR);
        } else {
            ps.setString(7, req.getAssignedStaff());
        }
        ps.setString(8, req.getLastUpdated());
    }
}
