package com.campusflow.view;

import com.campusflow.config.AppConfig;
import com.campusflow.dao.AttendanceDAO;
import com.campusflow.dao.CourseDAO;
import com.campusflow.dao.impl.AttendanceDAOImpl;
import com.campusflow.dao.impl.CourseDAOImpl;
import com.campusflow.model.Course;
import com.campusflow.model.Faculty;
import com.campusflow.model.User;
import com.campusflow.service.CourseService;
import com.campusflow.service.FacultyService;
import com.campusflow.view.attendance.AttendanceManagementPanel;
import com.campusflow.view.components.ModernButton;
import com.campusflow.view.components.RoundedPanel;
import com.campusflow.view.components.Sidebar;
import com.campusflow.view.components.StatCard;
import com.campusflow.view.components.TopBar;
import com.campusflow.view.enrollment.EnrollmentManagementPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Faculty Dashboard View.
 * Displays instructor courses, class rosters, attendance overview,
 * and academic management actions with real-time database calculations.
 */
public class FacultyDashboardView extends JFrame {
    private static final long serialVersionUID = 1L;

    private final User currentUser;
    private final Runnable logoutCallback;
    private final FacultyService facultyService;
    private final CourseService courseService;
    private final CourseDAO courseDAO;
    private final AttendanceDAO attendanceDAO;

    private Faculty facultyProfile;
    private Sidebar sidebar;
    private TopBar topBar;
    private JPanel mainCardContainer;
    private CardLayout cardLayout;

    // Stat cards
    private StatCard coursesStatCard;
    private StatCard studentsStatCard;
    private StatCard attendanceStatCard;
    private StatCard deptStatCard;

    // Table
    private JTable overviewCoursesTable;
    private JTable fullCoursesTable;
    private DefaultTableModel coursesTableModel;

    public FacultyDashboardView(User currentUser, Runnable logoutCallback) {
        this.currentUser = currentUser;
        this.logoutCallback = logoutCallback;
        this.facultyService = new FacultyService();
        this.courseService = new CourseService();
        this.courseDAO = new CourseDAOImpl();
        this.attendanceDAO = new AttendanceDAOImpl();

        setTitle(AppConfig.APP_NAME + " - Faculty Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setBackground(AppConfig.BACKGROUND_COLOR);

        initUI();
        loadData();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppConfig.BACKGROUND_COLOR);

        // --- SIDEBAR ---
        sidebar = new Sidebar(User.Role.FACULTY, logoutCallback);
        sidebar.setNavigationListener(this::onNavSelected);
        root.add(sidebar, BorderLayout.WEST);

        // --- RIGHT PANEL ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(AppConfig.BACKGROUND_COLOR);

        // TOP BAR
        topBar = new TopBar("Faculty Portal", currentUser);
        rightPanel.add(topBar, BorderLayout.NORTH);

        // CARD CONTAINER
        cardLayout = new CardLayout();
        mainCardContainer = new JPanel(cardLayout);
        mainCardContainer.setBackground(AppConfig.BACKGROUND_COLOR);

        mainCardContainer.add(createOverviewPanel(), "Dashboard");
        mainCardContainer.add(createCoursesPanel(), "My Courses");
        mainCardContainer.add(new AttendanceManagementPanel(currentUser), "Attendance");
        mainCardContainer.add(new EnrollmentManagementPanel(currentUser), "Students");
        mainCardContainer.add(new EnrollmentManagementPanel(currentUser), "Grades");

        rightPanel.add(mainCardContainer, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void onNavSelected(String itemKey) {
        topBar.setPageTitle("Faculty - " + itemKey);
        cardLayout.show(mainCardContainer, itemKey);
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Stats Row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);

        coursesStatCard = new StatCard("ASSIGNED COURSES", "0", "Current semester", AppConfig.PRIMARY_COLOR);
        studentsStatCard = new StatCard("STUDENTS TAUGHT", "0", "Active enrollments", AppConfig.SECONDARY_COLOR);
        attendanceStatCard = new StatCard("AVG ATTENDANCE", "0.0%", "Academic average", AppConfig.SUCCESS_COLOR);
        deptStatCard = new StatCard("DEPARTMENT", "General", "Faculty Department", AppConfig.ACCENT_COLOR);

        statsRow.add(coursesStatCard);
        statsRow.add(studentsStatCard);
        statsRow.add(attendanceStatCard);
        statsRow.add(deptStatCard);

        panel.add(statsRow, BorderLayout.NORTH);

        // Center: Actions & My Courses Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        // Quick Actions Card
        RoundedPanel actionsCard = new RoundedPanel(12, Color.WHITE, new Color(0xE2E8F0));
        actionsCard.setLayout(new BorderLayout());
        actionsCard.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel actionsTitle = new JLabel("Academic Management");
        actionsTitle.setFont(AppConfig.FONT_TITLE);
        actionsTitle.setForeground(AppConfig.TEXT_PRIMARY);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        ModernButton markAttBtn = ModernButton.primary("Mark Attendance");
        markAttBtn.addActionListener(e -> {
            sidebar.setActiveItem("Attendance");
            onNavSelected("Attendance");
        });

        ModernButton enterGradesBtn = ModernButton.secondary("Enter Grades");
        enterGradesBtn.addActionListener(e -> {
            sidebar.setActiveItem("Grades");
            onNavSelected("Grades");
        });

        ModernButton refreshBtn = ModernButton.outline("Refresh", AppConfig.PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadData());

        btnBar.add(markAttBtn);
        btnBar.add(enterGradesBtn);
        btnBar.add(refreshBtn);

        actionsCard.add(actionsTitle, BorderLayout.WEST);
        actionsCard.add(btnBar, BorderLayout.EAST);
        centerPanel.add(actionsCard, BorderLayout.NORTH);

        // My Courses Summary Card
        RoundedPanel tableCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel tableTitle = new JLabel("Assigned Course Sections");
        tableTitle.setFont(AppConfig.FONT_TITLE);
        tableTitle.setForeground(AppConfig.TEXT_PRIMARY);

        ModernButton viewAllBtn = ModernButton.outline("View Full Directory", AppConfig.PRIMARY_COLOR);
        viewAllBtn.addActionListener(e -> {
            sidebar.setActiveItem("My Courses");
            onNavSelected("My Courses");
        });

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(viewAllBtn, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Course Name", "Credits", "Department", "Capacity"};
        coursesTableModel = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        overviewCoursesTable = new JTable(coursesTableModel);
        styleTable(overviewCoursesTable);

        JScrollPane scroll = new JScrollPane(overviewCoursesTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        RoundedPanel card = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("My Assigned Courses");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        ModernButton markAttBtn = ModernButton.primary("Mark Attendance");
        markAttBtn.addActionListener(e -> {
            sidebar.setActiveItem("Attendance");
            onNavSelected("Attendance");
        });

        ModernButton refreshBtn = ModernButton.outline("Refresh", AppConfig.PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadData());

        btnBar.add(markAttBtn);
        btnBar.add(refreshBtn);

        header.add(title, BorderLayout.WEST);
        header.add(btnBar, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        fullCoursesTable = new JTable(coursesTableModel);
        styleTable(fullCoursesTable);

        JScrollPane scroll = new JScrollPane(fullCoursesTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    public void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Course> courses = List.of();
            String facultyName = currentUser.getUsername();
            String department = "General";
            String designation = "Faculty";
            int totalEnrolledStudents = 0;
            double avgAttendance = 0.0;

            @Override
            protected Void doInBackground() {
                try {
                    Optional<Faculty> fOpt = facultyService.getFacultyByUserId(currentUser.getId());
                    if (fOpt.isPresent()) {
                        facultyProfile = fOpt.get();
                        facultyName = facultyProfile.getFullName();
                        department = facultyProfile.getDepartment();
                        designation = facultyProfile.getDesignation();
                        courses = courseService.getCoursesByFacultyId(facultyProfile.getId());
                    } else {
                        courses = courseService.getAllCourses();
                    }

                    // Compute real enrolled student count and average attendance across assigned courses
                    int studentSum = 0;
                    double attSum = 0.0;
                    int attCount = 0;
                    for (Course c : courses) {
                        studentSum += courseDAO.getEnrolledCount(c.getId());
                        List<com.campusflow.model.Attendance> atts = attendanceDAO.findByCourseId(c.getId());
                        if (!atts.isEmpty()) {
                            long pres = atts.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
                            long eligible = atts.stream().filter(a -> !"LEAVE".equalsIgnoreCase(a.getStatus())).count();
                            if (eligible > 0) {
                                attSum += (pres * 100.0) / eligible;
                                attCount++;
                            }
                        }
                    }
                    totalEnrolledStudents = studentSum;
                    avgAttendance = attCount > 0 ? (attSum / attCount) : 0.0;

                } catch (Exception ex) {
                    System.err.println("Faculty load data error: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                topBar.updateUserInfo(facultyName, User.Role.FACULTY);
                coursesStatCard.setValue(String.valueOf(courses.size()));
                studentsStatCard.setValue(String.valueOf(totalEnrolledStudents));
                attendanceStatCard.setValue(String.format(Locale.US, "%.1f%%", avgAttendance));
                attendanceStatCard.setSubtitle(avgAttendance >= 75.0 ? "Good Standing (>= 75%)" : (courses.isEmpty() ? "No active sessions" : "Deficit Warning (< 75%)"));
                attendanceStatCard.setAccentColor(avgAttendance >= 75.0 ? AppConfig.SUCCESS_COLOR : (courses.isEmpty() ? AppConfig.TEXT_SECONDARY : AppConfig.DANGER_COLOR));

                deptStatCard.setValue(department != null ? department : "General");
                deptStatCard.setSubtitle(designation != null ? designation : "Faculty");

                coursesTableModel.setRowCount(0);
                if (courses.isEmpty()) {
                    coursesTableModel.addRow(new Object[]{"-", "No courses assigned", "-", "-", "-"});
                } else {
                    for (Course c : courses) {
                        int enrolled = courseDAO.getEnrolledCount(c.getId());
                        coursesTableModel.addRow(new Object[]{
                                c.getCourseCode(),
                                c.getCourseName(),
                                c.getCredits() + " Credits",
                                c.getDepartment(),
                                enrolled + " / " + c.getMaxStudents()
                        });
                    }
                }
                if (overviewCoursesTable != null) {
                    overviewCoursesTable.revalidate();
                    overviewCoursesTable.repaint();
                }
                if (fullCoursesTable != null) {
                    fullCoursesTable.revalidate();
                    fullCoursesTable.repaint();
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

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(0xF8FAFC));
        header.setForeground(AppConfig.TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
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
