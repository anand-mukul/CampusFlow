package com.campusflow.service;

import com.campusflow.dao.CourseDAO;
import com.campusflow.dao.EnrollmentDAO;
import com.campusflow.dao.FacultyDAO;
import com.campusflow.dao.StudentDAO;
import com.campusflow.dao.impl.CourseDAOImpl;
import com.campusflow.dao.impl.EnrollmentDAOImpl;
import com.campusflow.dao.impl.FacultyDAOImpl;
import com.campusflow.dao.impl.StudentDAOImpl;
import com.campusflow.model.Course;
import com.campusflow.model.Enrollment;
import com.campusflow.model.Faculty;
import com.campusflow.model.Student;
import com.campusflow.model.User;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public class EnrollmentService {
    private final EnrollmentDAO enrollmentDAO;
    private final CourseDAO courseDAO;
    private final StudentDAO studentDAO;
    private final FacultyDAO facultyDAO;

    public EnrollmentService() {
        this.enrollmentDAO = new EnrollmentDAOImpl();
        this.courseDAO = new CourseDAOImpl();
        this.studentDAO = new StudentDAOImpl();
        this.facultyDAO = new FacultyDAOImpl();
    }

    public EnrollmentService(EnrollmentDAO enrollmentDAO, CourseDAO courseDAO, StudentDAO studentDAO) {
        this.enrollmentDAO = enrollmentDAO;
        this.courseDAO = courseDAO;
        this.studentDAO = studentDAO;
        this.facultyDAO = new FacultyDAOImpl();
    }

    public List<Enrollment> getEnrollmentsByCourse(int courseId) {
        return enrollmentDAO.findByCourseId(courseId);
    }

    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        return enrollmentDAO.findByStudentId(studentId);
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentDAO.findAll();
    }

    public ServiceResult enrollStudent(int studentId, int courseId) {
        return enrollStudent(studentId, courseId, null);
    }

    public ServiceResult enrollStudent(int studentId, int courseId, User currentUser) {
        // Validate student
        Optional<Student> sOpt = studentDAO.findById(studentId);
        if (sOpt.isEmpty()) {
            return ServiceResult.fail("Student does not exist.");
        }
        Student student = sOpt.get();
        if (!student.isActive()) {
            return ServiceResult.fail("Cannot enroll inactive student '" + student.getFullName() + "'.");
        }

        // Validate course
        Optional<Course> cOpt = courseDAO.findById(courseId);
        if (cOpt.isEmpty()) {
            return ServiceResult.fail("Course does not exist.");
        }
        Course course = cOpt.get();
        if (!course.isActive()) {
            return ServiceResult.fail("Course '" + course.getCourseName() + "' is currently archived/inactive.");
        }

        // Faculty role authorization check
        if (currentUser != null && currentUser.getRole() == User.Role.FACULTY) {
            Optional<Faculty> facOpt = facultyDAO.findByUserId(currentUser.getId());
            if (facOpt.isEmpty() || course.getFacultyId() != facOpt.get().getId()) {
                return ServiceResult.fail("Authorization Denied: You cannot manage enrollments for another instructor's course.");
            }
        }

        // Prevent duplicate enrollment
        if (enrollmentDAO.isAlreadyEnrolled(studentId, courseId)) {
            return ServiceResult.fail("Student '" + student.getFullName() + "' is already enrolled in " + course.getCourseCode() + ".");
        }

        // Capacity check
        int currentEnrolled = enrollmentDAO.countByCourseId(courseId);
        if (currentEnrolled >= course.getMaxStudents()) {
            return ServiceResult.fail("Course capacity reached (" + course.getMaxStudents() + " students max). Cannot enroll additional students.");
        }

        Enrollment e = new Enrollment();
        e.setStudentId(studentId);
        e.setCourseId(courseId);
        e.setEnrollmentDate(new Date(System.currentTimeMillis()));
        e.setStatus("ACTIVE");

        boolean saved = enrollmentDAO.save(e);
        if (saved) {
            return ServiceResult.ok("Enrolled " + student.getFullName() + " into " + course.getCourseCode() + " successfully.", e);
        } else {
            return ServiceResult.fail("Failed to save enrollment.");
        }
    }

    public ServiceResult dropStudent(int studentId, int courseId) {
        boolean deleted = enrollmentDAO.delete(studentId, courseId);
        if (deleted) {
            return ServiceResult.ok("Student enrollment dropped successfully.");
        } else {
            return ServiceResult.fail("Failed to drop student enrollment.");
        }
    }

    public ServiceResult updateStatusAndGrade(int studentId, int courseId, String status, String grade) {
        boolean updated = enrollmentDAO.updateStatus(studentId, courseId, status, grade);
        if (updated) {
            return ServiceResult.ok("Enrollment status updated to " + status + ".");
        } else {
            return ServiceResult.fail("Failed to update enrollment status.");
        }
    }
}

