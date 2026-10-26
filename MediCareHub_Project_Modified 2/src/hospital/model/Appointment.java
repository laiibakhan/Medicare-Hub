package hospital.model;

/**
 * Appointment class links a Patient to a Doctor at a specific date/time.
 * Demonstrates a simple relationship between two model classes.
 * All logic in this class is my own original work.
 */
public class Appointment {

    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String date;      // Format: yyyy-MM-dd
    private String time;      // Format: HH:mm
    private String status;    // Booked, Completed, Cancelled

    public Appointment(String appointmentId, String patientId, String doctorId,
                       String date, String time, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // CSV format for appointment file
    public String toCSV() {
        return appointmentId + "," + patientId + "," + doctorId + ","
                + date + "," + time + "," + status;
    }

    // Parse a line from file back into an Appointment object
    public static Appointment fromCSV(String line) {
        String[] p = line.split(",");
        return new Appointment(p[0], p[1], p[2], p[3], p[4], p[5]);
    }
}