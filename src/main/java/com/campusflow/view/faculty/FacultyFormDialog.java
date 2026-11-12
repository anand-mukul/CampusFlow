package com.campusflow.view.faculty;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Faculty;
import com.campusflow.service.FacultyService;
import com.campusflow.service.ServiceResult;
import com.campusflow.view.components.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class FacultyFormDialog extends JDialog {
    private final FacultyService facultyService;
    private final Faculty existingFaculty;
    private final Runnable onSuccess;

    private JTextField facultyIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> departmentCombo;
    private JComboBox<String> designationCombo;
    private JPasswordField passwordField;
    private JCheckBox activeCheckBox;
    private JLabel errorLabel;

    public FacultyFormDialog(Window owner, Faculty existingFaculty, FacultyService facultyService, Runnable onSuccess) {
        super(owner, existingFaculty == null ? "Register New Faculty" : "Edit Faculty Details", ModalityType.APPLICATION_MODAL);
        this.existingFaculty = existingFaculty;
        this.facultyService = facultyService;
        this.onSuccess = onSuccess;

        setSize(520, 580);
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
        JLabel title = new JLabel(existingFaculty == null ? "Register New Faculty" : "Update Faculty Profile");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(existingFaculty == null ? "Enter faculty contact, designation and departmental details" : "Modify existing records");
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

        // Faculty ID row
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(createLabel("Faculty ID *"), gbc);

        JPanel idPanel = new JPanel(new BorderLayout(8, 0));
        idPanel.setOpaque(false);
        facultyIdField = createTextField();
        idPanel.add(facultyIdField, BorderLayout.CENTER);
        if (existingFaculty == null) {
            JButton genBtn = new JButton("Auto");
            genBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            genBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            genBtn.addActionListener(e -> facultyIdField.setText(facultyService.generateNextFacultyId()));
            idPanel.add(genBtn, BorderLayout.EAST);
        } else {
            facultyIdField.setEditable(false);
            facultyIdField.setBackground(new Color(0xF1F5F9));
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
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        form.add(createLabel("Last Name *"), gbc);
        lastNameField = createTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(lastNameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        form.add(createLabel("Email Address *"), gbc);
        emailField = createTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(emailField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        form.add(createLabel("Phone Number"), gbc);
        phoneField = createTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(phoneField, gbc);

        // Department
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
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
        departmentCombo = new JComboBox<>(depts);
        departmentCombo.setFont(AppConfig.FONT_REGULAR);
        departmentCombo.setBackground(Color.WHITE);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(departmentCombo, gbc);

        // Designation
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.3;
        form.add(createLabel("Designation *"), gbc);
        String[] designations = {
                "Professor",
                "Associate Professor",
                "Assistant Professor",
                "Head of Department",
                "Adjunct Lecturer",
                "Research Fellow"
        };
        designationCombo = new JComboBox<>(designations);
        designationCombo.setFont(AppConfig.FONT_REGULAR);
        designationCombo.setBackground(Color.WHITE);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(designationCombo, gbc);

        // Password (only for new registrations)
        int currentGridY = 8;
        if (existingFaculty == null) {
            gbc.gridx = 0; gbc.gridy = currentGridY; gbc.weightx = 0.3;
            form.add(createLabel("Initial Password"), gbc);
            passwordField = new JPasswordField("faculty123");
            passwordField.setFont(AppConfig.FONT_REGULAR);
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0xCBD5E1), 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
            gbc.gridx = 1; gbc.weightx = 0.7;
            form.add(passwordField, gbc);
            currentGridY++;
        }

        // Active Status
        gbc.gridx = 0; gbc.gridy = currentGridY; gbc.weightx = 0.3;
        form.add(createLabel("Status"), gbc);
        activeCheckBox = new JCheckBox("Active Faculty Member", true);
        activeCheckBox.setFont(AppConfig.FONT_REGULAR);
        activeCheckBox.setOpaque(false);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(activeCheckBox, gbc);

        root.add(form, BorderLayout.CENTER);

        // Actions Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = ModernButton.primary(existingFaculty == null ? "Register Faculty" : "Save Changes");
        saveBtn.addActionListener(e -> saveFaculty());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        root.add(footer, BorderLayout.SOUTH);

        // Populate if editing
        if (existingFaculty != null) {
            facultyIdField.setText(existingFaculty.getFacultyId());
            firstNameField.setText(existingFaculty.getFirstName());
            lastNameField.setText(existingFaculty.getLastName());
            emailField.setText(existingFaculty.getEmail());
            phoneField.setText(existingFaculty.getPhone() != null ? existingFaculty.getPhone() : "");
            if (existingFaculty.getDepartment() != null) {
                departmentCombo.setSelectedItem(existingFaculty.getDepartment());
            }
            if (existingFaculty.getDesignation() != null) {
                designationCombo.setSelectedItem(existingFaculty.getDesignation());
            }
            activeCheckBox.setSelected(existingFaculty.isActive());
        } else {
            facultyIdField.setText(facultyService.generateNextFacultyId());
        }

        setContentPane(root);
    }

    private void saveFaculty() {
        errorLabel.setText(" ");
        String fId = facultyIdField.getText().trim();
        String fName = firstNameField.getText().trim();
        String lName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String dept = (String) departmentCombo.getSelectedItem();
        String designation = (String) designationCombo.getSelectedItem();
        boolean active = activeCheckBox.isSelected();

        if (fId.isEmpty() || fName.isEmpty() || lName.isEmpty() || email.isEmpty()) {
            errorLabel.setText("Please fill in all mandatory fields (*)");
            return;
        }

        Faculty f = existingFaculty != null ? existingFaculty : new Faculty();
        f.setFacultyId(fId);
        f.setFirstName(fName);
        f.setLastName(lName);
        f.setEmail(email);
        f.setPhone(phone.isEmpty() ? null : phone);
        f.setDepartment(dept);
        f.setDesignation(designation);
        f.setActive(active);

        ServiceResult res;
        if (existingFaculty == null) {
            String pwd = passwordField != null ? new String(passwordField.getPassword()).trim() : "faculty123";
            res = facultyService.registerFaculty(f, pwd);
        } else {
            res = facultyService.updateFaculty(f);
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
        tf.setFont(AppConfig.FONT_REGULAR);
        tf.setPreferredSize(new Dimension(200, 34));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        return tf;
    }
}
