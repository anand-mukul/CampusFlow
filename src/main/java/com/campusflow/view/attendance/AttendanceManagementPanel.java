package com.campusflow.view.attendance;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Attendance;
import com.campusflow.model.Course;
import com.campusflow.model.Enrollment;
import com.campusflow.model.Faculty;
import com.campusflow.model.Student;
import com.campusflow.model.User;
import com.campusflow.service.AttendanceService;
import com.campusflow.service.CourseService;
import com.campusflow.service.EnrollmentService;
import com.campusflow.service.FacultyService;
import com.campusflow.service.ServiceResult;
import com.campusflow.service.StudentService;
import com.campusflow.view.components.ModernButton;
import com.campusflow.view.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AttendanceManagementPanel extends JPanel {
    private final User currentUser;
    private final AttendanceService attendanceService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final StudentService studentService;
    private final FacultyService facultyService;

    private JComboBox<CourseItem> courseSelector;
    private JTextField dateField;
    private JLabel summaryLabel;
    private JTable table;
    private DefaultTableModel tableModel;

    private List<Student> enrolledStudents = new ArrayList<>();
    private final Map<Integer, String> currentStatusMap = new HashMap<>();

    public static class CourseItem {
        public final int id;
        public final String name;
        public CourseItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    public AttendanceManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        this.attendanceService = new AttendanceService();
        this.courseService = new CourseService();
        this.enrollmentService = new EnrollmentService();
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

        // Row 1: Title and Save Action
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Attendance Register");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        summaryLabel = new JLabel("Select course and date to record attendance");
        summaryLabel.setFont(AppConfig.FONT_MEDIUM);
        summaryLabel.setForeground(AppConfig.TEXT_SECONDARY);

        titlePanel.add(title);
        titlePanel.add(summaryLabel);
        topRow.add(titlePanel, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        ModernButton markAllPresentBtn = ModernButton.accent("Mark All Present");
        markAllPresentBtn.addActionListener(e -> setAllStatus("PRESENT"));

        ModernButton markAllAbsentBtn = ModernButton.danger("Mark All Absent");
        markAllAbsentBtn.addActionListener(e -> setAllStatus("ABSENT"));

        ModernButton saveBtn = ModernButton.primary("Save Attendance");
        saveBtn.addActionListener(e -> saveAttendance());

        actionBtns.add(markAllPresentBtn);
        actionBtns.add(markAllAbsentBtn);
        actionBtns.add(saveBtn);
        topRow.add(actionBtns, BorderLayout.EAST);
        controlCard.add(topRow, BorderLayout.NORTH);

        // Row 2: Course & Date Selector
        JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        selectorRow.setOpaque(false);

        selectorRow.add(new JLabel("Course:"));
        courseSelector = new JComboBox<>();
        courseSelector.setFont(AppConfig.FONT_MEDIUM);
        courseSelector.setBackground(Color.WHITE);
        courseSelector.setPreferredSize(new Dimension(300, 36));
        courseSelector.addActionListener(e -> loadSession());
        selectorRow.add(courseSelector);

        selectorRow.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField(LocalDate.now().toString(), 10);
        dateField.setFont(AppConfig.FONT_MEDIUM);
        dateField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        selectorRow.add(dateField);

        ModernButton loadBtn = ModernButton.secondary("Load Date");
        loadBtn.addActionListener(e -> loadSession());
        selectorRow.add(loadBtn);

        controlCard.add(selectorRow, BorderLayout.SOUTH);
        add(controlCard, BorderLayout.NORTH);

        // TABLE PANEL
        RoundedPanel tableCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(12, 16, 16, 16));

        String[] cols = {"Student ID", "Full Name", "Department", "Semester", "Cumulative Attendance", "Session Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 5; // Only status combo is editable
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(AppConfig.FONT_MEDIUM);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(0xF8FAFC));
        table.getTableHeader().setForeground(AppConfig.TEXT_SECONDARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        // Combo editor for Status column
        JComboBox<String> statusEditorCombo = new JComboBox<>(new String[]{"PRESENT", "ABSENT", "LEAVE"});
        statusEditorCombo.setFont(AppConfig.FONT_MEDIUM);
        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(statusEditorCombo));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadCourses() {
        courseSelector.removeAllItems();
        List<Course> courses;

        // If faculty, filter only their assigned courses
        if (currentUser != null && currentUser.getRole() == User.Role.FACULTY) {
            Optional<Faculty> facOpt = facultyService.getFacultyByUserId(currentUser.getId());
            if (facOpt.isPresent()) {
                courses = courseService.getCoursesByFacultyId(facOpt.get().getId());
            } else {
                courses = List.of();
            }
        } else {
            courses = courseService.getAllCourses();
        }

        for (Course c : courses) {
            courseSelector.addItem(new CourseItem(c.getId(), c.getCourseCode() + " - " + c.getCourseName()));
        }

        loadSession();
    }

    private void loadSession() {
        CourseItem selected = (CourseItem) courseSelector.getSelectedItem();
        tableModel.setRowCount(0);
        currentStatusMap.clear();
        enrolledStudents.clear();

        if (selected == null) {
            summaryLabel.setText("No course selected.");
            return;
        }

        Date date;
        try {
            date = Date.valueOf(dateField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Fetch enrolled students
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(selected.id);
        // 2. Fetch existing attendance for this course and date
        List<Attendance> existing = attendanceService.getAttendanceByCourseAndDate(selected.id, date);
        Map<Integer, String> existingMap = new HashMap<>();
        for (Attendance a : existing) {
            existingMap.put(a.getStudentId(), a.getStatus());
        }

        int presentCount = 0;
        int absentCount = 0;
        int leaveCount = 0;

        for (Enrollment e : enrollments) {
            Optional<Student> sOpt = studentService.getStudentById(e.getStudentId());
            if (sOpt.isEmpty()) continue;
            Student s = sOpt.get();
            enrolledStudents.add(s);

            String status = existingMap.getOrDefault(s.getId(), "PRESENT");
            currentStatusMap.put(s.getId(), status);

            if ("PRESENT".equals(status)) presentCount++;
            else if ("ABSENT".equals(status)) absentCount++;
            else if ("LEAVE".equals(status)) leaveCount++;

            double cumPct = attendanceService.getAttendancePercentage(s.getId(), selected.id);

            tableModel.addRow(new Object[]{
                    s.getStudentId(),
                    s.getFullName(),
                    s.getDepartment(),
                    "Sem " + s.getSemester(),
                    cumPct + "%",
                    status
            });
        }

        summaryLabel.setText(String.format("Session on %s: %d Enrolled | %d Present | %d Absent | %d Leave",
                date.toString(), enrolledStudents.size(), presentCount, absentCount, leaveCount));
    }

    private void setAllStatus(String status) {
        if (table.isEditing()) {
            TableCellEditor editor = table.getCellEditor();
            if (editor != null) editor.stopCellEditing();
        }
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            tableModel.setValueAt(status, r, 5);
        }
        updateSummaryCounts();
    }

    private void updateSummaryCounts() {
        int p = 0, a = 0, l = 0;
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            String st = (String) tableModel.getValueAt(r, 5);
            if ("PRESENT".equals(st)) p++;
            else if ("ABSENT".equals(st)) a++;
            else if ("LEAVE".equals(st)) l++;
        }
        summaryLabel.setText(String.format("Current count: %d Present | %d Absent | %d Leave (Unsaved)", p, a, l));
    }

    private void saveAttendance() {
        CourseItem selected = (CourseItem) courseSelector.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a course.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (table.isEditing()) {
            TableCellEditor editor = table.getCellEditor();
            if (editor != null) editor.stopCellEditing();
        }

        Date date;
        try {
            date = Date.valueOf(dateField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Attendance> records = new ArrayList<>();
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            if (r >= enrolledStudents.size()) break;
            Student s = enrolledStudents.get(r);
            String st = (String) tableModel.getValueAt(r, 5);

            Attendance att = new Attendance();
            att.setStudentId(s.getId());
            att.setCourseId(selected.id);
            att.setAttendanceDate(date);
            att.setStatus(st != null ? st : "PRESENT");
            records.add(att);
        }

        if (records.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No enrolled students to mark attendance for.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ServiceResult res = attendanceService.saveCourseAttendance(selected.id, date, records, currentUser);
        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Saved Successfully", JOptionPane.INFORMATION_MESSAGE);
            loadSession();
        } else {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Error Saving Attendance", JOptionPane.ERROR_MESSAGE);
        }
    }
}

