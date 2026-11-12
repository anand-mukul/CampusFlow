package com.campusflow.dao;

import com.campusflow.model.Enrollment;
import java.util.List;

public interface EnrollmentDAO {
    List<Enrollment> findByStudentId(int studentId);
    int countByStudentId(int studentId);
    List<Enrollment> findByCourseId(int courseId);
    int countByCourseId(int courseId);
    List<Enrollment> findAll();
    boolean isAlreadyEnrolled(int studentId, int courseId);
    boolean save(Enrollment enrollment);
    boolean updateStatus(int studentId, int courseId, String status, String grade);
    boolean delete(int studentId, int courseId);
}
