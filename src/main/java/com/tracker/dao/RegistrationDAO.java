package com.tracker.dao;

import com.tracker.config.DatabaseConfig;
import com.tracker.model.Registration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {

    public void registerStudent(Registration reg) throws SQLException {
        // Logic: 1. Ensure student exists (or create), 2. Create registration
        // For simplicity, we will just insert into students if not exists (using email)

        int studentId = getOrCreateStudent(reg.getStudentName(), reg.getStudentEmail(), reg.getDepartment());

        String sql = "INSERT INTO registrations (event_id, student_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reg.getEventId());
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        }
    }

    private int getOrCreateStudent(String name, String email, String dept) throws SQLException {
        // Check if student exists
        String checkSql = "SELECT id FROM students WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Create student
        String insertSql = "INSERT INTO students (student_id, full_name, email, department_id) VALUES (?, ?, ?, ?)";
        // We need a student_id, generate one or use email? Schema says student_id is
        // varchar.
        // Let's use email as student_id for simplicity or a random string.
        String newStudentId = "S" + System.currentTimeMillis();

        // We also need department_id. Hardcode to 1 (Computer Science) for now or look
        // up.
        int deptId = 1;

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, newStudentId);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setInt(4, deptId);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create student");
    }

    public List<Registration> getRegistrationsForEvent(int eventId) throws SQLException {
        List<Registration> regs = new ArrayList<>();
        // Join with students table to get names
        String sql = "SELECT r.*, s.full_name, s.email, s.department_id FROM registrations r " +
                "JOIN students s ON r.student_id = s.id " +
                "WHERE r.event_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Registration reg = new Registration();
                    reg.setId(rs.getInt("id"));
                    reg.setEventId(rs.getInt("event_id"));
                    reg.setStudentId(rs.getInt("student_id"));
                    reg.setAttended(rs.getBoolean("attended"));
                    reg.setStudentName(rs.getString("full_name"));
                    reg.setStudentEmail(rs.getString("email"));
                    // Mapping dept id to string just for display
                    reg.setDepartment("Dept " + rs.getInt("department_id"));
                    regs.add(reg);
                }
            }
        }
        return regs;
    }

    public void updateAttendance(int registrationId, boolean attended) throws SQLException {
        String sql = "UPDATE registrations SET attended = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, attended);
            pstmt.setInt(2, registrationId);
            pstmt.executeUpdate();
        }
    }

    public int getTotalAttendance() throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE attended = true";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
