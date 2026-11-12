package com.campusflow.view.faculty;

import com.campusflow.config.AppConfig;
import com.campusflow.model.Faculty;
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

public class FacultyManagementPanel extends JPanel {
    private final FacultyService facultyService;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Faculty> currentList = new ArrayList<>();

    private JTextField searchField;
    private JComboBox<String> deptFilterCombo;
    private JLabel countLabel;

    public FacultyManagementPanel() {
        this.facultyService = new FacultyService();
        setLayout(new BorderLayout(0, 16));
        setBackground(AppConfig.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        loadFaculty();
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
        JLabel title = new JLabel("Faculty Directory");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT_PRIMARY);

        countLabel = new JLabel("(0 faculty)");
        countLabel.setFont(AppConfig.FONT_MEDIUM);
        countLabel.setForeground(AppConfig.TEXT_SECONDARY);
        titlePanel.add(title);
        titlePanel.add(countLabel);
        topRow.add(titlePanel, BorderLayout.WEST);

        // Buttons
        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        ModernButton addBtn = ModernButton.primary("+ Add Faculty");
        addBtn.addActionListener(e -> openFacultyForm(null));

        ModernButton editBtn = ModernButton.secondary("✎ Edit");
        editBtn.addActionListener(e -> editSelectedFaculty());

        ModernButton profileBtn = ModernButton.accent("👁 View Profile");
        profileBtn.addActionListener(e -> viewSelectedProfile());

        ModernButton deleteBtn = ModernButton.danger("🗑 Delete");
        deleteBtn.addActionListener(e -> deleteSelectedFaculty());

        ModernButton refreshBtn = ModernButton.outline("↻", AppConfig.PRIMARY_COLOR);
        refreshBtn.setToolTipText("Refresh Directory");
        refreshBtn.addActionListener(e -> loadFaculty());

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

        searchField = new JTextField(20);
        searchField.setFont(AppConfig.FONT_MEDIUM);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search by name, ID or email...");
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

        String[] cols = {"Faculty ID", "Full Name", "Department", "Designation", "Email", "Phone", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(38);
        table.setFont(AppConfig.FONT_REGULAR);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(0xF8FAFC));
        table.getTableHeader().setForeground(AppConfig.TEXT_SECONDARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        // Center render ID and Status
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Double click to view profile
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedProfile();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadFaculty() {
        populateDepartmentFilter();
        applyFilters();
    }

    private void populateDepartmentFilter() {
        String currentSelection = (String) deptFilterCombo.getSelectedItem();
        deptFilterCombo.removeAllItems();
        deptFilterCombo.addItem("All Departments");
        List<String> depts = facultyService.getDepartments();
        for (String d : depts) {
            deptFilterCombo.addItem(d);
        }
        if (currentSelection != null) {
            deptFilterCombo.setSelectedItem(currentSelection);
        }
    }

    private void applyFilters() {
        String kw = searchField.getText().trim();
        String dept = (String) deptFilterCombo.getSelectedItem();
        if ("All Departments".equals(dept)) dept = null;

        currentList = facultyService.searchFaculty(kw, dept);
        tableModel.setRowCount(0);

        for (Faculty f : currentList) {
            tableModel.addRow(new Object[]{
                    f.getFacultyId(),
                    f.getFullName(),
                    f.getDepartment(),
                    f.getDesignation(),
                    f.getEmail(),
                    f.getPhone() != null ? f.getPhone() : "-",
                    f.isActive() ? "Active" : "Inactive"
            });
        }

        countLabel.setText("(" + currentList.size() + " faculty)");
    }

    private Faculty getSelectedFaculty() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) return null;
        return currentList.get(row);
    }

    private void openFacultyForm(Faculty faculty) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        FacultyFormDialog dialog = new FacultyFormDialog(parent, faculty, facultyService, this::loadFaculty);
        dialog.setVisible(true);
    }

    private void editSelectedFaculty() {
        Faculty selected = getSelectedFaculty();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        openFacultyForm(selected);
    }

    private void viewSelectedProfile() {
        Faculty selected = getSelectedFaculty();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member to view profile.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window parent = SwingUtilities.getWindowAncestor(this);
        FacultyProfileDialog dialog = new FacultyProfileDialog(parent, selected);
        dialog.setVisible(true);
    }

    private void deleteSelectedFaculty() {
        Faculty selected = getSelectedFaculty();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete faculty member '" + selected.getFullName() + "' (" + selected.getFacultyId() + ")?\n" +
                        "This will also delete their login account.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            ServiceResult res = facultyService.deleteFaculty(selected.getId());
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
                loadFaculty();
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

