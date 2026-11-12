package com.campusflow.view.student;

import com.campusflow.config.AppConfig;
import com.campusflow.dao.AttendanceDAO;
import com.campusflow.dao.CourseDAO;
import com.campusflow.dao.EnrollmentDAO;
import com.campusflow.dao.impl.AttendanceDAOImpl;
import com.campusflow.dao.impl.CourseDAOImpl;
import com.campusflow.dao.impl.EnrollmentDAOImpl;
import com.campusflow.model.Course;
import com.campusflow.model.Enrollment;
import com.campusflow.model.Student;
import com.campusflow.view.components.ModernButton;
import com.campusflow.view.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class StudentProfileDialog extends JDialog {
    private final Student student;
    private final EnrollmentDAO enrollmentDAO;
    private final CourseDAO courseDAO;
    private final AttendanceDAO attendanceDAO;

    public StudentProfileDialog(Window owner, Student student) {
        super(owner, "Student Profile — " + student.getFullName(), ModalityType.APPLICATION_MODAL);
        this.student = student;
        this.enrollmentDAO = new EnrollmentDAOImpl();
        this.courseDAO = new CourseDAOImpl();
        this.attendanceDAO = new AttendanceDAOImpl();

        setSize(680, 560);
        setLocationRelativeTo(owner);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(AppConfig.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Card with Avatar & Basic Info
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
                String in = student.getFirstName().substring(0, 1).toUpperCase() + student.getLastName().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(in)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(in, x, y);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(64, 64));
        avatar.setOpaque(false);
        headerCard.add(avatar, BorderLayout.WEST);

        // Name & details
        JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(student.getFullName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel idLabel = new JLabel("ID: " + student.getStudentId() + "  •  Department: " + student.getDepartment() + "  •  Semester " + student.getSemester());
        idLabel.setFont(AppConfig.FONT_MEDIUM);
        idLabel.setForeground(AppConfig.TEXT_SECONDARY);

        JLabel contactLabel = new JLabel("✉ " + student.getEmail() + "    ☎ " + (student.getPhone() != null ? student.getPhone() : "N/A"));
        contactLabel.setFont(AppConfig.FONT_SMALL);
        contactLabel.setForeground(AppConfig.TEXT_SECONDARY);

        textPanel.add(nameLabel);
        textPanel.add(idLabel);
        textPanel.add(contactLabel);
        headerCard.add(textPanel, BorderLayout.CENTER);

        root.add(headerCard, BorderLayout.NORTH);

        // Center: Enrolled Courses & Attendance
        RoundedPanel tableCard = new RoundedPanel(14, Color.WHITE, new Color(0xE2E8F0));
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel tableTitle = new JLabel("Academic Enrollments & Attendance Summary");
        tableTitle.setFont(AppConfig.FONT_TITLE);
        tableTitle.setForeground(AppConfig.TEXT_PRIMARY);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Course Name", "Credits", "Status", "Attendance %"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Enrollment> enrollments = enrollmentDAO.findByStudentId(student.getId());
        if (enrollments.isEmpty()) {
            model.addRow(new Object[]{"-", "No active course enrollments", "-", "-", "-"});
        } else {
            for (Enrollment e : enrollments) {
                String code = "CRS-" + e.getCourseId();
                String name = "Course #" + e.getCourseId();
                int credits = 3;

                Optional<Course> cOpt = courseDAO.findById(e.getCourseId());
                if (cOpt.isPresent()) {
                    Course c = cOpt.get();
                    code = c.getCourseCode();
                    name = c.getCourseName();
                    credits = c.getCredits();
                }

                double att = attendanceDAO.getAttendancePercentage(student.getId(), e.getCourseId());
                String attStr = String.format("%.1f%%", att);

                model.addRow(new Object[]{code, name, credits + " cr", e.getStatus(), attStr});
            }
        }

        JTable table = new JTable(model);
        table.setFont(AppConfig.FONT_MEDIUM);
        table.setRowHeight(32);
        table.getTableHeader().setFont(AppConfig.FONT_LABEL);
        table.getTableHeader().setBackground(new Color(0xF8FAFC));
        table.setShowGrid(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        root.add(tableCard, BorderLayout.CENTER);

        // Close button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        ModernButton closeBtn = ModernButton.secondary("Close");
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(closeBtn);
        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);
    }
}

