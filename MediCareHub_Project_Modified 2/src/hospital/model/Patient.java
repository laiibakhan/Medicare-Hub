package hospital.model;

/**
 * Patient class extends Person.
 * Adds disease and assigned doctor info.
 * Demonstrates INHERITANCE.
 *  All logic in this class is my own original work.
 */
public class Patient extends Person {

    private String disease;
    private String assignedDoctorId;

    // Constructor initializes parent fields and its own fields
    public Patient(String id, String name, int age, String gender,
                   String disease, String assignedDoctorId) {
        super(id, name, age, gender);
        this.disease = disease;
        this.assignedDoctorId = assignedDoctorId;
    }

    public String getDisease() { return disease; }
    public void setDisease(String disease) { this.disease = disease; }

    public String getAssignedDoctorId() { return assignedDoctorId; }
    public void setAssignedDoctorId(String assignedDoctorId) {
        this.assignedDoctorId = assignedDoctorId;
    }

    // Overrides abstract method from Person (POLYMORPHISM)
    @Override
    public String getRole() {
        return "PATIENT";
    }

    // CSV format for patient file
    @Override
    public String toCSV() {
        return super.toCSV() + "," + disease + "," + assignedDoctorId;
    }
}