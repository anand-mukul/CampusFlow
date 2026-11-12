package com.campusflow.service;

import com.campusflow.dao.CourseDAO;
import com.campusflow.dao.FacultyDAO;
import com.campusflow.dao.ReportDAO;
import com.campusflow.dao.StudentDAO;
import com.campusflow.dao.impl.CourseDAOImpl;
import com.campusflow.dao.impl.FacultyDAOImpl;
import com.campusflow.dao.impl.ReportDAOImpl;
import com.campusflow.dao.impl.StudentDAOImpl;
import com.campusflow.model.report.CourseAttendanceStat;
import com.campusflow.model.report.CourseEnrollmentStat;
import com.campusflow.model.report.DefaulterReportItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private final ReportDAO reportDAO;
    private final StudentDAO studentDAO;
    private final FacultyDAO facultyDAO;
    private final CourseDAO courseDAO;

    public ReportService() {
        this.reportDAO = new ReportDAOImpl();
        this.studentDAO = new StudentDAOImpl();
        this.facultyDAO = new FacultyDAOImpl();
        this.courseDAO = new CourseDAOImpl();
    }

    public ReportService(ReportDAO reportDAO, StudentDAO studentDAO, FacultyDAO facultyDAO, CourseDAO courseDAO) {
        this.reportDAO = reportDAO;
        this.studentDAO = studentDAO;
        this.facultyDAO = facultyDAO;
        this.courseDAO = courseDAO;
    }

    public List<DefaulterReportItem> getDefaulterReport(double thresholdPercent) {
        double threshold = thresholdPercent > 0 ? thresholdPercent : 75.0;
        return reportDAO.getDefaulters(threshold);
    }

    public List<CourseEnrollmentStat> getCourseEnrollmentReport() {
        return reportDAO.getCourseEnrollmentStats();
    }

    public List<CourseAttendanceStat> getCourseAttendanceReport() {
        return reportDAO.getCourseAttendanceStats();
    }

    public Map<String, Integer> getSystemMetrics() {
        Map<String, Integer> metrics = new HashMap<>();
        metrics.put("totalStudents", studentDAO.count());
        metrics.put("totalFaculty", facultyDAO.count());
        metrics.put("totalCourses", courseDAO.count());
        return metrics;
    }
}

