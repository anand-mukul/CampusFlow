package com.campusflow.dao.impl;

import com.campusflow.config.DatabaseConfig;
import com.campusflow.dao.AttendanceDAO;
import com.campusflow.model.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAOImpl implements AttendanceDAO {

    @Override
    public List<Attendance> findByStudentId(int studentId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE student_id = ? ORDER BY attendance_date DESC, id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance by student: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Attendance> findByStudentAndCourse(int studentId, int courseId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE student_id = ? AND course_id = ? ORDER BY attendance_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance by student and course: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Attendance> findByCourseId(int courseId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE course_id = ? ORDER BY attendance_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance by course: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Attendance> findByCourseAndDate(int courseId, Date date) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE course_id = ? AND attendance_date = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setDate(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance by course and date: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean saveOrUpdate(Attendance attendance) {
        String sql = "INSERT INTO attendance (student_id, course_id, attendance_date, status, marked_by) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = VALUES(status), marked_by = VALUES(marked_by)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attendance.getStudentId());
            ps.setInt(2, attendance.getCourseId());
            ps.setDate(3, attendance.getAttendanceDate());
            ps.setString(4, attendance.getStatus());
            if (attendance.getMarkedBy() > 0) {
                ps.setInt(5, attendance.getMarkedBy());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving attendance record: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean saveBatch(List<Attendance> attendanceList) {
        if (attendanceList == null || attendanceList.isEmpty()) return true;

        String sql = "INSERT INTO attendance (student_id, course_id, attendance_date, status, marked_by) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = VALUES(status), marked_by = VALUES(marked_by)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Attendance a : attendanceList) {
                    ps.setInt(1, a.getStudentId());
                    ps.setInt(2, a.getCourseId());
                    ps.setDate(3, a.getAttendanceDate());
                    ps.setString(4, a.getStatus());
                    if (a.getMarkedBy() > 0) {
                        ps.setInt(5, a.getMarkedBy());
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Error executing batch attendance save: " + ex.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database error during batch attendance save: " + e.getMessage());
            return false;
        }
    }

    @Override
    public double getAttendancePercentage(int studentId, int courseId) {
        String sql = "SELECT " +
                "SUM(CASE WHEN status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) AS present_count, " +
                "SUM(CASE WHEN status = 'LEAVE' THEN 1 ELSE 0 END) AS leave_count, " +
                "COUNT(*) AS total_count " +
                "FROM attendance WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_count");
                    if (total == 0) return 100.0;
                    int present = rs.getInt("present_count");
                    int leave = rs.getInt("leave_count");
                    int eligible = total - leave;
                    if (eligible <= 0) return 100.0;
                    return Math.round(((present * 100.0) / eligible) * 10.0) / 10.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating course attendance %: " + e.getMessage());
        }
        return 100.0;
    }

    @Override
    public double getOverallAttendancePercentage(int studentId) {
        String sql = "SELECT " +
                "SUM(CASE WHEN status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) AS present_count, " +
                "SUM(CASE WHEN status = 'LEAVE' THEN 1 ELSE 0 END) AS leave_count, " +
                "COUNT(*) AS total_count " +
                "FROM attendance WHERE student_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_count");
                    if (total == 0) return 100.0;
                    int present = rs.getInt("present_count");
                    int leave = rs.getInt("leave_count");
                    int eligible = total - leave;
                    if (eligible <= 0) return 100.0;
                    return Math.round(((present * 100.0) / eligible) * 10.0) / 10.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating overall attendance %: " + e.getMessage());
        }
        return 100.0;
    }

    @Override
    public int getConductedSessionCount(int courseId) {
        String sql = "SELECT COUNT(DISTINCT attendance_date) FROM attendance WHERE course_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting conducted sessions: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<Date> getDistinctDatesForCourse(int courseId) {
        List<Date> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT attendance_date FROM attendance WHERE course_id = ? ORDER BY attendance_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dates.add(rs.getDate(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching distinct attendance dates: " + e.getMessage());
        }
        return dates;
    }

    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setCourseId(rs.getInt("course_id"));
        a.setAttendanceDate(rs.getDate("attendance_date"));
        a.setStatus(rs.getString("status"));
        a.setMarkedBy(rs.getInt("marked_by"));
        return a;
    }
}
