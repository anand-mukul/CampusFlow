package com.campusflow.dao;

import com.campusflow.model.report.CourseAttendanceStat;
import com.campusflow.model.report.CourseEnrollmentStat;
import com.campusflow.model.report.DefaulterReportItem;

import java.util.List;

public interface ReportDAO {
    List<DefaulterReportItem> getDefaulters(double thresholdPercent);
    List<CourseEnrollmentStat> getCourseEnrollmentStats();
    List<CourseAttendanceStat> getCourseAttendanceStats();
}

