
package hospital.ui;

import hospital.model.Patient;
import hospital.service.PatientService;
import hospital.service.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class PatientPanel extends JPanel {

    private final PatientService patientService;

    private final JTextField idField = new JTextField(15);
    private final JTextField nameField = new JTextField(15);
    private final JTextField ageField = new JTextField(15);
    private final JComboBox<String> genderBox =
            new JComboBox<>(PatientService.GENDERS.toArray(new String[0]));
    private final JTextField phoneField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    private final JTextField searchField = new JTextField(12);

    private final JTextArea resultArea = new JTextArea(6, 35);

    public PatientPanel(PatientService patientService) {
        this.patientService = patientService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createForm(), BorderLayout.NORTH);
        add(createResultArea(), BorderLayout.CENTER);

        idField.setText(patientService.generateNextPatientId());
        idField.setEditable(false);
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Register New Patient"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0, "Patient ID:", idField);
        addRow(form, gbc, 1, "Full Name:", nameField);
        addRow(form, gbc, 2, "Age:", ageField);
        addRow(form, gbc, 3, "Gender:", genderBox);
        addRow(form, gbc, 4, "Phone (10 digits):", phoneField);
        addRow(form, gbc, 5, "Email (optional):", emailField);

        JButton registerButton = new JButton("Register Patient");
        JButton clearButton = new JButton("Clear Form");

        registerButton.addActionListener(e -> registerPatient());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(registerButton);
        buttons.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        form.add(buttons, gbc);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(
                BorderFactory.createTitledBorder("Search Patient by ID"));

        searchPanel.add(new JLabel("Patient ID:"));
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchPatient());
        searchPanel.add(searchButton);

        gbc.gridy = 7;
        form.add(searchPanel, gbc);

        return form;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc,
                        int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JScrollPane createResultArea() {
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        resultArea.setText(
                "Welcome to Patient Management.\n"
                + "Register a patient or search using a patient ID.");

        return new JScrollPane(resultArea);
    }

    private void registerPatient() {
        try {
            Patient patient = patientService.registerPatient(
                    idField.getText(),
                    nameField.getText(),
                    ageField.getText(),
                    (String) genderBox.getSelectedItem(),
                    phoneField.getText(),
                    emailField.getText()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Patient registered successfully!\nPatient ID: "
                            + patient.getPatientId(),
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resultArea.setText("REGISTERED PATIENT\n\n" + patient);
            clearForm();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Invalid Patient Details",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void clearForm() {
        nameField.setText("");
        ageField.setText("");
        genderBox.setSelectedIndex(0);
        phoneField.setText("");
        emailField.setText("");
        idField.setText(patientService.generateNextPatientId());
        nameField.requestFocusInWindow();
    }

    private void searchPatient() {
        String patientId = searchField.getText().trim();

        Optional<Patient> found = patientService.findById(patientId);

        if (found.isPresent()) {
            Patient patient = found.get();
            resultArea.setText("PATIENT FOUND\n\n" + patient);
        } else {
            resultArea.setText(
                    "No patient found with ID: " + patientId);
            JOptionPane.showMessageDialog(
                    this,
                    "No patient found with ID: " + patientId,
                    "Patient Not Found",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
