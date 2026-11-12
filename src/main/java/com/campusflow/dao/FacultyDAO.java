package com.campusflow.dao;

import com.campusflow.model.Faculty;
import java.util.List;
import java.util.Optional;

public interface FacultyDAO {
    List<Faculty> findAll();
    Optional<Faculty> findById(int id);
    Optional<Faculty> findByUserId(int userId);
    Optional<Faculty> findByFacultyId(String facultyId);
    int count();

    boolean save(Faculty faculty);
    boolean update(Faculty faculty);
    boolean delete(int id);

    List<Faculty> search(String keyword, String department);
    boolean isFacultyIdTaken(String facultyId, int excludeId);
    boolean isEmailTaken(String email, int excludeId);
    String generateNextFacultyId();
    boolean hasAssignedCourses(int facultyId);
    List<String> getDistinctDepartments();
}
