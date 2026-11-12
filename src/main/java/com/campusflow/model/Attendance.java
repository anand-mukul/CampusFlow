package com.campusflow.model;

import java.sql.Date;

public class Attendance {
    private int id;
    private int studentId;
    private int courseId;
    private Date attendanceDate;
    private String status;
    private int markedBy;

    public Attendance() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public Date getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(Date attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getMarkedBy() { return markedBy; }
    public void setMarkedBy(int markedBy) { this.markedBy = markedBy; }
}

