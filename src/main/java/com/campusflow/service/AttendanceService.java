package com.campusflow.service;

import com.campusflow.dao.AttendanceDAO;
import com.campusflow.dao.CourseDAO;
import com.campusflow.dao.EnrollmentDAO;
import com.campusflow.dao.FacultyDAO;
import com.campusflow.dao.StudentDAO;
import com.campusflow.dao.impl.AttendanceDAOImpl;
import com.campusflow.dao.impl.CourseDAOImpl;
import com.campusflow.dao.impl.EnrollmentDAOImpl;
import com.campusflow.dao.impl.FacultyDAOImpl;
import com.campusflow.dao.impl.StudentDAOImpl;
import com.campusflow.model.Attendance;
import com.campusflow.model.Course;
import com.campusflow.model.Faculty;
import com.campusflow.model.User;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttendanceService {
    private final AttendanceDAO attendanceDAO;
    private final CourseDAO courseDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final FacultyDAO facultyDAO;
    private final StudentDAO studentDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAOImpl();
        this.courseDAO = new CourseDAOImpl();
        this.enrollmentDAO = new EnrollmentDAOImpl();
        this.facultyDAO = new FacultyDAOImpl();
        this.studentDAO = new StudentDAOImpl();
    }

    public AttendanceService(AttendanceDAO attendanceDAO, CourseDAO courseDAO, EnrollmentDAO enrollmentDAO,
                             FacultyDAO facultyDAO, StudentDAO studentDAO) {
        this.attendanceDAO = attendanceDAO;
        this.courseDAO = courseDAO;
        this.enrollmentDAO = enrollmentDAO;
        this.facultyDAO = facultyDAO;
        this.studentDAO = studentDAO;
    }

    public List<Attendance> getAttendanceByCourseAndDate(int courseId, Date date) {
        return attendanceDAO.findByCourseAndDate(courseId, date);
    }

    public List<Attendance> getAttendanceByStudent(int studentId) {
        return attendanceDAO.findByStudentId(studentId);
    }

    public List<Attendance> getAttendanceByStudentAndCourse(int studentId, int courseId) {
        return attendanceDAO.findByStudentAndCourse(studentId, courseId);
    }

    public List<Date> getAttendanceDatesForCourse(int courseId) {
        return attendanceDAO.getDistinctDatesForCourse(courseId);
    }

    public double getAttendancePercentage(int studentId, int courseId) {
        return attendanceDAO.getAttendancePercentage(studentId, courseId);
    }

    public double getOverallAttendancePercentage(int studentId) {
        return attendanceDAO.getOverallAttendancePercentage(studentId);
    }

    public int getConductedSessionCount(int courseId) {
        return attendanceDAO.getConductedSessionCount(courseId);
    }

    /**
     * Saves attendance records for a course session with role-based authorization check.
     */
    public ServiceResult saveCourseAttendance(int courseId, Date date, List<Attendance> records, User currentUser) {
        if (courseId <= 0) return ServiceResult.fail("Invalid course selected.");
        if (date == null) return ServiceResult.fail("Attendance date is required.");
        if (records == null || records.isEmpty()) return ServiceResult.fail("No attendance records provided.");

        Optional<Course> cOpt = courseDAO.findById(courseId);
        if (cOpt.isEmpty()) return ServiceResult.fail("Course does not exist.");
        Course course = cOpt.get();

        // Enforce faculty authorization: Faculty can ONLY mark attendance for courses they are assigned to
        if (currentUser != null && currentUser.getRole() == User.Role.FACULTY) {
            Optional<Faculty> facOpt = facultyDAO.findByUserId(currentUser.getId());
            if (facOpt.isEmpty() || course.getFacultyId() != facOpt.get().getId()) {
                return ServiceResult.fail("Authorization Denied: You can only record attendance for your own assigned courses.");
            }
        }

        // Validate each record status
        for (Attendance a : records) {
            a.setCourseId(courseId);
            if (a.getAttendanceDate() == null) {
                a.setAttendanceDate(date);
            }
            if (currentUser != null) a.setMarkedBy(currentUser.getId());

            String status = a.getStatus();
            if (status == null || (!status.equals("PRESENT") && !status.equals("ABSENT") && !status.equals("LEAVE") && !status.equals("LATE"))) {
                return ServiceResult.fail("Invalid status '" + status + "'. Allowed: PRESENT, ABSENT, LEAVE.");
            }
        }

        boolean saved = attendanceDAO.saveBatch(records);
        if (saved) {
            return ServiceResult.ok("Attendance for " + records.size() + " student(s) saved successfully for " + date + ".");
        } else {
            return ServiceResult.fail("Failed to persist attendance records to database.");
        }
    }
}
