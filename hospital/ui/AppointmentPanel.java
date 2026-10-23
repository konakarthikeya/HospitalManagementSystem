
package hospital.ui;

import hospital.model.Appointment;
import hospital.model.Doctor;
import hospital.service.AppointmentService;
import hospital.service.DoctorService;
import hospital.service.PatientService;
import hospital.service.ValidationException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Pattern;

public class AppointmentPanel extends JPanel {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    private final JTextField patientIdField = new JTextField(15);
    private final JComboBox<Doctor> doctorBox = new JComboBox<>();
    private final JTextField dateField = new JTextField(15);
    private final JComboBox<String> timeBox = new JComboBox<>();

    private final JTextField cancelIdField = new JTextField(12);
    private final JTextField searchField = new JTextField(18);

    private final JTextArea resultArea = new JTextArea(4, 40);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                            "Appointment ID",
                            "Patient ID",
                            "Patient Name",
                            "Doctor",
                            "Date",
                            "Time",
                            "Status"
                    }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable appointmentTable = new JTable(tableModel);

    private final TableRowSorter<DefaultTableModel> tableSorter =
            new TableRowSorter<>(tableModel);

    private String selectedAppointmentId;

    public AppointmentPanel(
            PatientService patientService,
            DoctorService doctorService,
            AppointmentService appointmentService) {

        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        appointmentTable.setRowSorter(tableSorter);
        appointmentTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS);
        appointmentTable.setFillsViewportHeight(true);

        add(createForm(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createResultPanel(), BorderLayout.SOUTH);

        loadDoctors();

        dateField.setText(
                LocalDate.now().plusDays(1).toString());

        installListeners();
        loadTimeSlots();
        refreshAppointmentTable();
    }

    private JPanel createForm() {

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(
                BorderFactory.createTitledBorder(
                        "Appointment Management"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0, "Patient ID:", patientIdField);
        addRow(form, gbc, 1, "Doctor:", doctorBox);
        addRow(form, gbc, 2, "Date (YYYY-MM-DD):", dateField);
        addRow(form, gbc, 3, "Available Time:", timeBox);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        JButton refreshButton = new JButton("Refresh Times");
        JButton bookButton = new JButton("Book");
        JButton modifyButton = new JButton("Modify Selected");
        JButton cancelButton = new JButton("Cancel Selected");
        JButton clearButton = new JButton("Clear");

        refreshButton.addActionListener(e -> loadTimeSlots());
        bookButton.addActionListener(e -> bookAppointment());
        modifyButton.addActionListener(e -> modifyAppointment());
        cancelButton.addActionListener(e -> cancelAppointment());
        clearButton.addActionListener(e -> clearForm());

        buttons.add(refreshButton);
        buttons.add(bookButton);
        buttons.add(modifyButton);
        buttons.add(cancelButton);
        buttons.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        form.add(buttons, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Appointment ID:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(cancelIdField, gbc);

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        searchPanel.add(new JLabel("Search by Patient ID:"));
        searchPanel.add(searchField);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        form.add(searchPanel, gbc);

        return form;
    }

    private void addRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Scheduled Appointments"));

        panel.add(
                new JScrollPane(appointmentTable),
                BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createResultPanel() {

        resultArea.setEditable(false);
        resultArea.setFont(
                new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        resultArea.setText(
                "Select an appointment row to view and edit its details.");

        return new JScrollPane(resultArea);
    }

    private void loadDoctors() {

        doctorBox.removeAllItems();

        for (Doctor doctor : doctorService.getAllDoctors()) {
            doctorBox.addItem(doctor);
        }
    }

    private void installListeners() {

        // MouseListener: select an appointment and populate the form.
        appointmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int viewRow = appointmentTable.getSelectedRow();

                if (viewRow >= 0) {
                    int modelRow =
                            appointmentTable.convertRowIndexToModel(
                                    viewRow);

                    String appointmentId =
                            tableModel.getValueAt(modelRow, 0).toString();

                    selectAppointment(appointmentId);
                }
            }
        });

        // KeyListener: filter the table while typing.
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyPatientSearch();
            }
        });

        // Refresh time slots when the doctor changes.
        doctorBox.addActionListener(e -> {
            if (isShowing()) {
                loadTimeSlots();
            }
        });

        // Refresh time slots when Enter is pressed in the date field.
        dateField.addActionListener(e -> loadTimeSlots());

        // Update search results when the text changes.
        searchField.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        applyPatientSearch();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        applyPatientSearch();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        applyPatientSearch();
                    }
                });
    }

    private void loadTimeSlots() {

        timeBox.removeAllItems();

        try {
            LocalDate date =
                    appointmentService.parseDate(
                            dateField.getText());

            Doctor doctor =
                    (Doctor) doctorBox.getSelectedItem();

            if (doctor == null) {
                return;
            }

            List<LocalTime> slots =
                    appointmentService.getAvailableTimeSlots(
                            doctor, date);

            for (LocalTime time : slots) {
                timeBox.addItem(time.toString());
            }

            // Preserve the selected appointment's current slot
            // when editing that appointment.
            if (selectedAppointmentId != null) {
                appointmentService.findById(selectedAppointmentId)
                        .filter(Appointment::isScheduled)
                        .filter(a -> a.getDoctor().getDoctorId()
                                .equals(doctor.getDoctorId()))
                        .filter(a -> a.getDate().equals(date))
                        .ifPresent(a -> {

                            String currentTime =
                                    a.getTime().toString();

                            boolean exists = false;

                            for (int i = 0;
                                 i < timeBox.getItemCount(); i++) {

                                if (timeBox.getItemAt(i)
                                        .equals(currentTime)) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (!exists) {
                                timeBox.addItem(currentTime);
                            }
                        });
            }

            if (slots.isEmpty()) {
                resultArea.setText(
                        "No new available time slots for this date. "
                                + "An existing appointment's current slot "
                                + "may still be available for editing.");
            }

        } catch (ValidationException ex) {
            resultArea.setText(ex.getMessage());
        }
    }

    private void bookAppointment() {

        try {
            String patientId = patientIdField.getText().trim();
            Doctor doctor = (Doctor) doctorBox.getSelectedItem();

            if (doctor == null) {
                throw new ValidationException(
                        "Please select a doctor.");
            }

            LocalDate date =
                    appointmentService.parseDate(dateField.getText());

            Object selectedTime = timeBox.getSelectedItem();

            if (selectedTime == null) {
                throw new ValidationException(
                        "Please select an available time slot.");
            }

            LocalTime time =
                    appointmentService.parseTime(
                            selectedTime.toString());

            Appointment appointment =
                    appointmentService.bookAppointment(
                            patientId, doctor, date, time);

            resultArea.setText(
                    "APPOINTMENT BOOKED SUCCESSFULLY\n\n"
                            + appointment);

            refreshAppointmentTable();
            loadTimeSlots();

            JOptionPane.showMessageDialog(
                    this,
                    "Appointment booked successfully!\nAppointment ID: "
                            + appointment.getAppointmentId(),
                    "Booking Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (ValidationException ex) {
            showError("Booking Failed", ex.getMessage());
        }
    }

    private void selectAppointment(String appointmentId) {

        appointmentService.findById(appointmentId)
                .filter(Appointment::isScheduled)
                .ifPresentOrElse(appointment -> {

                    selectedAppointmentId =
                            appointment.getAppointmentId();

                    patientIdField.setText(
                            appointment.getPatient().getPatientId());

                    cancelIdField.setText(
                            appointment.getAppointmentId());

                    doctorBox.setSelectedItem(
                            appointment.getDoctor());

                    dateField.setText(
                            appointment.getDate().toString());

                    loadTimeSlots();

                    timeBox.setSelectedItem(
                            appointment.getTime().toString());

                    resultArea.setText(
                            "SELECTED APPOINTMENT\n\n" + appointment
                                    + "\n\nChange the doctor, date, or time, "
                                    + "then click Modify Selected.");

                }, () -> showError(
                        "Appointment Not Found",
                        "This appointment is not scheduled or could not be found."));
    }

    private void modifyAppointment() {

        try {
            if (selectedAppointmentId == null) {
                throw new ValidationException(
                        "Select an appointment row in the table first.");
            }

            Doctor doctor =
                    (Doctor) doctorBox.getSelectedItem();

            if (doctor == null) {
                throw new ValidationException(
                        "Please select a doctor.");
            }

            LocalDate date =
                    appointmentService.parseDate(dateField.getText());

            Object selectedTime = timeBox.getSelectedItem();

            if (selectedTime == null) {
                throw new ValidationException(
                        "Please select an available time.");
            }

            LocalTime time =
                    appointmentService.parseTime(
                            selectedTime.toString());

            appointmentService.modifyAppointment(
                    selectedAppointmentId,
                    doctor,
                    date,
                    time);

            resultArea.setText(
                    "APPOINTMENT MODIFIED SUCCESSFULLY\n\n"
                            + appointmentService.findById(
                                    selectedAppointmentId).orElseThrow());

            refreshAppointmentTable();
            loadTimeSlots();

            JOptionPane.showMessageDialog(
                    this,
                    "Appointment modified successfully.",
                    "Modification Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (ValidationException ex) {
            showError("Modification Failed", ex.getMessage());
        }
    }

    private void cancelAppointment() {

        try {
            String appointmentId =
                    cancelIdField.getText().trim();

            if (appointmentId.isEmpty()) {
                throw new ValidationException(
                        "Select an appointment row or enter an appointment ID.");
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Cancel appointment " + appointmentId + "?",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            appointmentService.cancelAppointment(appointmentId);

            resultArea.setText(
                    "APPOINTMENT CANCELLED\n\nAppointment ID: "
                            + appointmentId);

            selectedAppointmentId = null;
            cancelIdField.setText("");
            appointmentTable.clearSelection();

            // Refreshing removes the cancelled appointment from the table.
            refreshAppointmentTable();
            loadTimeSlots();

            JOptionPane.showMessageDialog(
                    this,
                    "Appointment " + appointmentId
                            + " cancelled and removed from the table.",
                    "Cancellation Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (ValidationException ex) {
            showError("Cancellation Failed", ex.getMessage());
        }
    }

    private void refreshAppointmentTable() {

        tableModel.setRowCount(0);

        for (Appointment appointment :
                appointmentService.getAllAppointments()) {

            // IMPORTANT: display scheduled appointments only.
            if (!appointment.isScheduled()) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    appointment.getAppointmentId(),
                    appointment.getPatient().getPatientId(),
                    appointment.getPatient().getFullName(),
                    appointment.getDoctor().getName(),
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getStatus()
            });
        }

        applyPatientSearch();
    }

    private void applyPatientSearch() {

        String patientId = searchField.getText().trim();

        if (patientId.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" + Pattern.quote(patientId), 1));
        }
    }

    private void clearForm() {

        patientIdField.setText("");
        cancelIdField.setText("");
        selectedAppointmentId = null;

        appointmentTable.clearSelection();

        dateField.setText(
                LocalDate.now().plusDays(1).toString());

        doctorBox.setSelectedIndex(
                doctorBox.getItemCount() > 0 ? 0 : -1);

        loadTimeSlots();

        resultArea.setText(
                "Form cleared. Register a patient first, "
                        + "then book or select an appointment.");

        patientIdField.requestFocusInWindow();
    }

    private void showError(String title, String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.WARNING_MESSAGE);
    }
}
