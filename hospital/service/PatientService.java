
package hospital.service;

import hospital.model.Patient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Registers and finds patients. All patient validation lives here.
 * Data is kept in memory only and is lost when the program closes.
 */
public class PatientService {

    public static final int MIN_AGE = 0;
    public static final int MAX_AGE = 120;
    public static final List<String> GENDERS =
            List.of("Male", "Female", "Other");

    private static final Pattern ID_PATTERN =
            Pattern.compile("P\\d{3,6}");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("[A-Za-z][A-Za-z .'-]{1,59}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\d{10}");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+");

    private final List<Patient> patients = new ArrayList<>();

    public String generateNextPatientId() {
        int number = patients.size() + 1;

        while (findById(String.format("P%03d", number)).isPresent()) {
            number++;
        }

        return String.format("P%03d", number);
    }

    public Patient registerPatient(String patientId, String fullName,
                                   String ageText, String gender,
                                   String phone, String email)
            throws ValidationException {

        String id = clean(patientId).toUpperCase();

        if (id.isEmpty()) {
            throw new ValidationException("Patient ID is required.");
        }

        if (!ID_PATTERN.matcher(id).matches()) {
            throw new ValidationException(
                    "Patient ID must look like P001 (letter P followed by 3-6 digits).");
        }

        if (findById(id).isPresent()) {
            throw new ValidationException(
                    "Patient ID " + id + " is already registered.");
        }

        String name = clean(fullName);

        if (name.isEmpty()) {
            throw new ValidationException("Full name is required.");
        }

        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new ValidationException(
                    "Name must be 2-60 characters: letters, spaces, . ' - only.");
        }

        int age = parseAge(ageText);

        String cleanGender = clean(gender);

        if (!GENDERS.contains(cleanGender)) {
            throw new ValidationException(
                    "Please select a gender (Male, Female or Other).");
        }

        String cleanPhone = clean(phone);

        if (cleanPhone.isEmpty()) {
            throw new ValidationException("Phone number is required.");
        }

        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            throw new ValidationException(
                    "Phone number must be exactly 10 digits.");
        }

        String cleanEmail = clean(email);

        if (!cleanEmail.isEmpty()
                && !EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new ValidationException(
                    "Email address is not valid (example: name@example.com).");
        }

        Patient patient = new Patient(
                id, name, age, cleanGender, cleanPhone, cleanEmail);

        patients.add(patient);
        return patient;
    }

    public Optional<Patient> findById(String patientId) {
        String wanted = clean(patientId);

        for (Patient patient : patients) {
            if (patient.getPatientId().equalsIgnoreCase(wanted)) {
                return Optional.of(patient);
            }
        }

        return Optional.empty();
    }

    public List<Patient> getAllPatients() {
        return Collections.unmodifiableList(patients);
    }

    private int parseAge(String ageText) throws ValidationException {
        String text = clean(ageText);

        if (text.isEmpty()) {
            throw new ValidationException("Age is required.");
        }

        int age;

        try {
            age = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("Age must be a whole number.");
        }

        if (age < MIN_AGE || age > MAX_AGE) {
            throw new ValidationException(
                    "Age must be between " + MIN_AGE + " and " + MAX_AGE + ".");
        }

        return age;
    }

    private static String clean(String text) {
        return (text == null) ? "" : text.trim();
    }
}
