package com.campusflow.view.reports;

import com.campusflow.config.AppConfig;
import com.campusflow.model.report.CourseAttendanceStat;
import com.campusflow.model.report.CourseEnrollmentStat;
import com.campusflow.model.report.DefaulterReportItem;
import com.campusflow.service.ReportService;
import com.campusflow.view.components.ModernButton;
import com.campusflow.view.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final ReportService reportService;

    // Tab 1: Defaulters
    private JTable defaulterTable;
    private DefaultTableModel defaulterModel;
    private JSpinner thresholdSpinner;
    private JLabel defaulterCountLabel;

    // Tab 2: Enrollment Stats
    private JTable enrollmentTable;
    private DefaultTableModel enrollmentModel;
    private JLabel enrollmentSummaryLabel;

    // Tab 3: Attendance Analytics
    private JTable attendanceTable;
    private DefaultTableModel attendanceModel;
    private JLabel attendanceSummaryLabel;

    public ReportsPanel() {
        this.reportService = new ReportService();

        setLayout(new BorderLayout(0, 16));
        setBackground(AppConfig.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        loadAllReports();
    }

    private void initUI() {
        // TOP HEADER CARD
        RoundedPanel headerCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        headerCard.setLayout(new BorderLayout(14, 0));
        headerCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 4));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Academic Reports & Analytics");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Real-time database metrics: Attendance defaulters (<75%), course capacities, and attendance rates");
        subtitle.setFont(AppConfig.FONT_MEDIUM);
        subtitle.setForeground(AppConfig.TEXT_SECONDARY);

        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        headerCard.add(titlePanel, BorderLayout.WEST);

        ModernButton refreshAllBtn = ModernButton.primary("↻ Refresh All Reports");
        refreshAllBtn.addActionListener(e -> loadAllReports());
        headerCard.add(refreshAllBtn, BorderLayout.EAST);

        add(headerCard, BorderLayout.NORTH);

        // TABBED PANE
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("⚠️ Defaulter Watchlist (< 75%)", createDefaulterTab());
        tabbedPane.addTab("📊 Course Capacity & Enrollment", createEnrollmentTab());
        tabbedPane.addTab("📈 Course Attendance Summary", createAttendanceTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDefaulterTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));

        // Sub control bar
        RoundedPanel subBar = new RoundedPanel(10, Color.WHITE, new Color(0xE2E8F0));
        subBar.setLayout(new BorderLayout());
        subBar.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel leftControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControl.setOpaque(false);
        leftControl.add(new JLabel("Threshold Attendance %:"));

        thresholdSpinner = new JSpinner(new SpinnerNumberModel(75.0, 10.0, 100.0, 5.0));
        thresholdSpinner.setPreferredSize(new Dimension(80, 32));
        thresholdSpinner.addChangeListener(e -> loadDefaulters());
        leftControl.add(thresholdSpinner);

        ModernButton applyBtn = ModernButton.secondary("Apply");
        applyBtn.addActionListener(e -> loadDefaulters());
        leftControl.add(applyBtn);

        defaulterCountLabel = new JLabel("(0 students below threshold)");
        defaulterCountLabel.setFont(AppConfig.FONT_MEDIUM);
        defaulterCountLabel.setForeground(AppConfig.DANGER_COLOR);
        leftControl.add(defaulterCountLabel);
        subBar.add(leftControl, BorderLayout.WEST);

        panel.add(subBar, BorderLayout.NORTH);

        // Table
        RoundedPanel tableCard = new RoundedPanel(12, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 12, 12, 12));

        String[] cols = {"Student ID", "Student Name", "Department", "Course Code", "Course Name", "Present", "Conducted", "Leaves", "Attendance %", "Deficit Status"};
        defaulterModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        defaulterTable = createStyledTable(defaulterModel);
        // Highlight deficit percentage in danger color
        defaulterTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
                setForeground(AppConfig.DANGER_COLOR);
                setFont(new Font("SansSerif", Font.BOLD, 13));
                setHorizontalAlignment(SwingConstants.CENTER);
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(defaulterTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEnrollmentTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));

        // Sub control bar
        RoundedPanel subBar = new RoundedPanel(10, Color.WHITE, new Color(0xE2E8F0));
        subBar.setLayout(new BorderLayout());
        subBar.setBorder(new EmptyBorder(10, 16, 10, 16));

        enrollmentSummaryLabel = new JLabel("Enrollment stats across all active courses");
        enrollmentSummaryLabel.setFont(AppConfig.FONT_MEDIUM);
        enrollmentSummaryLabel.setForeground(AppConfig.TEXT_SECONDARY);
        subBar.add(enrollmentSummaryLabel, BorderLayout.WEST);

        panel.add(subBar, BorderLayout.NORTH);

        // Table
        RoundedPanel tableCard = new RoundedPanel(12, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 12, 12, 12));

        String[] cols = {"Course Code", "Course Name", "Department", "Assigned Faculty", "Max Capacity", "Enrolled Students", "Occupancy %"};
        enrollmentModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        enrollmentTable = createStyledTable(enrollmentModel);
        JScrollPane scroll = new JScrollPane(enrollmentTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAttendanceTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));

        // Sub control bar
        RoundedPanel subBar = new RoundedPanel(10, Color.WHITE, new Color(0xE2E8F0));
        subBar.setLayout(new BorderLayout());
        subBar.setBorder(new EmptyBorder(10, 16, 10, 16));

        attendanceSummaryLabel = new JLabel("Conducted sessions & aggregate attendance rates");
        attendanceSummaryLabel.setFont(AppConfig.FONT_MEDIUM);
        attendanceSummaryLabel.setForeground(AppConfig.TEXT_SECONDARY);
        subBar.add(attendanceSummaryLabel, BorderLayout.WEST);

        panel.add(subBar, BorderLayout.NORTH);

        // Table
        RoundedPanel tableCard = new RoundedPanel(12, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 12, 12, 12));

        String[] cols = {"Course Code", "Course Name", "Department", "Assigned Faculty", "Conducted Sessions", "Enrolled Students", "Average Attendance %"};
        attendanceModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        attendanceTable = createStyledTable(attendanceModel);
        JScrollPane scroll = new JScrollPane(attendanceTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(AppConfig.FONT_MEDIUM);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(0xF8FAFC));
        table.getTableHeader().setForeground(AppConfig.TEXT_SECONDARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0 || i >= 3) {
                table.getColumnModel().getColumn(i).setCellRenderer(center);
            }
        }
        return table;
    }

    public void loadAllReports() {
        loadDefaulters();
        loadEnrollmentStats();
        loadAttendanceStats();
    }

    private void loadDefaulters() {
        double threshold = (double) thresholdSpinner.getValue();
        List<DefaulterReportItem> defaulters = reportService.getDefaulterReport(threshold);
        defaulterModel.setRowCount(0);

        for (DefaulterReportItem item : defaulters) {
            defaulterModel.addRow(new Object[]{
                    item.getStudentCode(),
                    item.getStudentName(),
                    item.getDepartment(),
                    item.getCourseCode(),
                    item.getCourseName(),
                    item.getPresentCount(),
                    item.getTotalCount(),
                    item.getLeaveCount(),
                    item.getAttendancePercentage() + "%",
                    "CRITICAL DEFICIT"
            });
        }
        defaulterCountLabel.setText("(" + defaulters.size() + " records strictly below " + threshold + "%)");
    }

    private void loadEnrollmentStats() {
        List<CourseEnrollmentStat> list = reportService.getCourseEnrollmentReport();
        enrollmentModel.setRowCount(0);

        int totalSeats = 0;
        int totalEnrolled = 0;
        for (CourseEnrollmentStat s : list) {
            totalSeats += s.getMaxStudents();
            totalEnrolled += s.getEnrolledCount();
            enrollmentModel.addRow(new Object[]{
                    s.getCourseCode(),
                    s.getCourseName(),
                    s.getDepartment(),
                    s.getFacultyName(),
                    s.getMaxStudents(),
                    s.getEnrolledCount(),
                    s.getOccupancyPercentage() + "%"
            });
        }
        enrollmentSummaryLabel.setText(String.format("Courses: %d | Total Capacity: %d | Enrolled: %d", list.size(), totalSeats, totalEnrolled));
    }

    private void loadAttendanceStats() {
        List<CourseAttendanceStat> list = reportService.getCourseAttendanceReport();
        attendanceModel.setRowCount(0);

        for (CourseAttendanceStat s : list) {
            attendanceModel.addRow(new Object[]{
                    s.getCourseCode(),
                    s.getCourseName(),
                    s.getDepartment(),
                    s.getFacultyName(),
                    s.getConductedSessions(),
                    s.getEnrolledStudents(),
                    s.getAverageAttendancePercentage() + "%"
            });
        }
    }
}

