
package hospital;

import hospital.model.Appointment;
import hospital.model.AppointmentStatus;
import hospital.model.Doctor;
import hospital.service.AppointmentService;
import hospital.service.DoctorService;
import hospital.service.PatientService;
import hospital.service.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;

public class ServiceTest {

    private static int passed = 0;
    private static int failed = 0;

    private interface Action {
        void run() throws Exception;
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    private static void expectRejected(String name, Action action) {
        try {
            action.run();
            failed++;
            System.out.println("FAIL  " + name + " (no error was thrown!)");
        } catch (ValidationException e) {
            passed++;
            System.out.println("PASS  " + name + " -> \"" + e.getMessage() + "\"");
        } catch (Exception e) {
            failed++;
            System.out.println("FAIL  " + name + " (unexpected " + e + ")");
        }
    }

    public static void main(String[] args) throws Exception {
        final PatientService patients = new PatientService();
        final DoctorService doctors = new DoctorService();
        final AppointmentService appts = new AppointmentService(patients);

        final Doctor d1 = doctors.getAllDoctors().get(0);
        final Doctor d2 = doctors.getAllDoctors().get(1);
        final LocalDate day = LocalDate.now().plusDays(1);
        final LocalTime t1000 = LocalTime.of(10, 0);
        final LocalTime t1030 = LocalTime.of(10, 30);

        System.out.println("===== PATIENTS =====");

        check("Next suggested ID is P001",
                patients.generateNextPatientId().equals("P001"));

        patients.registerPatient("P001", "Ravi Kumar", "34",
                "Male", "9876543210", "");

        check("Register valid patient P001",
                patients.findById("P001").isPresent());

        check("Next suggested ID is now P002",
                patients.generateNextPatientId().equals("P002"));

        patients.registerPatient("P002", "Sita Devi", "28",
                "Female", "9123456780", "sita@example.com");

        expectRejected("Duplicate patient ID",
                () -> patients.registerPatient("p001", "Other Person",
                        "40", "Male", "9999999999", ""));

        expectRejected("Empty name",
                () -> patients.registerPatient("P003", "   ",
                        "40", "Male", "9999999999", ""));

        expectRejected("Age not a number",
                () -> patients.registerPatient("P003", "Arun Das",
                        "abc", "Male", "9999999999", ""));

        expectRejected("Age out of range",
                () -> patients.registerPatient("P003", "Arun Das",
                        "150", "Male", "9999999999", ""));

        expectRejected("Phone too short",
                () -> patients.registerPatient("P003", "Arun Das",
                        "40", "Male", "12345", ""));

        expectRejected("Malformed email",
                () -> patients.registerPatient("P003", "Arun Das",
                        "40", "Male", "9999999999", "abc@"));

        expectRejected("No gender selected",
                () -> patients.registerPatient("P003", "Arun Das",
                        "40", "", "9999999999", ""));

        check("Rejected patients were NOT stored",
                patients.getAllPatients().size() == 2);

        check("Find patient ignoring spaces/case",
                patients.findById("  p001 ").isPresent());

        check("Nonexistent patient P999",
                patients.findById("P999").isEmpty());

        System.out.println("\n===== BOOKING =====");

        Appointment a1 = appts.bookAppointment("P001", d1, day, t1000);

        check("Book valid appointment gets ID A001",
                a1.getAppointmentId().equals("A001"));

        expectRejected("Same doctor, same date/time",
                () -> appts.bookAppointment("P002", d1, day, t1000));

        Appointment a2 = appts.bookAppointment("P002", d2, day, t1000);

        check("Different doctor, same date/time is allowed",
                a2.getAppointmentId().equals("A002"));

        expectRejected("Unknown patient P999",
                () -> appts.bookAppointment("P999", d1, day, t1030));

        expectRejected("No doctor selected",
                () -> appts.bookAppointment("P001", null, day, t1030));

        expectRejected("Time not on slot grid (10:15)",
                () -> appts.bookAppointment("P001", d1, day,
                        LocalTime.of(10, 15)));

        expectRejected("Before clinic opens (08:00)",
                () -> appts.bookAppointment("P001", d1, day,
                        LocalTime.of(8, 0)));

        expectRejected("At closing time (17:00)",
                () -> appts.bookAppointment("P001", d1, day,
                        LocalTime.of(17, 0)));

        expectRejected("Date in the past",
                () -> appts.bookAppointment("P001", d1,
                        LocalDate.now().minusDays(1), t1000));

        expectRejected("Impossible date text",
                () -> appts.parseDate("2026-02-30"));

        expectRejected("Empty date text",
                () -> appts.parseDate("  "));

        expectRejected("Invalid time text",
                () -> appts.parseTime("25:00"));

        check("parseDate works",
                appts.parseDate(" " + day + " ").equals(day));

        check("parseTime works",
                appts.parseTime("10:30").equals(t1030));

        System.out.println("\n===== CANCEL =====");

        appts.cancelAppointment("A001");

        check("A001 is now CANCELLED",
                a1.getStatus() == AppointmentStatus.CANCELLED);

        expectRejected("Cancel A001 a second time",
                () -> appts.cancelAppointment("A001"));

        expectRejected("Cancel nonexistent A999",
                () -> appts.cancelAppointment("A999"));

        Appointment a3 = appts.bookAppointment("P002", d1, day, t1000);

        check("Cancelled slot can be booked again",
                a3.getAppointmentId().equals("A003"));

        check("Cancelled A001 still stored",
                appts.findById("A001").isPresent());

        check("Total appointments stored = 3",
                appts.getAllAppointments().size() == 3);

        System.out.println("\n===== MODIFY =====");

        appts.modifyAppointment("A003", d1, day, t1000);

        check("Modify A003 with identical values succeeds",
                a3.getTime().equals(t1000));

        expectRejected("Modify A002 into A003's slot",
                () -> appts.modifyAppointment("A002", d1, day, t1000));

        check("A002 unchanged after failed modify",
                a2.getDoctor() == d2
                        && a2.getTime().equals(t1000)
                        && a2.getDate().equals(day));

        appts.modifyAppointment("A003", d1, day, t1030);

        check("Modify A003 to 10:30 succeeds",
                a3.getTime().equals(t1030));

        check("A003 keeps its ID after modify",
                a3.getAppointmentId().equals("A003"));

        appts.modifyAppointment("A002", d1, day, t1000);

        check("A002 can move into the freed slot",
                a2.getDoctor() == d1);

        expectRejected("Modify a cancelled appointment",
                () -> appts.modifyAppointment("A001", d2, day, t1030));

        expectRejected("Modify nonexistent A999",
                () -> appts.modifyAppointment("A999", d2, day, t1030));

        System.out.println("\n===== SUMMARY =====");
        System.out.println("Result: " + passed + " passed, " + failed + " failed.");
    }
}
