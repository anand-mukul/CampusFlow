package com.campusflow.view.faculty;

import com.campusflow.config.AppConfig;
import com.campusflow.dao.CourseDAO;
import com.campusflow.dao.EnrollmentDAO;
import com.campusflow.dao.impl.CourseDAOImpl;
import com.campusflow.dao.impl.EnrollmentDAOImpl;
import com.campusflow.model.Course;
import com.campusflow.model.Faculty;
import com.campusflow.view.components.ModernButton;
import com.campusflow.view.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FacultyProfileDialog extends JDialog {
    private final Faculty faculty;
    private final CourseDAO courseDAO;
    private final EnrollmentDAO enrollmentDAO;

    public FacultyProfileDialog(Window owner, Faculty faculty) {
        super(owner, "Faculty Profile — " + faculty.getFullName(), ModalityType.APPLICATION_MODAL);
        this.faculty = faculty;
        this.courseDAO = new CourseDAOImpl();
        this.enrollmentDAO = new EnrollmentDAOImpl();

        setSize(700, 560);
        setLocationRelativeTo(owner);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(AppConfig.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Card with Avatar & Details
        RoundedPanel headerCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        headerCard.setLayout(new BorderLayout(18, 0));
        headerCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Initials Avatar
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.PRIMARY_COLOR);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String in = getInitials(faculty.getFullName());
                int x = (getWidth() - fm.stringWidth(in)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(in, x, y);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(64, 64));
        avatar.setOpaque(false);
        headerCard.add(avatar, BorderLayout.WEST);

        // Faculty Info
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 6));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(faculty.getFullName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel idLabel = new JLabel("ID: " + faculty.getFacultyId() + " (" + faculty.getDesignation() + ")");
        idLabel.setFont(AppConfig.FONT_SMALL);
        idLabel.setForeground(AppConfig.PRIMARY_COLOR);

        JLabel deptLabel = new JLabel("Dept: " + faculty.getDepartment());
        deptLabel.setFont(AppConfig.FONT_REGULAR);
        deptLabel.setForeground(AppConfig.TEXT_SECONDARY);

        JLabel emailLabel = new JLabel("Email: " + faculty.getEmail());
        emailLabel.setFont(AppConfig.FONT_REGULAR);
        emailLabel.setForeground(AppConfig.TEXT_SECONDARY);

        infoPanel.add(nameLabel);
        infoPanel.add(deptLabel);
        infoPanel.add(idLabel);
        infoPanel.add(emailLabel);

        headerCard.add(infoPanel, BorderLayout.CENTER);
        root.add(headerCard, BorderLayout.NORTH);

        // Center Panel: Assigned Courses Table
        RoundedPanel centerCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        centerCard.setLayout(new BorderLayout(0, 10));
        centerCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel assignedTitle = new JLabel("Assigned Teaching Courses");
        assignedTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        assignedTitle.setForeground(AppConfig.TEXT_PRIMARY);
        centerCard.add(assignedTitle, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Course Name", "Credits", "Department", "Enrolled Students"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Course> courses = courseDAO.findByFacultyId(faculty.getId());
        for (Course c : courses) {
            int enrolledCount = enrollmentDAO.findByCourseId(c.getId()).size();
            model.addRow(new Object[]{
                    c.getCode(),
                    c.getName(),
                    c.getCredits() + " Credits",
                    c.getDepartment(),
                    enrolledCount + " / " + c.getMaxStudents()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(AppConfig.FONT_REGULAR);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(0xF8FAFC));
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        if (courses.isEmpty()) {
            JLabel emptyLabel = new JLabel("No courses currently assigned to this faculty member.", SwingConstants.CENTER);
            emptyLabel.setFont(AppConfig.FONT_REGULAR);
            emptyLabel.setForeground(AppConfig.TEXT_SECONDARY);
            centerCard.add(emptyLabel, BorderLayout.SOUTH);
        }

        root.add(centerCard, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        ModernButton closeBtn = ModernButton.primary("Close");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "FC";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
