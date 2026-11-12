package com.campusflow.view;

import com.campusflow.config.AppConfig;
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
import java.util.Optional;

/**
 * Faculty Dashboard View.
 * Displays instructor courses, class rosters, attendance overview,
 * and academic management actions.
 */
public class FacultyDashboardView extends JFrame {

    private final User currentUser;
    private final Runnable logoutCallback;
    private final FacultyService facultyService;
    private final CourseService courseService;

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
    private JTable coursesTable;
    private DefaultTableModel coursesTableModel;

    public FacultyDashboardView(User currentUser, Runnable logoutCallback) {
        this.currentUser = currentUser;
        this.logoutCallback = logoutCallback;
        this.facultyService = new FacultyService();
        this.courseService = new CourseService();

        setTitle(AppConfig.APP_NAME + " - Faculty Portal");
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
        mainCardContainer.add(new EnrollmentManagementPanel(currentUser), "Enrollments");
        mainCardContainer.add(createPlaceholderPanel("Grading & Evaluation", "Enter mid-term and final exam grades and grade curve calculations."), "Grades");

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

        coursesStatCard = new StatCard("ASSIGNED COURSES", "0", "Current semester", AppConfig.PRIMARY_COLOR, "📚");
        studentsStatCard = new StatCard("STUDENTS TAUGHT", "0", "Active enrollments", AppConfig.SECONDARY_COLOR, "👥");
        attendanceStatCard = new StatCard("AVG ATTENDANCE", "92%", "Last 30 days", AppConfig.SUCCESS_COLOR, "✓");
        deptStatCard = new StatCard("DEPARTMENT", "General", "Faculty of Engineering", AppConfig.ACCENT_COLOR, "🏫");

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

        JLabel actionsTitle = new JLabel("Academic Tools");
        actionsTitle.setFont(AppConfig.FONT_TITLE);
        actionsTitle.setForeground(AppConfig.TEXT_PRIMARY);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        ModernButton markAttBtn = ModernButton.primary("✓ Mark Attendance");
        markAttBtn.addActionListener(e -> {
            sidebar.setActiveItem("Attendance");
            onNavSelected("Attendance");
        });

        ModernButton enterGradesBtn = ModernButton.secondary("📝 Enter Grades");
        enterGradesBtn.addActionListener(e -> {
            sidebar.setActiveItem("Students");
            onNavSelected("Students");
        });

        ModernButton refreshBtn = ModernButton.outline("↻ Refresh", AppConfig.PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadData());

        btnBar.add(markAttBtn);
        btnBar.add(enterGradesBtn);
        btnBar.add(refreshBtn);

        actionsCard.add(actionsTitle, BorderLayout.WEST);
        actionsCard.add(btnBar, BorderLayout.EAST);
        centerPanel.add(actionsCard, BorderLayout.NORTH);

        // Assigned Courses Table Card
        RoundedPanel tableCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel tableTitle = new JLabel("My Assigned Courses");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        tableTitle.setForeground(AppConfig.TEXT_PRIMARY);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Course Title", "Credits", "Department", "Max Enrolled"};
        coursesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        coursesTable = new JTable(coursesTableModel);
        styleTable(coursesTable);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        RoundedPanel card = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Faculty Course Schedule");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(coursesTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPlaceholderPanel(String titleText, String descText) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppConfig.BACKGROUND_COLOR);

        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(0xE2E8F0));
        card.setPreferredSize(new Dimension(520, 260));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(36, 36, 36, 36));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(AppConfig.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("<html><center>" + descText + "<br><br><b>CampusFlow Phase 2 Feature</b></center></html>");
        desc.setFont(AppConfig.FONT_MEDIUM);
        desc.setForeground(AppConfig.TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        ModernButton btn = ModernButton.primary("Back to Dashboard");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            sidebar.setActiveItem("Dashboard");
            onNavSelected("Dashboard");
        });

        card.add(title);
        card.add(Box.createVerticalStrut(14));
        card.add(desc);
        card.add(Box.createVerticalStrut(24));
        card.add(btn);

        panel.add(card);
        return panel;
    }

    public void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Course> courses = List.of();
            String facultyName = currentUser.getUsername();
            String department = "General";
            String designation = "Faculty";

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
                } catch (Exception ex) {
                    System.err.println("Faculty load data error: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                topBar.updateUserInfo(facultyName, User.Role.FACULTY);
                coursesStatCard.setValue(String.valueOf(courses.size()));
                studentsStatCard.setValue(String.valueOf(courses.size() * 30));
                deptStatCard.setValue(department != null ? department : "General");
                deptStatCard.setSubtitle(designation != null ? designation : "Faculty");

                coursesTableModel.setRowCount(0);
                if (courses.isEmpty()) {
                    coursesTableModel.addRow(new Object[]{"-", "No courses assigned", "-", "-", "-"});
                } else {
                    for (Course c : courses) {
                        coursesTableModel.addRow(new Object[]{
                                c.getCourseCode(),
                                c.getCourseName(),
                                c.getCredits() + " Credits",
                                c.getDepartment(),
                                c.getMaxStudents() + " Students"
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

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(0xF8FAFC));
        header.setForeground(AppConfig.TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)));

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
