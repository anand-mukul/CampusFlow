package com.campusflow.view.student;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Student;
import com.campusflow.service.ServiceResult;
import com.campusflow.service.StudentService;
import com.campusflow.view.components.ModernButton;
import com.campusflow.view.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class StudentManagementPanel extends JPanel {
    private final StudentService studentService;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Student> currentList = new ArrayList<>();

    private JTextField searchField;
    private JComboBox<String> deptFilterCombo;
    private JComboBox<String> semFilterCombo;
    private JLabel countLabel;

    public StudentManagementPanel() {
        this.studentService = new StudentService();
        setLayout(new BorderLayout(0, 16));
        setBackground(AppConfig.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        loadStudents();
    }

    private void initUI() {
        // TOP CONTROL CARD: Search, Filters & Actions
        RoundedPanel controlCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        controlCard.setLayout(new BorderLayout(14, 14));
        controlCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Row 1: Title and Main Action Buttons
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Student Directory");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        countLabel = new JLabel("(0 students)");
        countLabel.setFont(AppConfig.FONT_MEDIUM);
        countLabel.setForeground(AppConfig.TEXT_SECONDARY);
        titlePanel.add(title);
        titlePanel.add(countLabel);
        topRow.add(titlePanel, BorderLayout.WEST);

        // Buttons
        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        ModernButton addBtn = ModernButton.primary("+ Add Student");
        addBtn.addActionListener(e -> openStudentForm(null));

        ModernButton editBtn = ModernButton.secondary("✎ Edit");
        editBtn.addActionListener(e -> editSelectedStudent());

        ModernButton profileBtn = ModernButton.accent("👁 View Profile");
        profileBtn.addActionListener(e -> viewSelectedProfile());

        ModernButton deleteBtn = ModernButton.danger("🗑 Delete");
        deleteBtn.addActionListener(e -> deleteSelectedStudent());

        ModernButton refreshBtn = ModernButton.outline("↻", AppConfig.PRIMARY_COLOR);
        refreshBtn.setToolTipText("Refresh Directory");
        refreshBtn.addActionListener(e -> loadStudents());

        actionBtns.add(addBtn);
        actionBtns.add(editBtn);
        actionBtns.add(profileBtn);
        actionBtns.add(deleteBtn);
        actionBtns.add(refreshBtn);
        topRow.add(actionBtns, BorderLayout.EAST);
        controlCard.add(topRow, BorderLayout.NORTH);

        // Row 2: Search Input and Filter Dropdowns
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterRow.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setFont(AppConfig.FONT_MEDIUM);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search by ID, name, or email...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        // Department filter
        JLabel deptLbl = new JLabel("Dept:");
        deptLbl.setFont(AppConfig.FONT_LABEL);
        deptLbl.setForeground(AppConfig.TEXT_SECONDARY);

        deptFilterCombo = new JComboBox<>(new String[]{"All", "Computer Science", "Electronics", "Mechanical", "Civil", "Information Technology"});
        deptFilterCombo.setFont(AppConfig.FONT_MEDIUM);
        deptFilterCombo.setPreferredSize(new Dimension(160, 34));
        deptFilterCombo.addActionListener(e -> applyFilters());

        // Semester filter
        JLabel semLbl = new JLabel("Sem:");
        semLbl.setFont(AppConfig.FONT_LABEL);
        semLbl.setForeground(AppConfig.TEXT_SECONDARY);

        semFilterCombo = new JComboBox<>(new String[]{"All", "Sem 1", "Sem 2", "Sem 3", "Sem 4", "Sem 5", "Sem 6", "Sem 7", "Sem 8"});
        semFilterCombo.setFont(AppConfig.FONT_MEDIUM);
        semFilterCombo.setPreferredSize(new Dimension(100, 34));
        semFilterCombo.addActionListener(e -> applyFilters());

        JButton clearFilterBtn = new JButton("Reset Filters");
        clearFilterBtn.setFont(AppConfig.FONT_SMALL);
        clearFilterBtn.addActionListener(e -> {
            searchField.setText("");
            deptFilterCombo.setSelectedIndex(0);
            semFilterCombo.setSelectedIndex(0);
            loadStudents();
        });

        filterRow.add(new JLabel("🔍"));
        filterRow.add(searchField);
        filterRow.add(deptLbl);
        filterRow.add(deptFilterCombo);
        filterRow.add(semLbl);
        filterRow.add(semFilterCombo);
        filterRow.add(clearFilterBtn);

        controlCard.add(filterRow, BorderLayout.SOUTH);
        add(controlCard, BorderLayout.NORTH);

        // CENTER: Data Table
        RoundedPanel tableCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(0, 0, 0, 0));

        String[] columns = {"ID", "Student ID", "Full Name", "Email Address", "Phone", "Department", "Sem", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(AppConfig.FONT_MEDIUM);
        table.setRowHeight(38);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xE0F2FE));
        table.setSelectionForeground(AppConfig.TEXT_PRIMARY);

        table.getTableHeader().setFont(AppConfig.FONT_LABEL);
        table.getTableHeader().setBackground(new Color(0xF8FAFC));
        table.getTableHeader().setForeground(AppConfig.TEXT_SECONDARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)));

        // Column alignment
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(60);
        table.getColumnModel().getColumn(6).setCellRenderer(center);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setCellRenderer(center);

        // Double click opens profile
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedProfile();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadStudents() {
        currentList = studentService.getAllStudents();
        renderTable(currentList);
    }

    private void applyFilters() {
        String query = searchField.getText().trim();
        String dept = (String) deptFilterCombo.getSelectedItem();
        int semIdx = semFilterCombo.getSelectedIndex();
        Integer sem = semIdx > 0 ? semIdx : null;

        currentList = studentService.searchStudents(query, dept, sem);
        renderTable(currentList);
    }

    private void renderTable(List<Student> students) {
        tableModel.setRowCount(0);
        countLabel.setText("(" + students.size() + " student" + (students.size() == 1 ? "" : "s") + ")");

        for (Student s : students) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getStudentId(),
                    s.getFullName(),
                    s.getEmail(),
                    s.getPhone() != null ? s.getPhone() : "-",
                    s.getDepartment(),
                    "Sem " + s.getSemester(),
                    s.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private Student getSelectedStudent() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) return null;
        return currentList.get(row);
    }

    private void openStudentForm(Student student) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        StudentFormDialog dialog = new StudentFormDialog(parent, student, studentService, this::loadStudents);
        dialog.setVisible(true);
    }

    private void editSelectedStudent() {
        Student s = getSelectedStudent();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        openStudentForm(s);
    }

    private void viewSelectedProfile() {
        Student s = getSelectedStudent();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a student to view their profile.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window parent = SwingUtilities.getWindowAncestor(this);
        StudentProfileDialog dialog = new StudentProfileDialog(parent, s);
        dialog.setVisible(true);
    }

    private void deleteSelectedStudent() {
        Student s = getSelectedStudent();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete student: " + s.getFullName() + " (" + s.getStudentId() + ")?\n" +
                "This action will also remove their portal user credentials.",
                "Confirm Student Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            ServiceResult res = studentService.deleteStudent(s.getId());
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

