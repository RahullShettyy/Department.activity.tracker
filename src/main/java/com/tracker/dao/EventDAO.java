package com.tracker.dao;

import com.tracker.config.DatabaseConfig;
import com.tracker.model.Event;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events";
        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                events.add(mapRowToEvent(rs));
            }
        }
        return events;
    }

    public void addEvent(Event event) throws SQLException {
        String sql = "INSERT INTO events (title, description, event_date, location, department_id, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, event.getTitle());
            pstmt.setString(2, event.getDescription());
            pstmt.setTimestamp(3, new Timestamp(event.getEventDate().getTime()));
            pstmt.setString(4, event.getLocation());
            pstmt.setInt(5, event.getDepartmentId());
            pstmt.setString(6, event.getStatus());
            pstmt.executeUpdate();
        }
    }

    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getInt("id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));
        event.setEventDate(rs.getTimestamp("event_date"));
        event.setLocation(rs.getString("location"));
        event.setDepartmentId(rs.getInt("department_id"));
        event.setStatus(rs.getString("status"));
        return event;
    }
}
