package com.campusflow.dao;

import com.campusflow.model.Course;

import java.util.List;
import java.util.Optional;

public interface CourseDAO {
    List<Course> findAll();
    Optional<Course> findById(int id);
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByFacultyId(int facultyId);
    List<Course> search(String keyword, String department);
    boolean isCourseCodeTaken(String courseCode, int excludeId);
    boolean save(Course course);
    boolean update(Course course);
    boolean delete(int id);
    boolean hasEnrollments(int courseId);
    int getEnrolledCount(int courseId);
    List<String> getDistinctDepartments();
    int count();
}
