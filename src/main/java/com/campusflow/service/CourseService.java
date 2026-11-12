package com.campusflow.service;

import com.campusflow.dao.CourseDAO;
import com.campusflow.dao.FacultyDAO;
import com.campusflow.dao.impl.CourseDAOImpl;
import com.campusflow.dao.impl.FacultyDAOImpl;
import com.campusflow.model.Course;
import com.campusflow.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class CourseService {
    private final CourseDAO courseDAO;
    private final FacultyDAO facultyDAO;

    public CourseService() {
        this.courseDAO = new CourseDAOImpl();
        this.facultyDAO = new FacultyDAOImpl();
    }

    public CourseService(CourseDAO courseDAO, FacultyDAO facultyDAO) {
        this.courseDAO = courseDAO;
        this.facultyDAO = facultyDAO;
    }

    public List<Course> getAllCourses() {
        return courseDAO.findAll();
    }

    public int getCourseCount() {
        return courseDAO.count();
    }

    public Optional<Course> getCourseById(int id) {
        return courseDAO.findById(id);
    }

    public Optional<Course> getCourseByCode(String code) {
        return courseDAO.findByCourseCode(code);
    }

    public List<Course> getCoursesByFacultyId(int facultyId) {
        return courseDAO.findByFacultyId(facultyId);
    }

    public List<Course> searchCourses(String keyword, String department) {
        return courseDAO.search(keyword, department);
    }

    public List<String> getDepartments() {
        return courseDAO.getDistinctDepartments();
    }

    public int getEnrolledCount(int courseId) {
        return courseDAO.getEnrolledCount(courseId);
    }

    public ServiceResult createCourse(Course course) {
        if (course == null) return ServiceResult.fail("Course information cannot be null.");
        if (ValidationUtil.isNullOrEmpty(course.getCourseCode())) return ServiceResult.fail("Course code is required.");
        if (ValidationUtil.isNullOrEmpty(course.getCourseName())) return ServiceResult.fail("Course name is required.");
        if (course.getCredits() <= 0) return ServiceResult.fail("Course credits must be at least 1.");
        if (course.getMaxStudents() <= 0) return ServiceResult.fail("Maximum students must be greater than 0.");

        if (courseDAO.isCourseCodeTaken(course.getCourseCode().trim().toUpperCase(), 0)) {
            return ServiceResult.fail("Course code '" + course.getCourseCode() + "' is already registered.");
        }

        course.setCourseCode(course.getCourseCode().trim().toUpperCase());
        course.setCourseName(course.getCourseName().trim());

        // Validate assigned faculty if set
        if (course.getFacultyId() > 0 && facultyDAO.findById(course.getFacultyId()).isEmpty()) {
            return ServiceResult.fail("Assigned faculty member does not exist.");
        }

        boolean saved = courseDAO.save(course);
        if (saved) {
            return ServiceResult.ok("Course '" + course.getCourseName() + "' created successfully.", course);
        } else {
            return ServiceResult.fail("Failed to save course to database.");
        }
    }

    public ServiceResult updateCourse(Course course) {
        if (course == null || course.getId() <= 0) return ServiceResult.fail("Invalid course record.");
        if (ValidationUtil.isNullOrEmpty(course.getCourseCode())) return ServiceResult.fail("Course code is required.");
        if (ValidationUtil.isNullOrEmpty(course.getCourseName())) return ServiceResult.fail("Course name is required.");
        if (course.getCredits() <= 0) return ServiceResult.fail("Course credits must be at least 1.");

        if (courseDAO.isCourseCodeTaken(course.getCourseCode().trim().toUpperCase(), course.getId())) {
            return ServiceResult.fail("Course code '" + course.getCourseCode() + "' is already taken by another course.");
        }

        course.setCourseCode(course.getCourseCode().trim().toUpperCase());
        course.setCourseName(course.getCourseName().trim());

        if (course.getFacultyId() > 0 && facultyDAO.findById(course.getFacultyId()).isEmpty()) {
            return ServiceResult.fail("Assigned faculty member does not exist.");
        }

        boolean updated = courseDAO.update(course);
        if (updated) {
            return ServiceResult.ok("Course updated successfully.", course);
        } else {
            return ServiceResult.fail("Failed to update course in database.");
        }
    }

    public ServiceResult deleteCourse(int courseId) {
        if (courseDAO.hasEnrollments(courseId)) {
            return ServiceResult.fail("Cannot delete course: Students are currently enrolled in this course.");
        }
        boolean deleted = courseDAO.delete(courseId);
        if (deleted) {
            return ServiceResult.ok("Course deleted successfully.");
        } else {
            return ServiceResult.fail("Failed to delete course.");
        }
    }

    public ServiceResult assignFaculty(int courseId, int facultyId) {
        Optional<Course> cOpt = courseDAO.findById(courseId);
        if (cOpt.isEmpty()) return ServiceResult.fail("Course not found.");
        Course course = cOpt.get();

        if (facultyId > 0 && facultyDAO.findById(facultyId).isEmpty()) {
            return ServiceResult.fail("Faculty member not found.");
        }

        course.setFacultyId(facultyId);
        boolean updated = courseDAO.update(course);
        if (updated) {
            return ServiceResult.ok("Faculty assigned successfully.");
        } else {
            return ServiceResult.fail("Failed to assign faculty to course.");
        }
    }
}
