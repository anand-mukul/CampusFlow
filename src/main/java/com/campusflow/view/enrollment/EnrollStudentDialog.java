package com.campusflow.view.enrollment;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Course;
import com.campusflow.model.Faculty;
import com.campusflow.model.Student;
import com.campusflow.model.User;
import com.campusflow.service.CourseService;
import com.campusflow.service.EnrollmentService;
import com.campusflow.service.FacultyService;
import com.campusflow.service.ServiceResult;
import com.campusflow.service.StudentService;
import com.campusflow.view.components.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class EnrollStudentDialog extends JDialog {
    private final User currentUser;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final StudentService studentService;
    private final FacultyService facultyService;
    private final int preselectedCourseId;
    private final Runnable onSuccess;

    private JComboBox<CourseItem> courseCombo;
    private JComboBox<StudentItem> studentCombo;
    private JLabel errorLabel;

    public static class CourseItem {
        public final int id;
        public final String name;
        public CourseItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    public static class StudentItem {
        public final int id;
        public final String name;
        public StudentItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    public EnrollStudentDialog(Window owner, int courseId, Runnable onSuccess) {
        this(owner, courseId, null, onSuccess);
    }

    public EnrollStudentDialog(Window owner, int courseId, User currentUser, Runnable onSuccess) {
        super(owner, "Enroll Student in Course", ModalityType.APPLICATION_MODAL);
        this.currentUser = currentUser;
        this.preselectedCourseId = courseId;
        this.onSuccess = onSuccess;
        this.enrollmentService = new EnrollmentService();
        this.courseService = new CourseService();
        this.studentService = new StudentService();
        this.facultyService = new FacultyService();

        setSize(500, 320);
        setResizable(false);
        setLocationRelativeTo(owner);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Enroll Student");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Assign an active student to an academic course");
        subtitle.setFont(AppConfig.FONT_SMALL);
        subtitle.setForeground(AppConfig.TEXT_SECONDARY);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setOpaque(false);

        form.add(new JLabel("Target Course:"));
        courseCombo = new JComboBox<>();
        courseCombo.setFont(AppConfig.FONT_MEDIUM);
        courseCombo.setBackground(Color.WHITE);
        List<Course> courses;
        if (currentUser != null && currentUser.getRole() == User.Role.FACULTY) {
            Optional<Faculty> facOpt = facultyService.getFacultyByUserId(currentUser.getId());
            courses = facOpt.map(f -> courseService.getCoursesByFacultyId(f.getId())).orElse(List.of());
        } else {
            courses = courseService.getAllCourses();
        }
        int selectedIndex = 0;
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            courseCombo.addItem(new CourseItem(c.getId(), c.getCourseCode() + " - " + c.getCourseName()));
            if (c.getId() == preselectedCourseId) selectedIndex = i;
        }
        if (!courses.isEmpty()) courseCombo.setSelectedIndex(selectedIndex);
        form.add(courseCombo);

        form.add(new JLabel("Select Student:"));
        studentCombo = new JComboBox<>();
        studentCombo.setFont(AppConfig.FONT_MEDIUM);
        studentCombo.setBackground(Color.WHITE);
        List<Student> students = studentService.getAllStudents();
        for (Student s : students) {
            studentCombo.addItem(new StudentItem(s.getId(), s.getStudentId() + " : " + s.getFullName() + " (" + s.getDepartment() + ")"));
        }
        form.add(studentCombo);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppConfig.FONT_SMALL);
        errorLabel.setForeground(AppConfig.DANGER_COLOR);
        form.add(new JLabel(""));
        form.add(errorLabel);

        root.add(form, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton enrollBtn = ModernButton.primary("Confirm Enrollment");
        enrollBtn.addActionListener(e -> executeEnrollment());

        footer.add(cancelBtn);
        footer.add(enrollBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void executeEnrollment() {
        errorLabel.setText(" ");
        CourseItem cItem = (CourseItem) courseCombo.getSelectedItem();
        StudentItem sItem = (StudentItem) studentCombo.getSelectedItem();

        if (cItem == null || sItem == null) {
            errorLabel.setText("Please select both a course and a student.");
            return;
        }

        ServiceResult res = enrollmentService.enrollStudent(sItem.id, cItem.id, currentUser);
        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Enrolled", JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            errorLabel.setText(res.getMessage());
        }
    }
}

