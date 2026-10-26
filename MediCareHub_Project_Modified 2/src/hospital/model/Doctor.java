package hospital.model;

/**
 * Doctor class extends Person.
 * Adds specialization and consultation fee.
 * Demonstrates INHERITANCE.
 * All logic in this class is my own original work.
 */
public class Doctor extends Person {

    private String specialization;
    private double consultationFee;
    private String availableDays = "Monday;Tuesday;Wednesday;Thursday;Friday";
    private String startTime = "09:00";
    private String endTime = "17:00";

    public Doctor(String id, String name, int age, String gender,
                  String specialization, double consultationFee) {
        super(id, name, age, gender);
        this.specialization = specialization;
        this.consultationFee = consultationFee;
    }

    public Doctor(String id, String name, int age, String gender, String specialization,
                  double consultationFee, String availableDays, String startTime, String endTime) {
        this(id, name, age, gender, specialization, consultationFee);
        this.availableDays = (availableDays == null || availableDays.trim().isEmpty()) ? "Monday;Tuesday;Wednesday;Thursday;Friday" : availableDays;
        this.startTime = (startTime == null || startTime.trim().isEmpty()) ? "09:00" : startTime;
        this.endTime = (endTime == null || endTime.trim().isEmpty()) ? "17:00" : endTime;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    @Override
    public String getRole() {
        return "DOCTOR";
    }

    @Override
    public String toCSV() {
        return super.toCSV() + "," + specialization + "," + consultationFee + ","
                + availableDays.replace(",", ";") + "," + startTime + "," + endTime;
    }
}