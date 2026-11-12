package com.campusflow.dao;

import com.campusflow.model.Attendance;

import java.sql.Date;
import java.util.List;

public interface AttendanceDAO {
    List<Attendance> findByStudentId(int studentId);
    List<Attendance> findByStudentAndCourse(int studentId, int courseId);
    List<Attendance> findByCourseId(int courseId);
    List<Attendance> findByCourseAndDate(int courseId, Date date);
    boolean saveOrUpdate(Attendance attendance);
    boolean saveBatch(List<Attendance> attendanceList);
    double getAttendancePercentage(int studentId, int courseId);
    double getOverallAttendancePercentage(int studentId);
    int getConductedSessionCount(int courseId);
    List<Date> getDistinctDatesForCourse(int courseId);
}
