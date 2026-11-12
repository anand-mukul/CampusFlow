package com.campusflow.model;

import java.sql.Date;

public class Student {
    private int id;
    private int userId;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private int semester;
    private Date enrollmentDate;
    private boolean active;

    public Student() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public Date getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(Date enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

