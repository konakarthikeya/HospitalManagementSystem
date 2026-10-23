
package hospital.model;

public class Doctor {

    private String doctorId;
    private String name;
    private String specialization;
    private double consultationFee;

    public Doctor(String doctorId, String name, String specialization,
                  double consultationFee) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.consultationFee = consultationFee;
    }

    public Doctor(String doctorId, String name, String specialization) {
        this(doctorId, name, specialization, 0.0);
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    @Override
    public String toString() {
        return doctorId + " - " + name + " (" + specialization + ")";
    }
}
