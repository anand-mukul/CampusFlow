package com.campusflow.model.report;

public class CourseAttendanceStat {
    private int courseId;
    private String courseCode;
    private String courseName;
    private String department;
    private String facultyName;
    private int conductedSessions;
    private int enrolledStudents;
    private double averageAttendancePercentage;

    public CourseAttendanceStat() {}

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
    public int getConductedSessions() { return conductedSessions; }
    public void setConductedSessions(int conductedSessions) { this.conductedSessions = conductedSessions; }
    public int getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(int enrolledStudents) { this.enrolledStudents = enrolledStudents; }
    public double getAverageAttendancePercentage() { return averageAttendancePercentage; }
    public void setAverageAttendancePercentage(double averageAttendancePercentage) { this.averageAttendancePercentage = averageAttendancePercentage; }
}

