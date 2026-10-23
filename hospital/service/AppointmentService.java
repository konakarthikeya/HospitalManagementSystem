
package hospital.service;

import hospital.model.Appointment;
import hospital.model.AppointmentStatus;
import hospital.model.Doctor;
import hospital.model.Patient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AppointmentService {

    public static final int SLOT_MINUTES = 30;
    public static final LocalTime CLINIC_OPEN = LocalTime.of(9, 0);
    public static final LocalTime CLINIC_CLOSE = LocalTime.of(17, 0);

    private final List<Appointment> appointments = new ArrayList<>();
    private final PatientService patientService;
    private int nextAppointmentNumber = 1;

    public AppointmentService(PatientService patientService) {
        this.patientService = patientService;
    }

    // ---------------- BOOKING ----------------

    public Appointment bookAppointment(
            String patientId,
            Doctor doctor,
            LocalDate date,
            LocalTime time) throws ValidationException {

        Patient patient = patientService.findById(patientId)
                .orElseThrow(() -> new ValidationException(
                        "No patient found with ID '"
                                + (patientId == null ? "" : patientId.trim())
                                + "'. Register the patient first."));

        validateSlot(patient, doctor, date, time, null);

        Appointment appointment = new Appointment(
                generateAppointmentId(),
                patient,
                doctor,
                date,
                time);

        appointments.add(appointment);
        return appointment;
    }

    // ---------------- MODIFICATION ----------------

    public void modifyAppointment(
            String appointmentId,
            Doctor newDoctor,
            LocalDate newDate,
            LocalTime newTime) throws ValidationException {

        Appointment appointment = requireAppointment(appointmentId);

        if (!appointment.isScheduled()) {
            throw new ValidationException(
                    "This appointment is cancelled and cannot be modified.");
        }

        validateSlot(
                appointment.getPatient(),
                newDoctor,
                newDate,
                newTime,
                appointment.getAppointmentId());

        appointment.reschedule(newDoctor, newDate, newTime);
    }

    // ---------------- CANCELLATION ----------------

    public void cancelAppointment(String appointmentId)
            throws ValidationException {

        Appointment appointment = requireAppointment(appointmentId);

        if (!appointment.isScheduled()) {
            throw new ValidationException(
                    "Appointment " + appointment.getAppointmentId()
                            + " is already cancelled.");
        }

        appointment.cancel();
    }

    // ---------------- SEARCHING ----------------

    public Optional<Appointment> findById(String appointmentId) {

        String wanted = appointmentId == null
                ? ""
                : appointmentId.trim();

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId()
                    .equalsIgnoreCase(wanted)) {
                return Optional.of(appointment);
            }
        }

        return Optional.empty();
    }

    public List<Appointment> getAllAppointments() {
        return Collections.unmodifiableList(appointments);
    }

    // ---------------- TIME SLOTS ----------------

    public List<LocalTime> getAvailableTimeSlots() {

        List<LocalTime> slots = new ArrayList<>();

        for (LocalTime time = CLINIC_OPEN;
             time.isBefore(CLINIC_CLOSE);
             time = time.plusMinutes(SLOT_MINUTES)) {

            slots.add(time);
        }

        return slots;
    }

    public List<LocalTime> getAvailableTimeSlots(
            Doctor doctor,
            LocalDate date) {

        List<LocalTime> availableSlots = new ArrayList<>();

        if (doctor == null || date == null
                || date.isBefore(LocalDate.now())) {
            return availableSlots;
        }

        for (LocalTime time : getAvailableTimeSlots()) {

            if (date.equals(LocalDate.now())
                    && time.isBefore(LocalTime.now())) {
                continue;
            }

            if (isSlotTaken(doctor, date, time, null)) {
                continue;
            }

            availableSlots.add(time);
        }

        return availableSlots;
    }

    // ---------------- DATE PARSING ----------------

    public LocalDate parseDate(String text)
            throws ValidationException {

        String clean = text == null ? "" : text.trim();

        if (clean.isEmpty()) {
            throw new ValidationException(
                    "Date is required (format yyyy-MM-dd, e.g. 2026-11-02).");
        }

        try {
            return LocalDate.parse(clean);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    "'" + clean
                            + "' is not a valid date. Use yyyy-MM-dd.");
        }
    }

    // ---------------- TIME PARSING ----------------

    public LocalTime parseTime(String text)
            throws ValidationException {

        String clean = text == null ? "" : text.trim();

        if (clean.isEmpty()) {
            throw new ValidationException(
                    "Time is required (format HH:mm, e.g. 09:30).");
        }

        try {
            return LocalTime.parse(clean);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    "'" + clean
                            + "' is not a valid time. Use HH:mm (24-hour).");
        }
    }

    // ---------------- VALIDATION ----------------

    private void validateSlot(
            Patient patient,
            Doctor doctor,
            LocalDate date,
            LocalTime time,
            String ignoreAppointmentId) throws ValidationException {

        if (patient == null) {
            throw new ValidationException("Please select a valid patient.");
        }

        if (doctor == null) {
            throw new ValidationException("Please select a doctor.");
        }

        if (date == null) {
            throw new ValidationException(
                    "Please enter an appointment date.");
        }

        if (time == null) {
            throw new ValidationException(
                    "Please select an appointment time.");
        }

        if (!getAvailableTimeSlots().contains(time)) {
            throw new ValidationException(
                    "Time must be a 30-minute slot between 09:00 and 16:30.");
        }

        LocalDate today = LocalDate.now();

        if (date.isBefore(today)) {
            throw new ValidationException(
                    "Appointment date cannot be in the past.");
        }

        if (date.equals(today) && time.isBefore(LocalTime.now())) {
            throw new ValidationException(
                    "That time has already passed today. Choose a later slot.");
        }

        if (isSlotTaken(doctor, date, time, ignoreAppointmentId)) {
            throw new ValidationException(
                    doctor.getName()
                            + " already has a scheduled appointment on "
                            + date + " at " + time + ".");
        }

        if (isPatientSlotTaken(
                patient, date, time, ignoreAppointmentId)) {

            throw new ValidationException(
                    "This patient already has a scheduled appointment on "
                            + date + " at " + time
                            + ". Choose a different time.");
        }
    }

    // Checks whether a doctor already has an appointment in this slot.
    public boolean isSlotTaken(
            Doctor doctor,
            LocalDate date,
            LocalTime time,
            String ignoreAppointmentId) {

        if (doctor == null || date == null || time == null) {
            return false;
        }

        for (Appointment existing : appointments) {

            boolean sameDoctor =
                    existing.getDoctor().getDoctorId()
                            .equals(doctor.getDoctorId());

            boolean sameDate = existing.getDate().equals(date);
            boolean sameTime = existing.getTime().equals(time);

            boolean active =
                    existing.getStatus() == AppointmentStatus.SCHEDULED;

            boolean isSelf = ignoreAppointmentId != null
                    && existing.getAppointmentId()
                            .equals(ignoreAppointmentId);

            if (sameDoctor && sameDate && sameTime && active && !isSelf) {
                return true;
            }
        }

        return false;
    }

    // Checks whether a patient already has an appointment in this slot.
    public boolean isPatientSlotTaken(
            Patient patient,
            LocalDate date,
            LocalTime time,
            String ignoreAppointmentId) {

        if (patient == null || date == null || time == null) {
            return false;
        }

        for (Appointment existing : appointments) {

            boolean samePatient =
                    existing.getPatient().getPatientId()
                            .equalsIgnoreCase(patient.getPatientId());

            boolean sameDate = existing.getDate().equals(date);
            boolean sameTime = existing.getTime().equals(time);

            boolean active =
                    existing.getStatus() == AppointmentStatus.SCHEDULED;

            boolean isSelf = ignoreAppointmentId != null
                    && existing.getAppointmentId()
                            .equals(ignoreAppointmentId);

            if (samePatient && sameDate && sameTime && active && !isSelf) {
                return true;
            }
        }

        return false;
    }

    // ---------------- PRIVATE HELPERS ----------------

    private Appointment requireAppointment(String appointmentId)
            throws ValidationException {

        return findById(appointmentId)
                .orElseThrow(() -> new ValidationException(
                        "No appointment found with ID '"
                                + (appointmentId == null
                                ? ""
                                : appointmentId.trim())
                                + "'."));
    }

    private String generateAppointmentId() {
        return String.format("A%03d", nextAppointmentNumber++);
    }
}
