
package hospital.ui;

import hospital.model.Appointment;
import hospital.model.Doctor;
import hospital.service.AppointmentService;
import hospital.service.DoctorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StatisticsPanel extends JPanel {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                            "Doctor ID",
                            "Doctor Name",
                            "Specialization",
                            "Scheduled Appointments",
                            "Cancelled Appointments"
                    }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable statisticsTable = new JTable(tableModel);

    private final JLabel totalDoctorsLabel =
            new JLabel("Total Doctors: 0");

    private final JLabel scheduledLabel =
            new JLabel("Scheduled Appointments: 0");

    private final JLabel cancelledLabel =
            new JLabel("Cancelled Appointments: 0");

    public StatisticsPanel(
            DoctorService doctorService,
            AppointmentService appointmentService) {

        this.doctorService = doctorService;
        this.appointmentService = appointmentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel("Hospital Statistics");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(heading, BorderLayout.WEST);

        JButton refreshButton = new JButton("Refresh Statistics");
        refreshButton.addActionListener(e -> refreshStatistics());

        topPanel.add(refreshButton, BorderLayout.EAST);

        statisticsTable.setFillsViewportHeight(true);
        statisticsTable.setAutoCreateRowSorter(true);

        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 3, 10, 10));

        summaryPanel.setBorder(
                BorderFactory.createTitledBorder("Overall Summary"));

        summaryPanel.add(totalDoctorsLabel);
        summaryPanel.add(scheduledLabel);
        summaryPanel.add(cancelledLabel);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(statisticsTable), BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);

        refreshStatistics();
    }

    private void refreshStatistics() {

        tableModel.setRowCount(0);

        int totalScheduled = 0;
        int totalCancelled = 0;

        for (Doctor doctor : doctorService.getAllDoctors()) {

            int scheduledCount = 0;
            int cancelledCount = 0;

            for (Appointment appointment :
                    appointmentService.getAllAppointments()) {

                if (appointment.getDoctor().getDoctorId()
                        .equals(doctor.getDoctorId())) {

                    if (appointment.isScheduled()) {
                        scheduledCount++;
                    } else {
                        cancelledCount++;
                    }
                }
            }

            totalScheduled += scheduledCount;
            totalCancelled += cancelledCount;

            tableModel.addRow(new Object[]{
                    doctor.getDoctorId(),
                    doctor.getName(),
                    doctor.getSpecialization(),
                    scheduledCount,
                    cancelledCount
            });
        }

        totalDoctorsLabel.setText(
                "Total Doctors: " + doctorService.getAllDoctors().size());

        scheduledLabel.setText(
                "Scheduled Appointments: " + totalScheduled);

        cancelledLabel.setText(
                "Cancelled Appointments: " + totalCancelled);
    }
}
