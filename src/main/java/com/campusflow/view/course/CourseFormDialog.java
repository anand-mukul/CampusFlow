package com.campusflow.view.course;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Course;
import com.campusflow.model.Faculty;
import com.campusflow.service.CourseService;
import com.campusflow.service.FacultyService;
import com.campusflow.service.ServiceResult;
import com.campusflow.view.components.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class CourseFormDialog extends JDialog {
    private final CourseService courseService;
    private final FacultyService facultyService;
    private final Course existingCourse;
    private final Runnable onSuccess;

    private JTextField codeField;
    private JTextField nameField;
    private JTextArea descArea;
    private JSpinner creditsSpinner;
    private JComboBox<String> deptCombo;
    private JComboBox<FacultyItem> facultyCombo;
    private JSpinner maxStudentsSpinner;
    private JCheckBox activeCheckBox;
    private JLabel errorLabel;

    public CourseFormDialog(Window owner, Course existingCourse, CourseService courseService, Runnable onSuccess) {
        super(owner, existingCourse == null ? "Create New Course" : "Edit Course", ModalityType.APPLICATION_MODAL);
        this.existingCourse = existingCourse;
        this.courseService = courseService;
        this.facultyService = new FacultyService();
        this.onSuccess = onSuccess;

        setSize(540, 620);
        setResizable(false);
        setLocationRelativeTo(owner);
        initUI();
    }

    private static class FacultyItem {
        final int id;
        final String displayName;

        FacultyItem(int id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel(existingCourse == null ? "Create New Course" : "Update Course Details");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(existingCourse == null ? "Enter curriculum and faculty assignment details" : "Modify existing course parameters");
        subtitle.setFont(AppConfig.FONT_SMALL);
        subtitle.setForeground(AppConfig.TEXT_SECONDARY);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        headerPanel.setBorder(new EmptyBorder(0, 0, 14, 0));
        root.add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);

        // Error message label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppConfig.FONT_SMALL);
        errorLabel.setForeground(AppConfig.DANGER_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(errorLabel, gbc);
        gbc.gridwidth = 1;

        // Course Code
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(createLabel("Course Code *"), gbc);
        codeField = createTextField();
        codeField.putClientProperty("JTextField.placeholderText", "e.g. CS101, IT204");
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(codeField, gbc);

        // Course Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(createLabel("Course Name *"), gbc);
        nameField = createTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(nameField, gbc);

        // Department
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        form.add(createLabel("Department *"), gbc);
        String[] depts = {
                "Computer Science",
                "Information Technology",
                "Electronics & Communication",
                "Mechanical Engineering",
                "Civil Engineering",
                "Mathematics & Computing",
                "Applied Physics",
                "Business Administration"
        };
        deptCombo = new JComboBox<>(depts);
        deptCombo.setFont(AppConfig.FONT_MEDIUM);
        deptCombo.setBackground(Color.WHITE);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(deptCombo, gbc);

        // Credits & Max Students row
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        form.add(createLabel("Credits & Capacity *"), gbc);

        JPanel numPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        numPanel.setOpaque(false);
        creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        creditsSpinner.setPreferredSize(new Dimension(70, 32));

        maxStudentsSpinner = new JSpinner(new SpinnerNumberModel(60, 5, 300, 5));
        maxStudentsSpinner.setPreferredSize(new Dimension(80, 32));

        numPanel.add(new JLabel("Credits:"));
        numPanel.add(creditsSpinner);
        numPanel.add(Box.createHorizontalStrut(10));
        numPanel.add(new JLabel("Max Seats:"));
        numPanel.add(maxStudentsSpinner);

        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(numPanel, gbc);

        // Assigned Faculty
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        form.add(createLabel("Assigned Faculty"), gbc);

        facultyCombo = new JComboBox<>();
        facultyCombo.setFont(AppConfig.FONT_MEDIUM);
        facultyCombo.setBackground(Color.WHITE);
        facultyCombo.addItem(new FacultyItem(0, "-- (Unassigned) --"));
        List<Faculty> allFaculty = facultyService.getAllFaculty();
        for (Faculty f : allFaculty) {
            facultyCombo.addItem(new FacultyItem(f.getId(), f.getFullName() + " (" + f.getDepartment() + ")"));
        }
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(facultyCombo, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        form.add(createLabel("Description"), gbc);
        descArea = new JTextArea(3, 20);
        descArea.setFont(AppConfig.FONT_MEDIUM);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(200, 75));
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(descScroll, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.3;
        form.add(createLabel("Status"), gbc);
        activeCheckBox = new JCheckBox("Course is Active & Available for Enrollment", true);
        activeCheckBox.setFont(AppConfig.FONT_MEDIUM);
        activeCheckBox.setOpaque(false);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(activeCheckBox, gbc);

        root.add(form, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = ModernButton.primary(existingCourse == null ? "Create Course" : "Save Changes");
        saveBtn.addActionListener(e -> saveCourse());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        root.add(footer, BorderLayout.SOUTH);

        // Populate fields if editing
        if (existingCourse != null) {
            codeField.setText(existingCourse.getCourseCode());
            nameField.setText(existingCourse.getCourseName());
            if (existingCourse.getDescription() != null) descArea.setText(existingCourse.getDescription());
            creditsSpinner.setValue(existingCourse.getCredits());
            maxStudentsSpinner.setValue(existingCourse.getMaxStudents());
            if (existingCourse.getDepartment() != null) deptCombo.setSelectedItem(existingCourse.getDepartment());

            for (int i = 0; i < facultyCombo.getItemCount(); i++) {
                if (facultyCombo.getItemAt(i).id == existingCourse.getFacultyId()) {
                    facultyCombo.setSelectedIndex(i);
                    break;
                }
            }
            activeCheckBox.setSelected(existingCourse.isActive());
        }

        setContentPane(root);
    }

    private void saveCourse() {
        errorLabel.setText(" ");
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String dept = (String) deptCombo.getSelectedItem();
        int credits = (int) creditsSpinner.getValue();
        int maxStudents = (int) maxStudentsSpinner.getValue();
        FacultyItem selectedFaculty = (FacultyItem) facultyCombo.getSelectedItem();
        int facultyId = selectedFaculty != null ? selectedFaculty.id : 0;
        String desc = descArea.getText().trim();
        boolean active = activeCheckBox.isSelected();

        if (code.isEmpty() || name.isEmpty()) {
            errorLabel.setText("Please enter Course Code and Course Name.");
            return;
        }

        Course c = existingCourse != null ? existingCourse : new Course();
        c.setCourseCode(code);
        c.setCourseName(name);
        c.setDepartment(dept);
        c.setCredits(credits);
        c.setMaxStudents(maxStudents);
        c.setFacultyId(facultyId);
        c.setDescription(desc.isEmpty() ? null : desc);
        c.setActive(active);

        ServiceResult res;
        if (existingCourse == null) {
            res = courseService.createCourse(c);
        } else {
            res = courseService.updateCourse(c);
        }

        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            errorLabel.setText(res.getMessage());
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(AppConfig.TEXT_PRIMARY);
        return label;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(AppConfig.FONT_MEDIUM);
        tf.setPreferredSize(new Dimension(200, 34));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        return tf;
    }
}

