
package hospital.service;

import hospital.model.Doctor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Holds the list of doctors. Sample data only, so edit it freely.
 */
public class DoctorService {

    private final List<Doctor> doctors = new ArrayList<>();

    public DoctorService() {
        loadSampleDoctors();
    }

    private void loadSampleDoctors() {
        doctors.add(new Doctor("D01", "Dr. Anita Rao", "Cardiology", 600.0));
        doctors.add(new Doctor("D02", "Dr. Sam Joseph", "Neurology", 700.0));
        doctors.add(new Doctor("D03", "Dr. Meena Iyer", "Pediatrics", 400.0));
        doctors.add(new Doctor("D04", "Dr. Rahul Verma", "Orthopedics", 500.0));
        doctors.add(new Doctor("D05", "Dr. Priya Nair", "General Medicine"));
    }

    /** Read-only view: callers cannot add or remove doctors through it. */
    public List<Doctor> getAllDoctors() {
        return Collections.unmodifiableList(doctors);
    }

    public Optional<Doctor> findById(String doctorId) {
        if (doctorId == null) {
            return Optional.empty();
        }

        String wanted = doctorId.trim();

        for (Doctor doctor : doctors) {
            if (doctor.getDoctorId().equalsIgnoreCase(wanted)) {
                return Optional.of(doctor);
            }
        }

        return Optional.empty();
    }
}
