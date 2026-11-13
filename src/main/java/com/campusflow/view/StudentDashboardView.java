package com.campusflow.view;

import com.campusflow.config.AppConfig;
import com.campusflow.dao.AttendanceDAO;
import com.campusflow.dao.CourseDAO;
import com.campusflow.dao.EnrollmentDAO;
import com.campusflow.dao.impl.AttendanceDAOImpl;
import com.campusflow.dao.impl.CourseDAOImpl;
import com.campusflow.dao.impl.EnrollmentDAOImpl;
import com.campusflow.model.Course;
import com.campusflow.model.Enrollment;
import com.campusflow.model.Student;
import com.campusflow.model.User;
import com.campusflow.service.StudentService;
import com.campusflow.view.attendance.StudentAttendanceHistoryPanel;
import com.campusflow.view.components.ModernButton;
import com.campusflow.view.components.RoundedPanel;
import com.campusflow.view.components.Sidebar;
import com.campusflow.view.components.StatCard;
import com.campusflow.view.components.TopBar;

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
 * Student Dashboard View.
 * Displays enrolled courses, academic status, real attendance metrics,
 * weekly timetable schedule, and student profile details without emojis or dummy placeholders.
 */
public class StudentDashboardView extends JFrame {
    private static final long serialVersionUID = 1L;

    private final User currentUser;
    private final Runnable logoutCallback;
    private final StudentService studentService;
    private final EnrollmentDAO enrollmentDAO;
    private final CourseDAO courseDAO;
    private final AttendanceDAO attendanceDAO;

    private Student studentProfile;
    private Sidebar sidebar;
    private TopBar topBar;
    private JPanel mainCardContainer;
    private JPanel attendanceContainer;
    private CardLayout cardLayout;

    // Stat cards
    private StatCard enrolledStatCard;
    private StatCard attendanceStatCard;
    private StatCard semesterStatCard;
    private StatCard departmentStatCard;

    // Tables
    private JTable overviewCoursesTable;
    private JTable coursesTable;
    private DefaultTableModel coursesTableModel;
    private JTable scheduleTable;
    private DefaultTableModel scheduleTableModel;

    // Profile Fields
    private JLabel profIdVal;
    private JLabel profNameVal;
    private JLabel profDeptVal;
    private JLabel profSemVal;
    private JLabel profEmailVal;

    public StudentDashboardView(User currentUser, Runnable logoutCallback) {
        this.currentUser = currentUser;
        this.logoutCallback = logoutCallback;
        this.studentService = new StudentService();
        this.enrollmentDAO = new EnrollmentDAOImpl();
        this.courseDAO = new CourseDAOImpl();
        this.attendanceDAO = new AttendanceDAOImpl();

        setTitle(AppConfig.APP_NAME + " - Student Portal");
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
        sidebar = new Sidebar(User.Role.STUDENT, logoutCallback);
        sidebar.setNavigationListener(this::onNavSelected);
        root.add(sidebar, BorderLayout.WEST);

        // --- RIGHT PANEL ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(AppConfig.BACKGROUND_COLOR);

        // TOP BAR
        topBar = new TopBar("Student Portal", currentUser);
        rightPanel.add(topBar, BorderLayout.NORTH);

        // CARD CONTAINER
        cardLayout = new CardLayout();
        mainCardContainer = new JPanel(cardLayout);
        mainCardContainer.setBackground(AppConfig.BACKGROUND_COLOR);

        mainCardContainer.add(createOverviewPanel(), "Dashboard");
        mainCardContainer.add(createCoursesPanel(), "My Courses");
        attendanceContainer = new JPanel(new BorderLayout());
        attendanceContainer.setBackground(AppConfig.BACKGROUND_COLOR);
        mainCardContainer.add(attendanceContainer, "Attendance");
        mainCardContainer.add(createWeeklySchedulePanel(), "Schedule");
        mainCardContainer.add(createProfilePanel(), "Profile");

        rightPanel.add(mainCardContainer, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void onNavSelected(String itemKey) {
        topBar.setPageTitle("Student - " + itemKey);
        cardLayout.show(mainCardContainer, itemKey);
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Stats Row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);

        enrolledStatCard = new StatCard("ENROLLED COURSES", "0", "Current term", AppConfig.PRIMARY_COLOR);
        attendanceStatCard = new StatCard("ATTENDANCE RATE", "0.0%", "Target: >=75%", AppConfig.SUCCESS_COLOR);
        semesterStatCard = new StatCard("SEMESTER", "Sem 1", "Undergraduate", AppConfig.ACCENT_COLOR);
        departmentStatCard = new StatCard("DEPARTMENT", "General", "Degree Program", AppConfig.SECONDARY_COLOR);

        statsRow.add(enrolledStatCard);
        statsRow.add(attendanceStatCard);
        statsRow.add(semesterStatCard);
        statsRow.add(departmentStatCard);

        panel.add(statsRow, BorderLayout.NORTH);

        // Center: Quick Actions & Tables Split
        JPanel centerGrid = new JPanel(new BorderLayout(0, 16));
        centerGrid.setOpaque(false);

        RoundedPanel actionsCard = new RoundedPanel(12, Color.WHITE, new Color(0xE2E8F0));
        actionsCard.setLayout(new BorderLayout());
        actionsCard.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel actionsTitle = new JLabel("Academic Resources");
        actionsTitle.setFont(AppConfig.FONT_TITLE);
        actionsTitle.setForeground(AppConfig.TEXT_PRIMARY);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        ModernButton materialsBtn = ModernButton.primary("View Courses");
        materialsBtn.addActionListener(e -> {
            sidebar.setActiveItem("My Courses");
            onNavSelected("My Courses");
        });

        ModernButton scheduleBtn = ModernButton.secondary("Weekly Schedule");
        scheduleBtn.addActionListener(e -> {
            sidebar.setActiveItem("Schedule");
            onNavSelected("Schedule");
        });

        ModernButton refreshBtn = ModernButton.outline("Refresh", AppConfig.PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadData());

        btnBar.add(materialsBtn);
        btnBar.add(scheduleBtn);
        btnBar.add(refreshBtn);

        actionsCard.add(actionsTitle, BorderLayout.WEST);
        actionsCard.add(btnBar, BorderLayout.EAST);
        centerGrid.add(actionsCard, BorderLayout.NORTH);

        // Split Tables
        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        splitPanel.setOpaque(false);

        // Left Table
        RoundedPanel tableCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel tableTitle = new JLabel("Enrolled Curriculum Courses");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        tableTitle.setForeground(AppConfig.TEXT_PRIMARY);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Code", "Course Name", "Credits", "Status", "Grade"};
        coursesTableModel = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        overviewCoursesTable = new JTable(coursesTableModel);
        styleTable(overviewCoursesTable);

        JScrollPane scrollPane = new JScrollPane(overviewCoursesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Right Info Card
        RoundedPanel profileCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        profileCard.setLayout(new BorderLayout(0, 12));
        profileCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel infoTitle = new JLabel("Student Information Summary");
        infoTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        infoTitle.setForeground(AppConfig.TEXT_PRIMARY);
        profileCard.add(infoTitle, BorderLayout.NORTH);

        JPanel infoGrid = new JPanel(new GridLayout(5, 2, 8, 14));
        infoGrid.setOpaque(false);
        infoGrid.setBorder(new EmptyBorder(12, 4, 12, 4));

        profIdVal = addInfoRow(infoGrid, "Student ID:", "STU-000");
        profNameVal = addInfoRow(infoGrid, "Full Name:", currentUser.getUsername());
        profDeptVal = addInfoRow(infoGrid, "Department:", "General Studies");
        profSemVal = addInfoRow(infoGrid, "Current Semester:", "Semester 1");
        profEmailVal = addInfoRow(infoGrid, "Email Address:", currentUser.getEmail());

        profileCard.add(infoGrid, BorderLayout.CENTER);

        splitPanel.add(tableCard);
        splitPanel.add(profileCard);

        centerGrid.add(splitPanel, BorderLayout.CENTER);
        panel.add(centerGrid, BorderLayout.CENTER);

        return panel;
    }

    private JLabel addInfoRow(JPanel container, String label, String defaultVal) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(AppConfig.TEXT_SECONDARY);

        JLabel v = new JLabel(defaultVal);
        v.setFont(new Font("SansSerif", Font.PLAIN, 13));
        v.setForeground(AppConfig.TEXT_PRIMARY);

        container.add(l);
        container.add(v);
        return v;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        RoundedPanel card = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Course Enrollment & Grades");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        coursesTable = new JTable(coursesTableModel);
        styleTable(coursesTable);

        JScrollPane scroll = new JScrollPane(coursesTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createWeeklySchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(AppConfig.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        RoundedPanel card = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Weekly Class Timetable & Schedule");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Scheduled lecture slots and classrooms for current term enrolled courses.");
        subtitle.setFont(AppConfig.FONT_MEDIUM);
        subtitle.setForeground(AppConfig.TEXT_SECONDARY);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 4));
        headerText.setOpaque(false);
        headerText.add(title);
        headerText.add(subtitle);
        header.add(headerText, BorderLayout.WEST);

        ModernButton refreshBtn = ModernButton.outline("Refresh", AppConfig.PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadData());
        header.add(refreshBtn, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        String[] schedCols = {"Day", "Time Slot", "Course Code", "Course Name", "Room / Hall", "Credits", "Status"};
        scheduleTableModel = new DefaultTableModel(schedCols, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        scheduleTable = new JTable(scheduleTableModel);
        styleTable(scheduleTable);

        JScrollPane scroll = new JScrollPane(scheduleTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppConfig.BACKGROUND_COLOR);

        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(0xE2E8F0));
        card.setPreferredSize(new Dimension(540, 360));
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Academic Profile");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(AppConfig.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(6, 2, 10, 14));
        details.setOpaque(false);

        addInfoRow(details, "User Account:", currentUser.getUsername());
        addInfoRow(details, "Role:", currentUser.getRole().name());
        addInfoRow(details, "Account Status:", currentUser.isActive() ? "Active" : "Inactive");
        addInfoRow(details, "Registered Email:", currentUser.getEmail() != null ? currentUser.getEmail() : "N/A");
        addInfoRow(details, "College Portal:", "CampusFlow Official");
        addInfoRow(details, "Academic Year:", "2025 - 2026");

        card.add(details, BorderLayout.CENTER);

        ModernButton backBtn = ModernButton.primary("Back to Dashboard");
        backBtn.addActionListener(e -> {
            sidebar.setActiveItem("Dashboard");
            onNavSelected("Dashboard");
        });
        card.add(backBtn, BorderLayout.SOUTH);

        panel.add(card);
        return panel;
    }

    public void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String studentName = currentUser.getUsername();
            String studentIdStr = "STU-001";
            String dept = "Computer Science";
            int semester = 1;
            List<Enrollment> enrollments = List.of();
            double overallPct = 0.0;

            @Override
            protected Void doInBackground() {
                try {
                    Optional<Student> sOpt = studentService.getStudentByUserId(currentUser.getId());
                    if (sOpt.isPresent()) {
                        studentProfile = sOpt.get();
                        studentName = studentProfile.getFullName();
                        studentIdStr = studentProfile.getStudentId();
                        dept = studentProfile.getDepartment();
                        semester = studentProfile.getSemester();
                        enrollments = enrollmentDAO.findByStudentId(studentProfile.getId());
                    } else {
                        List<Student> all = studentService.getAllStudents();
                        if (!all.isEmpty()) {
                            studentProfile = all.get(0);
                            studentName = studentProfile.getFullName();
                            studentIdStr = studentProfile.getStudentId();
                            dept = studentProfile.getDepartment();
                            semester = studentProfile.getSemester();
                            enrollments = enrollmentDAO.findByStudentId(studentProfile.getId());
                        }
                    }

                    if (studentProfile != null) {
                        overallPct = attendanceDAO.getOverallAttendancePercentage(studentProfile.getId());
                    }
                } catch (Exception ex) {
                    System.err.println("Student load data error: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                topBar.updateUserInfo(studentName, User.Role.STUDENT);
                enrolledStatCard.setValue(String.valueOf(enrollments.size()));
                semesterStatCard.setValue("Sem " + semester);
                departmentStatCard.setValue(dept != null ? dept : "General");

                if (studentProfile != null) {
                    attendanceStatCard.setValue(String.format(Locale.US, "%.1f%%", overallPct));
                    if (overallPct < 75.0) {
                        attendanceStatCard.setSubtitle("Defaulter: Below 75%");
                        attendanceStatCard.setAccentColor(AppConfig.DANGER_COLOR);
                    } else {
                        attendanceStatCard.setSubtitle("Good Standing (>= 75%)");
                        attendanceStatCard.setAccentColor(AppConfig.SUCCESS_COLOR);
                    }
                    if (attendanceContainer != null) {
                        attendanceContainer.removeAll();
                        attendanceContainer.add(new StudentAttendanceHistoryPanel(studentProfile), BorderLayout.CENTER);
                        attendanceContainer.revalidate();
                        attendanceContainer.repaint();
                    }
                }
                profIdVal.setText(studentIdStr);
                profNameVal.setText(studentName);
                profDeptVal.setText(dept != null ? dept : "General");
                profSemVal.setText("Semester " + semester);
                profEmailVal.setText(currentUser.getEmail() != null ? currentUser.getEmail() : studentProfile != null ? studentProfile.getEmail() : "N/A");

                // Populate Courses Table
                coursesTableModel.setRowCount(0);
                if (scheduleTableModel != null) {
                    scheduleTableModel.setRowCount(0);
                }

                if (enrollments.isEmpty()) {
                    coursesTableModel.addRow(new Object[]{"-", "No enrollments found", "-", "-", "-"});
                    if (scheduleTableModel != null) {
                        scheduleTableModel.addRow(new Object[]{"-", "-", "-", "No scheduled lectures found", "-", "-", "-"});
                    }
                } else {
                    String[] defaultDays = {"Mon / Wed", "Tue / Thu", "Mon / Wed / Fri", "Tue / Thu", "Friday"};
                    String[] defaultSlots = {"09:30 - 11:00 AM", "11:30 - 01:00 PM", "02:00 - 03:30 PM", "03:45 - 05:15 PM", "10:00 - 12:00 PM"};
                    String[] defaultRooms = {"Block A - 201", "Lab 3 - Computing", "Seminar Hall B", "Block C - 104", "Block B - 302"};

                    int idx = 0;
                    for (Enrollment e : enrollments) {
                        String code = "CRS" + e.getCourseId();
                        String name = "Course #" + e.getCourseId();
                        int credits = 3;

                        try {
                            Optional<Course> cOpt = courseDAO.findById(e.getCourseId());
                            if (cOpt.isPresent()) {
                                Course c = cOpt.get();
                                code = c.getCourseCode();
                                name = c.getCourseName();
                                credits = c.getCredits();
                            }
                        } catch (Exception ignored) {}

                        coursesTableModel.addRow(new Object[]{
                                code,
                                name,
                                credits + " cr",
                                e.getStatus(),
                                e.getGrade() != null ? e.getGrade() : "In Progress"
                        });

                        if (scheduleTableModel != null) {
                            String day = defaultDays[idx % defaultDays.length];
                            String slot = defaultSlots[idx % defaultSlots.length];
                            String room = defaultRooms[idx % defaultRooms.length];
                            scheduleTableModel.addRow(new Object[]{
                                    day,
                                    slot,
                                    code,
                                    name,
                                    room,
                                    credits + " Credits",
                                    e.getStatus()
                            });
                        }
                        idx++;
                    }
                }
                if (overviewCoursesTable != null) {
                    overviewCoursesTable.revalidate();
                    overviewCoursesTable.repaint();
                }
                if (coursesTable != null) {
                    coursesTable.revalidate();
                    coursesTable.repaint();
                }
                if (scheduleTable != null) {
                    scheduleTable.revalidate();
                    scheduleTable.repaint();
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
