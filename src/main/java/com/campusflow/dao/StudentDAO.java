package com.campusflow.dao;

import com.campusflow.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentDAO {
    List<Student> findAll();
    Optional<Student> findById(int id);
    Optional<Student> findByUserId(int userId);
    Optional<Student> findByStudentId(String studentId);
    int count();

    boolean save(Student student);
    boolean update(Student student);
    boolean delete(int id);

    List<Student> search(String keyword, String department, Integer semester);
    boolean isStudentIdTaken(String studentId, int excludeId);
    boolean isEmailTaken(String email, int excludeId);
    String generateNextStudentId();
    boolean hasEnrollments(int studentId);
    boolean hasAttendance(int studentId);
    List<String> getDistinctDepartments();
}
