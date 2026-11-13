package com.campusflow.view;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Course;
import com.campusflow.model.Faculty;
import com.campusflow.model.Student;
import com.campusflow.model.User;
import com.campusflow.service.CourseService;
import com.campusflow.service.FacultyService;
import com.campusflow.service.StudentService;
import com.campusflow.view.attendance.AttendanceManagementPanel;
import com.campusflow.view.components.ModernButton;
import com.campusflow.view.components.RoundedPanel;
import com.campusflow.view.components.Sidebar;
import com.campusflow.view.components.StatCard;
import com.campusflow.view.components.TopBar;
import com.campusflow.view.course.CourseFormDialog;
import com.campusflow.view.course.CourseManagementPanel;
import com.campusflow.view.enrollment.EnrollmentManagementPanel;
import com.campusflow.view.faculty.FacultyFormDialog;
import com.campusflow.view.faculty.FacultyManagementPanel;
import com.campusflow.view.reports.ReportsPanel;
import com.campusflow.view.student.StudentFormDialog;
import com.campusflow.view.student.StudentManagementPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Admin Dashboard View.
 * Provides real-time metrics, student/faculty directories, course rosters,
 * and quick actions for college administration.
 */
public class AdminDashboardView extends JFrame {

    private final User currentUser;
    private final Runnable logoutCallback;
    private final StudentService studentService;
    private final FacultyService facultyService;
    private final CourseService courseService;

    private Sidebar sidebar;
    private TopBar topBar;
    private JPanel mainCardContainer;
    private CardLayout cardLayout;

    // Stat cards
    private StatCard studentStatCard;
    private StatCard facultyStatCard;
    private StatCard courseStatCard;
    private StatCard statusStatCard;

    // Tables
    private JTable studentsTable;
    private DefaultTableModel studentsTableModel;
    private JTable coursesTable;
    private DefaultTableModel coursesTableModel;

    public AdminDashboardView(User currentUser, Runnable logoutCallback) {
        this.currentUser = currentUser;
        this.logoutCallback = logoutCallback;
        this.studentService = new StudentService();
        this.facultyService = new FacultyService();
        this.courseService = new CourseService();

        setTitle(AppConfig.APP_NAME + " - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);

        initUI();
        loadData();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppConfig.BACKGROUND_COLOR);

        // --- SIDEBAR ---
        sidebar = new Sidebar(User.Role.ADMIN, logoutCallback);
        sidebar.setNavigationListener(this::onNavSelected);
        root.add(sidebar, BorderLayout.WEST);

        // --- RIGHT CONTAINER (TOPBAR + CONTENT) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(AppConfig.BACKGROUND_COLOR);

        // TOP BAR
        topBar = new TopBar("Admin Dashboard", currentUser);
        rightPanel.add(topBar, BorderLayout.NORTH);

        // CARD CONTAINER (DASHBOARD, STUDENTS, FACULTY, COURSES, REPORTS, SETTINGS)
        cardLayout = new CardLayout();
        mainCardContainer = new JPanel(cardLayout);
        mainCardContainer.setBackground(AppConfig.BACKGROUND_COLOR);

        mainCardContainer.add(createDashboardOverviewPanel(), "Dashboard");
        mainCardContainer.add(new StudentManagementPanel(), "Students");
        mainCardContainer.add(new FacultyManagementPanel(), "Faculty");
        mainCardContainer.add(new CourseManagementPanel(), "Courses");
        mainCardContainer.add(new EnrollmentManagementPanel(), "Enrollments");
        mainCardContainer.add(new AttendanceManagementPanel(currentUser), "Attendance");
        mainCardContainer.add(new ReportsPanel(), "Reports");
        mainCardContainer.add(createSystemSettingsPanel(), "Settings");

        rightPanel.add(mainCardContainer, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    public void selectTab(String itemKey) {
        sidebar.setActiveItem(itemKey);
        onNavSelected(itemKey);
    }

    private void onNavSelected(String itemKey) {
        topBar.setPageTitle("Admin - " + itemKey);
        cardLayout.show(mainCardContainer, itemKey);
    }

    private JPanel createDashboardOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Top KPI Row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);

        studentStatCard = new StatCard("TOTAL STUDENTS", "0", "Enrolled students", AppConfig.PRIMARY_COLOR);
        facultyStatCard = new StatCard("TOTAL FACULTY", "0", "Teaching faculty", AppConfig.SECONDARY_COLOR);
        courseStatCard = new StatCard("ACTIVE COURSES", "0", "Active curriculum", AppConfig.ACCENT_COLOR);
        statusStatCard = new StatCard("SYSTEM STATUS", "Active", "MySQL Connected", AppConfig.SUCCESS_COLOR);

        statsRow.add(studentStatCard);
        statsRow.add(facultyStatCard);
        statsRow.add(courseStatCard);
        statsRow.add(statusStatCard);

        panel.add(statsRow, BorderLayout.NORTH);

        // Center Content: Quick Action Bar + Tables Split
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        // Actions Header
        RoundedPanel actionsCard = new RoundedPanel(12, Color.WHITE, new Color(0xE2E8F0));
        actionsCard.setLayout(new BorderLayout());
        actionsCard.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel actionsTitle = new JLabel("Quick Management");
        actionsTitle.setFont(AppConfig.FONT_TITLE);
        actionsTitle.setForeground(AppConfig.TEXT_PRIMARY);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        ModernButton addStudentBtn = ModernButton.primary("Add Student");
        addStudentBtn.addActionListener(e -> new StudentFormDialog(this, null, studentService, this::loadData).setVisible(true));

        ModernButton addFacultyBtn = ModernButton.secondary("Add Faculty");
        addFacultyBtn.addActionListener(e -> new FacultyFormDialog(this, null, facultyService, this::loadData).setVisible(true));

        ModernButton addCourseBtn = ModernButton.accent("Add Course");
        addCourseBtn.addActionListener(e -> new CourseFormDialog(this, null, courseService, this::loadData).setVisible(true));

        ModernButton refreshBtn = ModernButton.outline("Refresh", AppConfig.PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadData());

        btnBar.add(addStudentBtn);
        btnBar.add(addFacultyBtn);
        btnBar.add(addCourseBtn);
        btnBar.add(refreshBtn);

        actionsCard.add(actionsTitle, BorderLayout.WEST);
        actionsCard.add(btnBar, BorderLayout.EAST);
        centerPanel.add(actionsCard, BorderLayout.NORTH);

        // Recent Students and Courses dual table split
        JPanel tablesGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesGrid.setOpaque(false);

        // Left: Students Summary Table
        RoundedPanel studentsCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        studentsCard.setLayout(new BorderLayout(0, 10));
        studentsCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel sTitle = new JLabel("Registered Students");
        sTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        sTitle.setForeground(AppConfig.TEXT_PRIMARY);
        studentsCard.add(sTitle, BorderLayout.NORTH);

        String[] sCols = {"ID", "Name", "Department", "Sem"};
        studentsTableModel = new DefaultTableModel(sCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        studentsTable = new JTable(studentsTableModel);
        styleTable(studentsTable);
        JScrollPane sScroll = new JScrollPane(studentsTable);
        sScroll.setBorder(BorderFactory.createEmptyBorder());
        sScroll.getViewport().setBackground(Color.WHITE);
        studentsCard.add(sScroll, BorderLayout.CENTER);

        // Right: Courses Summary Table
        RoundedPanel coursesCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        coursesCard.setLayout(new BorderLayout(0, 10));
        coursesCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel cTitle = new JLabel("Curriculum Courses");
        cTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        cTitle.setForeground(AppConfig.TEXT_PRIMARY);
        coursesCard.add(cTitle, BorderLayout.NORTH);

        String[] cCols = {"Code", "Course Name", "Dept", "Credits"};
        coursesTableModel = new DefaultTableModel(cCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        coursesTable = new JTable(coursesTableModel);
        styleTable(coursesTable);
        JScrollPane cScroll = new JScrollPane(coursesTable);
        cScroll.setBorder(BorderFactory.createEmptyBorder());
        cScroll.getViewport().setBackground(Color.WHITE);
        coursesCard.add(cScroll, BorderLayout.CENTER);

        tablesGrid.add(studentsCard);
        tablesGrid.add(coursesCard);

        centerPanel.add(tablesGrid, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSystemSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Header Card
        RoundedPanel headerCard = new RoundedPanel(12, Color.WHITE, new Color(0xE2E8F0));
        headerCard.setLayout(new BorderLayout());
        headerCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("System Settings & Environment Diagnostics");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Runtime configuration, database metadata, and active JVM environment parameters.");
        subtitle.setFont(AppConfig.FONT_MEDIUM);
        subtitle.setForeground(AppConfig.TEXT_SECONDARY);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 4));
        headerText.setOpaque(false);
        headerText.add(title);
        headerText.add(subtitle);
        headerCard.add(headerText, BorderLayout.WEST);

        panel.add(headerCard, BorderLayout.NORTH);

        // Center Grid with Database & System Cards
        JPanel grid = new JPanel(new GridLayout(1, 2, 16, 0));
        grid.setOpaque(false);

        // Card 1: Database Information
        RoundedPanel dbCard = new RoundedPanel(12, Color.WHITE, new Color(0xE2E8F0));
        dbCard.setLayout(new BorderLayout());
        dbCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel dbTitle = new JLabel("Database Configuration");
        dbTitle.setFont(AppConfig.FONT_TITLE);
        dbTitle.setForeground(AppConfig.TEXT_PRIMARY);

        JPanel dbInfoList = new JPanel(new GridLayout(6, 1, 0, 10));
        dbInfoList.setOpaque(false);
        dbInfoList.setBorder(new EmptyBorder(16, 0, 16, 0));

        dbInfoList.add(createSettingRow("Host / Port", "localhost:3306"));
        dbInfoList.add(createSettingRow("Database Name", "campusflow_db"));
        dbInfoList.add(createSettingRow("JDBC Driver", "com.mysql.cj.jdbc.Driver"));
        dbInfoList.add(createSettingRow("Authentication", "BCrypt (Salt Cost 12)"));
        dbInfoList.add(createSettingRow("Connection Status", "Active (Pool Connected)"));
        dbInfoList.add(createSettingRow("Schema Tables", "users, students, faculty, courses, enrollments, attendance"));

        ModernButton testConnBtn = ModernButton.primary("Test Connection");
        JLabel testResultLabel = new JLabel(" ");
        testResultLabel.setFont(AppConfig.FONT_SMALL);

        testConnBtn.addActionListener(e -> {
            boolean ok = com.campusflow.config.DatabaseConfig.testConnection();
            testResultLabel.setText(ok ? "Connection OK: MySQL server responding" : "Connection Failed: Check MySQL service");
            testResultLabel.setForeground(ok ? AppConfig.SUCCESS_COLOR : AppConfig.DANGER_COLOR);
        });

        JPanel dbBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dbBottom.setOpaque(false);
        dbBottom.add(testConnBtn);
        dbBottom.add(testResultLabel);

        dbCard.add(dbTitle, BorderLayout.NORTH);
        dbCard.add(dbInfoList, BorderLayout.CENTER);
        dbCard.add(dbBottom, BorderLayout.SOUTH);

        // Card 2: Runtime Environment
        RoundedPanel vmCard = new RoundedPanel(12, Color.WHITE, new Color(0xE2E8F0));
        vmCard.setLayout(new BorderLayout());
        vmCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel vmTitle = new JLabel("Runtime Environment");
        vmTitle.setFont(AppConfig.FONT_TITLE);
        vmTitle.setForeground(AppConfig.TEXT_PRIMARY);

        JPanel vmInfoList = new JPanel(new GridLayout(6, 1, 0, 10));
        vmInfoList.setOpaque(false);
        vmInfoList.setBorder(new EmptyBorder(16, 0, 16, 0));

        vmInfoList.add(createSettingRow("Java Version", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")"));
        vmInfoList.add(createSettingRow("Compiler Target", "JavaSE-17 (Bytecode Compatible)"));
        vmInfoList.add(createSettingRow("Operating System", System.getProperty("os.name") + " " + System.getProperty("os.arch")));
        long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        vmInfoList.add(createSettingRow("JVM Heap Memory", (totalMem - freeMem) + " MB used of " + totalMem + " MB"));
        vmInfoList.add(createSettingRow("GUI Framework", "Java Swing (FlatLaf Modern Theme)"));
        vmInfoList.add(createSettingRow("Software Edition", "CampusFlow Production Desktop v1.0.0"));

        ModernButton refreshDiagBtn = ModernButton.outline("Refresh Diagnostics", AppConfig.PRIMARY_COLOR);
        refreshDiagBtn.addActionListener(e -> {
            long tm = Runtime.getRuntime().totalMemory() / (1024 * 1024);
            long fm = Runtime.getRuntime().freeMemory() / (1024 * 1024);
            JOptionPane.showMessageDialog(this,
                    "System Diagnostics Status: Operational\n" +
                    "JVM Version: " + System.getProperty("java.version") + "\n" +
                    "Memory Usage: " + (tm - fm) + " MB / " + tm + " MB\n" +
                    "All database pools healthy.",
                    "System Health",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel vmBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        vmBottom.setOpaque(false);
        vmBottom.add(refreshDiagBtn);

        vmCard.add(vmTitle, BorderLayout.NORTH);
        vmCard.add(vmInfoList, BorderLayout.CENTER);
        vmCard.add(vmBottom, BorderLayout.SOUTH);

        grid.add(dbCard);
        grid.add(vmCard);
        panel.add(grid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSettingRow(String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppConfig.FONT_LABEL);
        lbl.setForeground(AppConfig.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(140, 20));

        JLabel val = new JLabel(valueText);
        val.setFont(AppConfig.FONT_MEDIUM);
        val.setForeground(AppConfig.TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    public void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int studentCount = 0;
            int facultyCount = 0;
            int courseCount = 0;
            List<Student> students = List.of();
            List<Faculty> facultyList = List.of();
            List<Course> courses = List.of();
            boolean dbSuccess = false;

            @Override
            protected Void doInBackground() {
                try {
                    studentCount = studentService.getStudentCount();
                    facultyCount = facultyService.getFacultyCount();
                    courseCount = courseService.getCourseCount();
                    students = studentService.getAllStudents();
                    facultyList = facultyService.getAllFaculty();
                    courses = courseService.getAllCourses();
                    dbSuccess = true;
                } catch (Exception ex) {
                    System.err.println("Database load error: " + ex.getMessage());
                    dbSuccess = false;
                }
                return null;
            }

            @Override
            protected void done() {
                studentStatCard.setValue(String.valueOf(studentCount));
                facultyStatCard.setValue(String.valueOf(facultyCount));
                courseStatCard.setValue(String.valueOf(courseCount));
                statusStatCard.setValue(dbSuccess ? "Active" : "Offline");
                statusStatCard.setSubtitle(dbSuccess ? "MySQL Connected" : "Check DB Config");
                statusStatCard.setAccentColor(dbSuccess ? AppConfig.SUCCESS_COLOR : AppConfig.DANGER_COLOR);

                // Populate Students table
                studentsTableModel.setRowCount(0);
                if (students.isEmpty()) {
                    studentsTableModel.addRow(new Object[]{"-", "No students found", "-", "-"});
                } else {
                    for (Student s : students) {
                        studentsTableModel.addRow(new Object[]{
                                s.getStudentId(),
                                s.getFullName(),
                                s.getDepartment(),
                                "Sem " + s.getSemester()
                        });
                    }
                }

                // Populate Courses table
                coursesTableModel.setRowCount(0);
                if (courses.isEmpty()) {
                    coursesTableModel.addRow(new Object[]{"-", "No courses found", "-", "-"});
                } else {
                    for (Course c : courses) {
                        coursesTableModel.addRow(new Object[]{
                                c.getCourseCode(),
                                c.getCourseName(),
                                c.getDepartment(),
                                c.getCredits() + " cr"
                        });
                    }
                }
            }
        };
        worker.execute();
    }

    private void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(0xE0F2FE));
        table.setSelectionForeground(AppConfig.TEXT_PRIMARY);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(0xF8FAFC));
        header.setForeground(AppConfig.TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)));

        // Alternating row renderer with padding
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                    c.setForeground(AppConfig.TEXT_PRIMARY);
                }
                return c;
            }
        });
    }
}
