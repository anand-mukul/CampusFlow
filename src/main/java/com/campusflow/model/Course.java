package com.campusflow.model;

public class Course {
    private int id;
    private String courseCode;
    private String courseName;
    private String description;
    private int credits;
    private String department;
    private int facultyId;
    private int maxStudents;
    private boolean active;

    public Course() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCode() { return courseCode; }
    public void setCode(String code) { this.courseCode = code; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getName() { return courseName; }
    public void setName(String name) { this.courseName = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }
    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

