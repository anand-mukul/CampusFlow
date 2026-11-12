package com.campusflow.view.student;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Student;
import com.campusflow.service.ServiceResult;
import com.campusflow.service.StudentService;
import com.campusflow.view.components.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Date;

public class StudentFormDialog extends JDialog {
    private final StudentService studentService;
    private final Student existingStudent;
    private final Runnable onSuccess;

    private JTextField studentIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> departmentCombo;
    private JSpinner semesterSpinner;
    private JPasswordField passwordField;
    private JCheckBox activeCheckBox;
    private JLabel errorLabel;

    public StudentFormDialog(Window owner, Student existingStudent, StudentService studentService, Runnable onSuccess) {
        super(owner, existingStudent == null ? "Register New Student" : "Edit Student Details", ModalityType.APPLICATION_MODAL);
        this.existingStudent = existingStudent;
        this.studentService = studentService;
        this.onSuccess = onSuccess;

        setSize(520, 600);
        setResizable(false);
        setLocationRelativeTo(owner);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel(existingStudent == null ? "Register New Student" : "Update Student Profile");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(existingStudent == null ? "Enter student identity, contact and academic details" : "Modify existing records");
        subtitle.setFont(AppConfig.FONT_SMALL);
        subtitle.setForeground(AppConfig.TEXT_SECONDARY);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));
        root.add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Error message label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppConfig.FONT_SMALL);
        errorLabel.setForeground(AppConfig.DANGER_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(errorLabel, gbc);
        gbc.gridwidth = 1;

        // Student ID row
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(createLabel("Student ID *"), gbc);

        JPanel idPanel = new JPanel(new BorderLayout(8, 0));
        idPanel.setOpaque(false);
        studentIdField = createTextField();
        idPanel.add(studentIdField, BorderLayout.CENTER);
        if (existingStudent == null) {
            JButton genBtn = new JButton("Auto");
            genBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            genBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            genBtn.addActionListener(e -> studentIdField.setText(studentService.generateNextStudentId()));
            idPanel.add(genBtn, BorderLayout.EAST);
        } else {
            studentIdField.setEditable(false);
            studentIdField.setBackground(new Color(0xF1F5F9));
        }
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(idPanel, gbc);

        // First Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(createLabel("First Name *"), gbc);
        firstNameField = createTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(createLabel("Last Name *"), gbc);
        lastNameField = createTextField();
        gbc.gridx = 1;
        form.add(lastNameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 4;
        form.add(createLabel("Email Address *"), gbc);
        emailField = createTextField();
        gbc.gridx = 1;
        form.add(emailField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 5;
        form.add(createLabel("Phone Number"), gbc);
        phoneField = createTextField();
        gbc.gridx = 1;
        form.add(phoneField, gbc);

        // Department
        gbc.gridx = 0; gbc.gridy = 6;
        form.add(createLabel("Department *"), gbc);
        String[] depts = {"Computer Science", "Electronics", "Mechanical", "Civil", "Information Technology", "Electrical"};
        departmentCombo = new JComboBox<>(depts);
        departmentCombo.setFont(AppConfig.FONT_MEDIUM);
        gbc.gridx = 1;
        form.add(departmentCombo, gbc);

        // Semester
        gbc.gridx = 0; gbc.gridy = 7;
        form.add(createLabel("Semester (1-8) *"), gbc);
        semesterSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        semesterSpinner.setFont(AppConfig.FONT_MEDIUM);
        gbc.gridx = 1;
        form.add(semesterSpinner, gbc);

        // Password (only for new students)
        if (existingStudent == null) {
            gbc.gridx = 0; gbc.gridy = 8;
            form.add(createLabel("Login Password"), gbc);
            passwordField = new JPasswordField("student123");
            passwordField.setFont(AppConfig.FONT_MEDIUM);
            passwordField.setBorder(new LineBorder(new Color(0xCBD5E1), 1, true));
            passwordField.setPreferredSize(new Dimension(0, 34));
            gbc.gridx = 1;
            form.add(passwordField, gbc);
        }

        // Active Status
        gbc.gridx = 0; gbc.gridy = 9;
        form.add(createLabel("Status"), gbc);
        activeCheckBox = new JCheckBox("Active Student Account", true);
        activeCheckBox.setFont(AppConfig.FONT_MEDIUM);
        activeCheckBox.setOpaque(false);
        gbc.gridx = 1;
        form.add(activeCheckBox, gbc);

        root.add(form, BorderLayout.CENTER);

        // Bottom Actions
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        buttonBar.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(AppConfig.FONT_MEDIUM);
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = ModernButton.primary(existingStudent == null ? "Save Student" : "Update Student");
        saveBtn.addActionListener(e -> saveStudent());

        buttonBar.add(cancelBtn);
        buttonBar.add(saveBtn);
        root.add(buttonBar, BorderLayout.SOUTH);

        // Populate fields if edit mode
        if (existingStudent != null) {
            studentIdField.setText(existingStudent.getStudentId());
            firstNameField.setText(existingStudent.getFirstName());
            lastNameField.setText(existingStudent.getLastName());
            emailField.setText(existingStudent.getEmail());
            phoneField.setText(existingStudent.getPhone());
            departmentCombo.setSelectedItem(existingStudent.getDepartment());
            semesterSpinner.setValue(existingStudent.getSemester());
            activeCheckBox.setSelected(existingStudent.isActive());
        } else {
            studentIdField.setText(studentService.generateNextStudentId());
        }

        setContentPane(root);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppConfig.FONT_LABEL);
        label.setForeground(AppConfig.TEXT_PRIMARY);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(AppConfig.FONT_MEDIUM);
        field.setBorder(new LineBorder(new Color(0xCBD5E1), 1, true));
        field.setPreferredSize(new Dimension(0, 34));
        return field;
    }

    private void saveStudent() {
        errorLabel.setText(" ");
        String sid = studentIdField.getText().trim();
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String dept = (String) departmentCombo.getSelectedItem();
        int sem = (int) semesterSpinner.getValue();
        boolean active = activeCheckBox.isSelected();

        if (sid.isEmpty() || first.isEmpty() || last.isEmpty() || email.isEmpty()) {
            errorLabel.setText("Please fill in all required fields (*).");
            return;
        }

        ServiceResult res;
        if (existingStudent == null) {
            Student s = new Student();
            s.setStudentId(sid);
            s.setFirstName(first);
            s.setLastName(last);
            s.setEmail(email);
            s.setPhone(phone);
            s.setDepartment(dept);
            s.setSemester(sem);
            s.setActive(active);
            s.setEnrollmentDate(new Date(System.currentTimeMillis()));

            String pass = new String(passwordField.getPassword()).trim();
            res = studentService.registerStudent(s, pass);
        } else {
            existingStudent.setStudentId(sid);
            existingStudent.setFirstName(first);
            existingStudent.setLastName(last);
            existingStudent.setEmail(email);
            existingStudent.setPhone(phone);
            existingStudent.setDepartment(dept);
            existingStudent.setSemester(sem);
            existingStudent.setActive(active);

            res = studentService.updateStudent(existingStudent);
        }

        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            errorLabel.setText(res.getMessage());
        }
    }
}

