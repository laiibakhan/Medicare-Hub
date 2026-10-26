package hospital.model;

/**
 * Bill class stores billing information for a patient's appointment.
 * Demonstrates encapsulation.
 * All logic in this class is my own original work.
 */
public class Bill {

    private String billId;
    private String patientId;
    private String appointmentId;
    private double consultationFee;
    private double medicineCharges;
    private double totalAmount;

    public Bill(String billId, String patientId, String appointmentId,
                double consultationFee, double medicineCharges) {
        this.billId = billId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.consultationFee = consultationFee;
        this.medicineCharges = medicineCharges;
        // Total computed automatically
        this.totalAmount = consultationFee + medicineCharges;
    }

    public String getBillId() { return billId; }
    public String getPatientId() { return patientId; }
    public String getAppointmentId() { return appointmentId; }
    public double getConsultationFee() { return consultationFee; }
    public double getMedicineCharges() { return medicineCharges; }
    public double getTotalAmount() { return totalAmount; }

    public String toCSV() {
        return billId + "," + patientId + "," + appointmentId + ","
                + consultationFee + "," + medicineCharges + "," + totalAmount;
    }

    public static Bill fromCSV(String line) {
        String[] p = line.split(",");
        return new Bill(p[0], p[1], p[2],
                Double.parseDouble(p[3]),
                Double.parseDouble(p[4]));
    }
}