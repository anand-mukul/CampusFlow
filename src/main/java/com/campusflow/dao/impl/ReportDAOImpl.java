package com.campusflow.dao.impl;

import com.campusflow.config.DatabaseConfig;
import com.campusflow.dao.ReportDAO;
import com.campusflow.model.report.CourseAttendanceStat;
import com.campusflow.model.report.CourseEnrollmentStat;
import com.campusflow.model.report.DefaulterReportItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAOImpl implements ReportDAO {

    @Override
    public List<DefaulterReportItem> getDefaulters(double thresholdPercent) {
        List<DefaulterReportItem> list = new ArrayList<>();
        String sql = "SELECT " +
                "s.id AS student_id, " +
                "s.student_id AS student_code, " +
                "CONCAT(s.first_name, ' ', s.last_name) AS student_name, " +
                "s.department, " +
                "c.course_code, " +
                "c.course_name, " +
                "SUM(CASE WHEN a.status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) AS present_count, " +
                "SUM(CASE WHEN a.status = 'LEAVE' THEN 1 ELSE 0 END) AS leave_count, " +
                "COUNT(a.id) AS total_count " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.id " +
                "JOIN courses c ON a.course_id = c.id " +
                "GROUP BY s.id, s.student_id, s.first_name, s.last_name, s.department, c.course_code, c.course_name " +
                "HAVING (COUNT(a.id) - SUM(CASE WHEN a.status = 'LEAVE' THEN 1 ELSE 0 END)) > 0 " +
                "   AND ((SUM(CASE WHEN a.status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) * 100.0) / " +
                "        (COUNT(a.id) - SUM(CASE WHEN a.status = 'LEAVE' THEN 1 ELSE 0 END))) < ? " +
                "ORDER BY student_name, c.course_code";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, thresholdPercent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DefaulterReportItem item = new DefaulterReportItem();
                    item.setStudentId(rs.getInt("student_id"));
                    item.setStudentCode(rs.getString("student_code"));
                    item.setStudentName(rs.getString("student_name"));
                    item.setDepartment(rs.getString("department"));
                    item.setCourseCode(rs.getString("course_code"));
                    item.setCourseName(rs.getString("course_name"));

                    int present = rs.getInt("present_count");
                    int leave = rs.getInt("leave_count");
                    int total = rs.getInt("total_count");
                    int eligible = total - leave;

                    item.setPresentCount(present);
                    item.setLeaveCount(leave);
                    item.setTotalCount(total);

                    double pct = eligible > 0 ? (present * 100.0) / eligible : 100.0;
                    item.setAttendancePercentage(Math.round(pct * 10.0) / 10.0);
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating defaulter report: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<CourseEnrollmentStat> getCourseEnrollmentStats() {
        List<CourseEnrollmentStat> list = new ArrayList<>();
        String sql = "SELECT " +
                "c.id AS course_id, " +
                "c.course_code, " +
                "c.course_name, " +
                "c.department, " +
                "COALESCE(CONCAT(f.first_name, ' ', f.last_name), 'Unassigned') AS faculty_name, " +
                "c.max_students, " +
                "COUNT(e.id) AS enrolled_count " +
                "FROM courses c " +
                "LEFT JOIN faculty f ON c.faculty_id = f.id " +
                "LEFT JOIN enrollments e ON c.id = e.course_id AND e.status = 'ACTIVE' " +
                "GROUP BY c.id, c.course_code, c.course_name, c.department, f.first_name, f.last_name, c.max_students " +
                "ORDER BY c.course_code";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CourseEnrollmentStat stat = new CourseEnrollmentStat();
                stat.setCourseId(rs.getInt("course_id"));
                stat.setCourseCode(rs.getString("course_code"));
                stat.setCourseName(rs.getString("course_name"));
                stat.setDepartment(rs.getString("department"));
                stat.setFacultyName(rs.getString("faculty_name"));
                int max = rs.getInt("max_students");
                int enrolled = rs.getInt("enrolled_count");
                stat.setMaxStudents(max);
                stat.setEnrolledCount(enrolled);
                double pct = max > 0 ? (enrolled * 100.0) / max : 0.0;
                stat.setOccupancyPercentage(Math.round(pct * 10.0) / 10.0);
                list.add(stat);
            }
        } catch (SQLException e) {
            System.err.println("Error generating course enrollment stats: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<CourseAttendanceStat> getCourseAttendanceStats() {
        List<CourseAttendanceStat> list = new ArrayList<>();
        String sql = "SELECT " +
                "c.id AS course_id, " +
                "c.course_code, " +
                "c.course_name, " +
                "c.department, " +
                "COALESCE(CONCAT(f.first_name, ' ', f.last_name), 'Unassigned') AS faculty_name, " +
                "COUNT(DISTINCT a.attendance_date) AS conducted_sessions, " +
                "(SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.id AND e.status = 'ACTIVE') AS enrolled_students, " +
                "SUM(CASE WHEN a.status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) AS present_count, " +
                "SUM(CASE WHEN a.status = 'LEAVE' THEN 1 ELSE 0 END) AS leave_count, " +
                "COUNT(a.id) AS total_attendance_rows " +
                "FROM courses c " +
                "LEFT JOIN faculty f ON c.faculty_id = f.id " +
                "LEFT JOIN attendance a ON c.id = a.course_id " +
                "GROUP BY c.id, c.course_code, c.course_name, c.department, f.first_name, f.last_name " +
                "ORDER BY c.course_code";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CourseAttendanceStat stat = new CourseAttendanceStat();
                stat.setCourseId(rs.getInt("course_id"));
                stat.setCourseCode(rs.getString("course_code"));
                stat.setCourseName(rs.getString("course_name"));
                stat.setDepartment(rs.getString("department"));
                stat.setFacultyName(rs.getString("faculty_name"));
                stat.setConductedSessions(rs.getInt("conducted_sessions"));
                stat.setEnrolledStudents(rs.getInt("enrolled_students"));

                int present = rs.getInt("present_count");
                int leave = rs.getInt("leave_count");
                int totalRows = rs.getInt("total_attendance_rows");
                int eligible = totalRows - leave;

                double avgPct = eligible > 0 ? (present * 100.0) / eligible : 100.0;
                stat.setAverageAttendancePercentage(Math.round(avgPct * 10.0) / 10.0);
                list.add(stat);
            }
        } catch (SQLException e) {
            System.err.println("Error generating course attendance stats: " + e.getMessage());
        }
        return list;
    }
}

