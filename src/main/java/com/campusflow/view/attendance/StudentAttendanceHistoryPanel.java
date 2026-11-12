package com.campusflow.view.attendance;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Attendance;
import com.campusflow.model.Course;
import com.campusflow.model.Student;
import com.campusflow.service.AttendanceService;
import com.campusflow.service.CourseService;
import com.campusflow.service.StudentService;
import com.campusflow.view.components.ModernButton;
import com.campusflow.view.components.RoundedPanel;
import com.campusflow.view.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StudentAttendanceHistoryPanel extends JPanel {
    private final Student student;
    private final AttendanceService attendanceService;
    private final CourseService courseService;
    private final StudentService studentService;

    private JComboBox<CourseItem> courseFilterCombo;
    private StatCard totalSessionsCard;
    private StatCard attendedCard;
    private StatCard leavesCard;
    private StatCard percentageCard;

    private JTable table;
    private DefaultTableModel tableModel;
    private final Map<Integer, Course> courseMap = new HashMap<>();

    public static class CourseItem {
        public final int id;
        public final String name;
        public CourseItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    public StudentAttendanceHistoryPanel(Student student) {
        this.student = student;
        this.attendanceService = new AttendanceService();
        this.courseService = new CourseService();
        this.studentService = new StudentService();

        setLayout(new BorderLayout(0, 16));
        setBackground(AppConfig.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        loadHistory();
    }

    private void initUI() {
        // TOP CONTROL AND METRICS
        JPanel topContainer = new JPanel(new BorderLayout(0, 14));
        topContainer.setOpaque(false);

        // Header and Course Filter
        RoundedPanel controlCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        controlCard.setLayout(new BorderLayout(14, 0));
        controlCard.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Attendance Record — " + (student != null ? student.getFullName() : "Student"));
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(AppConfig.TEXT_PRIMARY);
        titlePanel.add(title);
        controlCard.add(titlePanel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Filter Course:"));

        courseFilterCombo = new JComboBox<>();
        courseFilterCombo.setFont(AppConfig.FONT_MEDIUM);
        courseFilterCombo.setBackground(Color.WHITE);
        courseFilterCombo.setPreferredSize(new Dimension(240, 34));
        courseFilterCombo.addItem(new CourseItem(0, "All Enrolled Courses"));
        courseFilterCombo.addActionListener(e -> applyFilter());
        filterPanel.add(courseFilterCombo);

        ModernButton refreshBtn = ModernButton.outline("↻", AppConfig.PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadHistory());
        filterPanel.add(refreshBtn);

        controlCard.add(filterPanel, BorderLayout.EAST);
        topContainer.add(controlCard, BorderLayout.NORTH);

        // Metric Cards Grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        metricsGrid.setOpaque(false);

        totalSessionsCard = new StatCard("Conducted Sessions", "0", "Total academic classes", AppConfig.PRIMARY_COLOR, "📅");
        attendedCard = new StatCard("Present / Attended", "0", "Present in class", AppConfig.SUCCESS_COLOR, "✓");
        leavesCard = new StatCard("Excused Leaves", "0", "Excluded from deficit", AppConfig.WARNING_COLOR, "ℹ");
        percentageCard = new StatCard("Attendance Rate", "0.0%", "Formula: P / (Total - L)", AppConfig.ACCENT_COLOR, "📊");

        metricsGrid.add(totalSessionsCard);
        metricsGrid.add(attendedCard);
        metricsGrid.add(leavesCard);
        metricsGrid.add(percentageCard);
        topContainer.add(metricsGrid, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // TABLE PANEL
        RoundedPanel tableCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(12, 16, 16, 16));

        String[] cols = {"Date", "Course Code", "Course Name", "Department", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFont(AppConfig.FONT_MEDIUM);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(0xF8FAFC));
        table.getTableHeader().setForeground(AppConfig.TEXT_SECONDARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadHistory() {
        if (student == null) return;

        // Cache courses
        List<Course> allCourses = courseService.getAllCourses();
        courseMap.clear();
        courseFilterCombo.removeAllItems();
        courseFilterCombo.addItem(new CourseItem(0, "All Enrolled Courses"));

        for (Course c : allCourses) {
            courseMap.put(c.getId(), c);
            courseFilterCombo.addItem(new CourseItem(c.getId(), c.getCourseCode() + " - " + c.getCourseName()));
        }

        applyFilter();
    }

    private void applyFilter() {
        if (student == null) return;

        CourseItem selectedCourse = (CourseItem) courseFilterCombo.getSelectedItem();
        int courseId = selectedCourse != null ? selectedCourse.id : 0;

        List<Attendance> records;
        if (courseId > 0) {
            records = attendanceService.getAttendanceByStudentAndCourse(student.getId(), courseId);
        } else {
            records = attendanceService.getAttendanceByStudent(student.getId());
        }

        tableModel.setRowCount(0);
        int total = records.size();
        int present = 0;
        int leaves = 0;

        for (Attendance a : records) {
            Course c = courseMap.get(a.getCourseId());
            String cCode = c != null ? c.getCourseCode() : "CRS-" + a.getCourseId();
            String cName = c != null ? c.getCourseName() : "Course";
            String cDept = c != null ? c.getDepartment() : "-";

            if ("PRESENT".equalsIgnoreCase(a.getStatus()) || "LATE".equalsIgnoreCase(a.getStatus())) {
                present++;
            } else if ("LEAVE".equalsIgnoreCase(a.getStatus())) {
                leaves++;
            }

            tableModel.addRow(new Object[]{
                    a.getAttendanceDate() != null ? a.getAttendanceDate().toString() : "-",
                    cCode,
                    cName,
                    cDept,
                    a.getStatus()
            });
        }

        totalSessionsCard.setValue(String.valueOf(total));
        attendedCard.setValue(String.valueOf(present));
        leavesCard.setValue(String.valueOf(leaves));

        int eligible = total - leaves;
        double pct = 100.0;
        if (eligible > 0) {
            pct = Math.round(((present * 100.0) / eligible) * 10.0) / 10.0;
        }

        percentageCard.setValue(pct + "%");
        if (pct < 75.0) {
            percentageCard.setSubtitle("⚠️ Defaulter: Below 75% Criteria");
            percentageCard.setAccentColor(AppConfig.DANGER_COLOR);
        } else {
            percentageCard.setSubtitle("Good Standing (>= 75%)");
            percentageCard.setAccentColor(AppConfig.SUCCESS_COLOR);
        }
    }
}

