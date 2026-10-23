
package hospital.ui;

import hospital.service.AppointmentService;
import hospital.service.DoctorService;
import hospital.service.PatientService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    private final JTabbedPane tabs = new JTabbedPane();

    private final JLabel statusLabel =
            new JLabel(" Ready | Hospital Management System");

    public MainFrame(
            PatientService patientService,
            DoctorService doctorService,
            AppointmentService appointmentService) {

        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;

        setTitle("Hospital Management System - Reception Desk");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        buildMenuBar();
        buildTabs();
        buildStatusBar();

        addWindowListener(createWindowListener());
    }

    private void buildMenuBar() {

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenu navigationMenu = new JMenu("Navigate");
        JMenu helpMenu = new JMenu("Help");

        JMenuItem patientsItem = new JMenuItem("Patients");
        JMenuItem appointmentsItem = new JMenuItem("Appointments");
        JMenuItem statisticsItem = new JMenuItem("Statistics");
        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem exitItem = new JMenuItem("Exit");

        patientsItem.addActionListener(
                e -> tabs.setSelectedIndex(0));

        appointmentsItem.addActionListener(
                e -> tabs.setSelectedIndex(1));

        statisticsItem.addActionListener(
                e -> tabs.setSelectedIndex(2));

        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Hospital Management System\nVersion 1.0",
                        "About",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );

        exitItem.addActionListener(e -> confirmExit());

        navigationMenu.add(patientsItem);
        navigationMenu.add(appointmentsItem);
        navigationMenu.add(statisticsItem);

        helpMenu.add(aboutItem);
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        menuBar.add(navigationMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void buildTabs() {

        // Patient registration and search screen
        tabs.addTab(
                "Patients",
                new PatientPanel(patientService)
        );

        // Appointment booking and scheduling screen
        tabs.addTab(
                "Appointments",
                new AppointmentPanel(
                        patientService,
                        doctorService,
                        appointmentService
                )
        );

        // Statistics screen — to be implemented later
        tabs.addTab(
        "Statistics",
        new StatisticsPanel(
                doctorService,
                appointmentService
        )
);

        tabs.addChangeListener(e -> updateStatusBar());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createPlaceholder(
            String heading,
            String message) {

        JPanel panel = new JPanel(new GridBagLayout());

        JPanel content = new JPanel();
        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel(heading);
        title.setFont(
                new Font("SansSerif", Font.BOLD, 24)
        );
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel(message);
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(12));
        content.add(description);

        panel.add(content);

        return panel;
    }

    private void buildStatusBar() {

        statusLabel.setBorder(
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );

        add(statusLabel, BorderLayout.SOUTH);
    }

    private void updateStatusBar() {

        int selectedIndex = tabs.getSelectedIndex();

        if (selectedIndex >= 0) {
            String selectedTab = tabs.getTitleAt(selectedIndex);

            statusLabel.setText(
                    " Ready | Current section: " + selectedTab
            );
        }
    }

    private WindowAdapter createWindowListener() {

        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        };
    }

    private void confirmExit() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
        }
    }
}
