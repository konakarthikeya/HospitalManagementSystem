
package hospital.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {

    private String appointmentId;
    private Patient patient;
    private Doctor doctor;
    private LocalDate date;
    private LocalTime time;
    private AppointmentStatus status;

    public Appointment(String appointmentId, Patient patient, Doctor doctor,
                       LocalDate date, LocalTime time) {
        this.appointmentId = appointmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.date = date;
        this.time = time;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public boolean isScheduled() {
        return status == AppointmentStatus.SCHEDULED;
    }

    public void reschedule(Doctor newDoctor, LocalDate newDate,
                           LocalTime newTime) {
        this.doctor = newDoctor;
        this.date = newDate;
        this.time = newTime;
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return appointmentId + " - " + patient.getFullName()
                + " - " + doctor.getName()
                + " - " + date + " " + time
                + " - " + status;
    }
}
