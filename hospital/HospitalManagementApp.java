
package hospital;

import hospital.service.AppointmentService;
import hospital.service.DoctorService;
import hospital.service.PatientService;
import hospital.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point of the application.
 */
public class HospitalManagementApp {

    public static void main(String[] args) {

        // Try to use the operating system's native appearance.
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println(
                    "Could not apply system look and feel: "
                            + e.getMessage());
        }

        // Create the services once and share them with the GUI.
        PatientService patientService = new PatientService();
        DoctorService doctorService = new DoctorService();
        AppointmentService appointmentService =
                new AppointmentService(patientService);

        // Create the Swing window on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(
                    patientService,
                    doctorService,
                    appointmentService);

            frame.setVisible(true);
        });
    }
}
