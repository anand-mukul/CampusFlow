package com.campusflow.model.report;

public class CourseEnrollmentStat {
    private int courseId;
    private String courseCode;
    private String courseName;
    private String department;
    private String facultyName;
    private int maxStudents;
    private int enrolledCount;
    private double occupancyPercentage;

    public CourseEnrollmentStat() {}

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
    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }
    public int getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }
    public double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }
}

