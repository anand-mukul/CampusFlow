package com.campusflow.view.enrollment;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Course;
import com.campusflow.model.Enrollment;
import com.campusflow.model.Faculty;
import com.campusflow.model.Student;
import com.campusflow.model.User;
import com.campusflow.service.CourseService;
import com.campusflow.service.EnrollmentService;
import com.campusflow.service.FacultyService;
import com.campusflow.service.ServiceResult;
import com.campusflow.service.StudentService;
import com.campusflow.view.components.ModernButton;
import com.campusflow.view.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnrollmentManagementPanel extends JPanel {
    private final User currentUser;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final StudentService studentService;
    private final FacultyService facultyService;

    private JComboBox<CourseItem> courseSelector;
    private JLabel courseStatsLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Enrollment> currentEnrollments = new ArrayList<>();
    private Map<Integer, Student> studentMap;

    public static class CourseItem {
        public final int id;
        public final String label;
        public CourseItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    public EnrollmentManagementPanel() {
        this(null);
    }

    public EnrollmentManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        this.enrollmentService = new EnrollmentService();
        this.courseService = new CourseService();
        this.studentService = new StudentService();
        this.facultyService = new FacultyService();

        setLayout(new BorderLayout(0, 16));
        setBackground(AppConfig.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        loadCourses();
    }

    private void initUI() {
        // TOP CONTROL CARD
        RoundedPanel controlCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        controlCard.setLayout(new BorderLayout(14, 14));
        controlCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Row: Title + Action buttons
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Course Enrollments");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        courseStatsLabel = new JLabel("(0 students enrolled)");
        courseStatsLabel.setFont(AppConfig.FONT_MEDIUM);
        courseStatsLabel.setForeground(AppConfig.TEXT_SECONDARY);

        titlePanel.add(title);
        titlePanel.add(courseStatsLabel);
        topRow.add(titlePanel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        ModernButton enrollBtn = ModernButton.primary("+ Enroll Student");
        enrollBtn.addActionListener(e -> openEnrollDialog());

        ModernButton updateBtn = ModernButton.secondary("✎ Update Status");
        updateBtn.addActionListener(e -> updateSelectedStatus());

        ModernButton dropBtn = ModernButton.danger("🗑 Drop");
        dropBtn.addActionListener(e -> dropSelectedStudent());

        ModernButton refreshBtn = ModernButton.outline("↻", AppConfig.PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadEnrollmentsForSelectedCourse());

        actions.add(enrollBtn);
        actions.add(updateBtn);
        actions.add(dropBtn);
        actions.add(refreshBtn);
        topRow.add(actions, BorderLayout.EAST);
        controlCard.add(topRow, BorderLayout.NORTH);

        // Filter / Selector Row
        JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        selectorRow.setOpaque(false);

        JLabel selectLabel = new JLabel("Select Course:");
        selectLabel.setFont(AppConfig.FONT_MEDIUM);
        selectLabel.setForeground(AppConfig.TEXT_PRIMARY);

        courseSelector = new JComboBox<>();
        courseSelector.setFont(AppConfig.FONT_MEDIUM);
        courseSelector.setBackground(Color.WHITE);
        courseSelector.setPreferredSize(new Dimension(320, 36));
        courseSelector.addActionListener(e -> loadEnrollmentsForSelectedCourse());

        selectorRow.add(selectLabel);
        selectorRow.add(courseSelector);

        controlCard.add(selectorRow, BorderLayout.SOUTH);
        add(controlCard, BorderLayout.NORTH);

        // TABLE CARD
        RoundedPanel tableCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(12, 16, 16, 16));

        String[] cols = {"Student ID", "Full Name", "Department", "Semester", "Enrollment Date", "Status", "Grade"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(38);
        table.setFont(AppConfig.FONT_MEDIUM);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(0xF8FAFC));
        table.getTableHeader().setForeground(AppConfig.TEXT_SECONDARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadCourses() {
        // Cache student map
        List<Student> students = studentService.getAllStudents();
        studentMap = students.stream().collect(Collectors.toMap(Student::getId, s -> s, (a, b) -> a));

        courseSelector.removeAllItems();
        List<Course> courses;
        if (currentUser != null && currentUser.getRole() == User.Role.FACULTY) {
            Optional<Faculty> facOpt = facultyService.getFacultyByUserId(currentUser.getId());
            courses = facOpt.map(f -> courseService.getCoursesByFacultyId(f.getId())).orElse(List.of());
        } else {
            courses = courseService.getAllCourses();
        }
        for (Course c : courses) {
            courseSelector.addItem(new CourseItem(c.getId(), c.getCourseCode() + " - " + c.getCourseName()));
        }

        loadEnrollmentsForSelectedCourse();
    }

    private void loadEnrollmentsForSelectedCourse() {
        CourseItem selectedCourse = (CourseItem) courseSelector.getSelectedItem();
        tableModel.setRowCount(0);
        if (selectedCourse == null) {
            courseStatsLabel.setText("(0 students)");
            return;
        }

        currentEnrollments = enrollmentService.getEnrollmentsByCourse(selectedCourse.id);
        Optional<Course> cOpt = courseService.getCourseById(selectedCourse.id);
        int max = cOpt.map(Course::getMaxStudents).orElse(60);

        for (Enrollment e : currentEnrollments) {
            Student s = studentMap != null ? studentMap.get(e.getStudentId()) : null;
            tableModel.addRow(new Object[]{
                    s != null ? s.getStudentId() : "ID:" + e.getStudentId(),
                    s != null ? s.getFullName() : "Unknown",
                    s != null ? s.getDepartment() : "-",
                    s != null ? "Sem " + s.getSemester() : "-",
                    e.getEnrollmentDate() != null ? e.getEnrollmentDate().toString() : "-",
                    e.getStatus(),
                    e.getGrade() != null ? e.getGrade() : "-"
            });
        }

        courseStatsLabel.setText("(" + currentEnrollments.size() + " / " + max + " enrolled)");
    }

    private void openEnrollDialog() {
        CourseItem selectedCourse = (CourseItem) courseSelector.getSelectedItem();
        int courseId = selectedCourse != null ? selectedCourse.id : 0;
        Window parent = SwingUtilities.getWindowAncestor(this);
        EnrollStudentDialog dialog = new EnrollStudentDialog(parent, courseId, currentUser, this::loadEnrollmentsForSelectedCourse);
        dialog.setVisible(true);
    }

    private Enrollment getSelectedEnrollment() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentEnrollments.size()) return null;
        return currentEnrollments.get(row);
    }

    private void dropSelectedStudent() {
        Enrollment e = getSelectedEnrollment();
        if (e == null) {
            JOptionPane.showMessageDialog(this, "Please select an enrollment record to drop.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student s = studentMap != null ? studentMap.get(e.getStudentId()) : null;
        String name = s != null ? s.getFullName() : "Student #" + e.getStudentId();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to drop " + name + " from this course?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            ServiceResult res = enrollmentService.dropStudent(e.getStudentId(), e.getCourseId());
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
                loadEnrollmentsForSelectedCourse();
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateSelectedStatus() {
        Enrollment e = getSelectedEnrollment();
        if (e == null) {
            JOptionPane.showMessageDialog(this, "Please select an enrollment record to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] statuses = {"ACTIVE", "COMPLETED", "DROPPED"};
        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Select Enrollment Status:",
                "Update Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                e.getStatus()
        );

        if (newStatus == null) return;

        String newGrade = JOptionPane.showInputDialog(this, "Enter Grade (e.g. A, B+, Pass, or leave blank):", e.getGrade() != null ? e.getGrade() : "");
        if (newGrade != null && newGrade.isBlank()) newGrade = null;

        ServiceResult res = enrollmentService.updateStatusAndGrade(e.getStudentId(), e.getCourseId(), newStatus, newGrade);
        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Updated", JOptionPane.INFORMATION_MESSAGE);
            loadEnrollmentsForSelectedCourse();
        } else {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

