package com.campusflow.view.course;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Course;
import com.campusflow.model.Faculty;
import com.campusflow.service.CourseService;
import com.campusflow.service.FacultyService;
import com.campusflow.service.ServiceResult;
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
import java.util.Map;
import java.util.stream.Collectors;

public class CourseManagementPanel extends JPanel {
    private final CourseService courseService;
    private final FacultyService facultyService;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Course> currentList = new ArrayList<>();
    private Map<Integer, String> facultyMap;

    private JTextField searchField;
    private JComboBox<String> deptFilterCombo;
    private JLabel countLabel;

    public CourseManagementPanel() {
        this.courseService = new CourseService();
        this.facultyService = new FacultyService();

        setLayout(new BorderLayout(0, 16));
        setBackground(AppConfig.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        loadCourses();
    }

    private void initUI() {
        // CONTROL CARD
        RoundedPanel controlCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        controlCard.setLayout(new BorderLayout(14, 14));
        controlCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Row: Title + Action Buttons
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Course Catalog");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        countLabel = new JLabel("(0 courses)");
        countLabel.setFont(AppConfig.FONT_MEDIUM);
        countLabel.setForeground(AppConfig.TEXT_SECONDARY);
        titlePanel.add(title);
        titlePanel.add(countLabel);
        topRow.add(titlePanel, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        ModernButton addBtn = ModernButton.primary("Add Course");
        addBtn.addActionListener(e -> openCourseForm(null));

        ModernButton editBtn = ModernButton.secondary("Edit Course");
        editBtn.addActionListener(e -> editSelectedCourse());

        ModernButton deleteBtn = ModernButton.danger("Delete");
        deleteBtn.addActionListener(e -> deleteSelectedCourse());

        ModernButton refreshBtn = ModernButton.outline("Refresh", AppConfig.PRIMARY_COLOR);
        refreshBtn.setToolTipText("Refresh Course List");
        refreshBtn.addActionListener(e -> loadCourses());

        actionBtns.add(addBtn);
        actionBtns.add(editBtn);
        actionBtns.add(deleteBtn);
        actionBtns.add(refreshBtn);
        topRow.add(actionBtns, BorderLayout.EAST);
        controlCard.add(topRow, BorderLayout.NORTH);

        // Filter Row: Search & Department
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterRow.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setFont(AppConfig.FONT_MEDIUM);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        searchField.putClientProperty("JTextField.placeholderText", "Search by code or title...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        JLabel filterLabel = new JLabel("Department:");
        filterLabel.setFont(AppConfig.FONT_MEDIUM);
        filterLabel.setForeground(AppConfig.TEXT_SECONDARY);

        deptFilterCombo = new JComboBox<>();
        deptFilterCombo.setFont(AppConfig.FONT_MEDIUM);
        deptFilterCombo.setBackground(Color.WHITE);
        deptFilterCombo.addItem("All Departments");
        deptFilterCombo.addActionListener(e -> applyFilters());

        ModernButton clearBtn = ModernButton.outline("Clear Filters", AppConfig.TEXT_SECONDARY);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            deptFilterCombo.setSelectedIndex(0);
            applyFilters();
        });

        filterRow.add(new JLabel("Search:"));
        filterRow.add(searchField);
        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(filterLabel);
        filterRow.add(deptFilterCombo);
        filterRow.add(clearBtn);

        controlCard.add(filterRow, BorderLayout.SOUTH);
        add(controlCard, BorderLayout.NORTH);

        // TABLE PANEL
        RoundedPanel tableCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(12, 16, 16, 16));

        String[] cols = { "Code", "Course Name", "Department", "Credits", "Assigned Faculty", "Enrollment", "Status" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
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
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedCourse();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadCourses() {
        // Cache faculty map
        List<Faculty> allFaculty = facultyService.getAllFaculty();
        facultyMap = allFaculty.stream().collect(Collectors.toMap(Faculty::getId, Faculty::getFullName, (a, b) -> a));

        populateDepartmentFilter();
        applyFilters();
    }

    private void populateDepartmentFilter() {
        String current = (String) deptFilterCombo.getSelectedItem();
        deptFilterCombo.removeAllItems();
        deptFilterCombo.addItem("All Departments");
        List<String> depts = courseService.getDepartments();
        for (String d : depts) {
            deptFilterCombo.addItem(d);
        }
        if (current != null) {
            deptFilterCombo.setSelectedItem(current);
        }
    }

    private void applyFilters() {
        String kw = searchField.getText().trim();
        String dept = (String) deptFilterCombo.getSelectedItem();
        if ("All Departments".equals(dept))
            dept = null;

        currentList = courseService.searchCourses(kw, dept);
        tableModel.setRowCount(0);

        for (Course c : currentList) {
            String facName = "-- Unassigned --";
            if (c.getFacultyId() > 0 && facultyMap != null && facultyMap.containsKey(c.getFacultyId())) {
                facName = facultyMap.get(c.getFacultyId());
            }

            int enrolled = courseService.getEnrolledCount(c.getId());
            String enrollmentRatio = enrolled + " / " + c.getMaxStudents();

            tableModel.addRow(new Object[] {
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getDepartment(),
                    c.getCredits() + " Credits",
                    facName,
                    enrollmentRatio,
                    c.isActive() ? "Active" : "Archived"
            });
        }

        countLabel.setText("(" + currentList.size() + " courses)");
    }

    private Course getSelectedCourse() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size())
            return null;
        return currentList.get(row);
    }

    private void openCourseForm(Course course) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        CourseFormDialog dialog = new CourseFormDialog(parent, course, courseService, this::loadCourses);
        dialog.setVisible(true);
    }

    private void editSelectedCourse() {
        Course selected = getSelectedCourse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        openCourseForm(selected);
    }

    private void deleteSelectedCourse() {
        Course selected = getSelectedCourse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete course '" + selected.getCourseName() + "' (" + selected.getCourseCode()
                        + ")?",
                "Confirm Course Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            ServiceResult res = courseService.deleteCourse(selected.getId());
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCourses();
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
