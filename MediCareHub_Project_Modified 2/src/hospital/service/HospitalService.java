package hospital.service;

import hospital.exception.InvalidInputException;
import hospital.file.FileHandler;
import hospital.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * HospitalService is the central business logic class.
 * Manages patients, doctors, appointments, and bills.
 * Data is persisted to text files via FileHandler.
 * All logic in this class is my own original work.
 */
public class HospitalService {

    private List<Patient> patients = new ArrayList<>();
    private List<Doctor> doctors = new ArrayList<>();
    private List<Appointment> appointments = new ArrayList<>();
    private List<Bill> bills = new ArrayList<>();

    private static final String PATIENT_FILE = "patients.txt";
    private static final String DOCTOR_FILE = "doctors.txt";
    private static final String APPOINTMENT_FILE = "appointments.txt";
    private static final String BILL_FILE = "bills.txt";

    public HospitalService() {
        loadAll();
    }

    // ---------- LOAD DATA ----------
    private void loadAll() {
        patients.clear();
        doctors.clear();
        appointments.clear();
        bills.clear();

        for (String line : FileHandler.readLines(PATIENT_FILE)) {
            String[] p = line.split(",");
            if (p.length >= 7) {
                patients.add(new Patient(p[1], p[2], Integer.parseInt(p[3]),
                        p[4], p[5], p[6]));
            }
        }

        for (String line : FileHandler.readLines(DOCTOR_FILE)) {
            String[] p = line.split(",");
            if (p.length >= 7) {
                doctors.add(new Doctor(p[1], p[2], Integer.parseInt(p[3]),
                        p[4], p[5], Double.parseDouble(p[6]),
                        p.length > 7 ? p[7] : "Monday;Tuesday;Wednesday;Thursday;Friday",
                        p.length > 8 ? p[8] : "09:00", p.length > 9 ? p[9] : "17:00"));
            }
        }

        for (String line : FileHandler.readLines(APPOINTMENT_FILE)) {
            appointments.add(Appointment.fromCSV(line));
        }

        for (String line : FileHandler.readLines(BILL_FILE)) {
            bills.add(Bill.fromCSV(line));
        }
    }

    // ---------- PATIENT OPERATIONS ----------
    public void addPatient(String id, String name, int age, String gender,
                           String disease, String assignedDoctorId)
            throws InvalidInputException {
        if (id == null || id.trim().isEmpty())
            throw new InvalidInputException("Patient ID cannot be empty.");
        validateName(name, "patient");
        if (age <= 0 || age > 150)
            throw new InvalidInputException("Age must be between 1 and 150.");
        validateGender(gender);
        if (findPatientById(id) != null)
            throw new InvalidInputException("Patient with ID " + id + " already exists.");

        Patient p = new Patient(id, name, age, gender, disease, assignedDoctorId);
        patients.add(p);
        FileHandler.appendLine(PATIENT_FILE, p.toCSV());
    }

    public Patient findPatientById(String id) {
        for (Patient p : patients) if (p.getId().equals(id)) return p;
        return null;
    }

    public List<Patient> getAllPatients() { return patients; }

    public void updatePatient(String id, String name, int age, String gender,
                              String disease, String doctorId)
            throws InvalidInputException {
        Patient p = findPatientById(id);
        if (p == null) throw new InvalidInputException("Patient not found.");
        validateName(name, "patient");
        if (age <= 0 || age > 150)
            throw new InvalidInputException("Age must be between 1 and 150.");

        validateGender(gender);
        p.setName(name);
        p.setAge(age);
        p.setGender(gender);
        p.setDisease(disease);
        p.setAssignedDoctorId(doctorId);
        saveAllPatients();
    }

    public void deletePatient(String id) throws InvalidInputException {
        Patient p = findPatientById(id);
        if (p == null) throw new InvalidInputException("Patient not found.");
        patients.remove(p);
        saveAllPatients();
    }

    private void saveAllPatients() {
        List<String> lines = new ArrayList<>();
        for (Patient p : patients) lines.add(p.toCSV());
        FileHandler.writeLines(PATIENT_FILE, lines);
    }

    // ---------- DOCTOR OPERATIONS ----------
    public void addDoctor(String id, String name, int age, String gender,
                          String specialization, double fee)
            throws InvalidInputException {
        if (id == null || id.trim().isEmpty())
            throw new InvalidInputException("Doctor ID cannot be empty.");
        validateName(name, "doctor");
        if (age <= 0 || age > 150)
            throw new InvalidInputException("Age must be between 1 and 150.");
        if (fee < 0 || !Double.isFinite(fee))
            throw new InvalidInputException("Fee must be a valid non-negative number.");
        validateGender(gender);
        if (findDoctorById(id) != null)
            throw new InvalidInputException("Doctor with ID " + id + " already exists.");

        Doctor d = new Doctor(id, name, age, gender, specialization, fee);
        doctors.add(d);
        FileHandler.appendLine(DOCTOR_FILE, d.toCSV());
    }

    public void addDoctor(String id, String name, int age, String gender, String specialization,
                          double fee, String availableDays, String startTime, String endTime)
            throws InvalidInputException {
        validateSchedule(availableDays, startTime, endTime);
        addDoctor(id, name, age, gender, specialization, fee);
        Doctor d = findDoctorById(id);
        d.setAvailableDays(availableDays); d.setStartTime(startTime); d.setEndTime(endTime);
        saveAllDoctors();
    }

    public Doctor findDoctorById(String id) {
        for (Doctor d : doctors) if (d.getId().equals(id)) return d;
        return null;
    }

    public List<Doctor> getAllDoctors() { return doctors; }

    public void updateDoctor(String id, String name, int age, String gender,
                             String specialization, double fee)
            throws InvalidInputException {
        Doctor d = findDoctorById(id);
        if (d == null) throw new InvalidInputException("Doctor not found.");
        validateName(name, "doctor");
        if (age <= 0 || age > 150)
            throw new InvalidInputException("Age must be between 1 and 150.");
        if (fee < 0 || !Double.isFinite(fee))
            throw new InvalidInputException("Fee must be a valid non-negative number.");
        validateGender(gender);
        d.setName(name);
        d.setAge(age);
        d.setGender(gender);
        d.setSpecialization(specialization);
        d.setConsultationFee(fee);
        saveAllDoctors();
    }

    public void updateDoctor(String id, String name, int age, String gender,
                             String specialization, double fee, String availableDays,
                             String startTime, String endTime) throws InvalidInputException {
        validateSchedule(availableDays, startTime, endTime);
        updateDoctor(id, name, age, gender, specialization, fee);
        Doctor d = findDoctorById(id);
        d.setAvailableDays(availableDays); d.setStartTime(startTime); d.setEndTime(endTime);
        saveAllDoctors();
    }

    public void deleteDoctor(String id) throws InvalidInputException {
        Doctor d = findDoctorById(id);
        if (d == null) throw new InvalidInputException("Doctor not found.");
        doctors.remove(d);
        saveAllDoctors();
    }

    private void saveAllDoctors() {
        List<String> lines = new ArrayList<>();
        for (Doctor d : doctors) lines.add(d.toCSV());
        FileHandler.writeLines(DOCTOR_FILE, lines);
    }

    // ---------- APPOINTMENT OPERATIONS ----------
    public void bookAppointment(String appointmentId, String patientId,
                                String doctorId, String date, String time)
            throws InvalidInputException {
        // Generate a fresh sequential ID if the form is empty or its displayed ID
        // has become stale/already used. Patient selection must never determine the ID.
        if (appointmentId == null || appointmentId.trim().isEmpty()
                || findAppointmentById(appointmentId.trim()) != null) {
            appointmentId = nextId("APT-", appointments.stream()
                    .map(Appointment::getAppointmentId).toList());
        } else {
            appointmentId = appointmentId.trim();
        }
        if (findPatientById(patientId) == null)
            throw new InvalidInputException("Patient ID not found.");
        if (findDoctorById(doctorId) == null)
            throw new InvalidInputException("Doctor ID not found.");
        if (date.isEmpty() || time.isEmpty())
            throw new InvalidInputException("Date and time required.");
        if (findAppointmentById(appointmentId) != null)
            throw new InvalidInputException("Appointment ID " + appointmentId + " already exists.");
        ensureDoctorSchedule(doctorId, date, time);
        ensureDoctorSlotAvailable(appointmentId, doctorId, date, time);

        Appointment a = new Appointment(appointmentId, patientId, doctorId,
                date, time, "Booked");
        appointments.add(a);
        FileHandler.appendLine(APPOINTMENT_FILE, a.toCSV());
    }

    public Appointment findAppointmentById(String id) {
        for (Appointment a : appointments) {
            if (a.getAppointmentId().equals(id)) return a;
        }
        return null;
    }

    public List<Appointment> getAllAppointments() { return appointments; }

    public void updateAppointment(String appointmentId, String patientId,
                                  String doctorId, String date, String time,
                                  String status)
            throws InvalidInputException {
        Appointment a = findAppointmentById(appointmentId);
        if (a == null) throw new InvalidInputException("Appointment not found.");
        if (findPatientById(patientId) == null)
            throw new InvalidInputException("Patient ID not found.");
        if (findDoctorById(doctorId) == null)
            throw new InvalidInputException("Doctor ID not found.");
        if (date.isEmpty() || time.isEmpty())
            throw new InvalidInputException("Date and time required.");
        ensureDoctorSchedule(doctorId, date, time);
        ensureDoctorSlotAvailable(appointmentId, doctorId, date, time);

        appointments.remove(a);
        Appointment updated = new Appointment(appointmentId, patientId, doctorId,
                date, time, status);
        appointments.add(updated);
        saveAllAppointments();
    }

    private void ensureDoctorSlotAvailable(String appointmentId, String doctorId,
                                           String date, String time)
            throws InvalidInputException {
        for (Appointment appointment : appointments) {
            if (!appointment.getAppointmentId().equals(appointmentId)
                    && !"Cancelled".equalsIgnoreCase(appointment.getStatus())
                    && appointment.getDoctorId().equalsIgnoreCase(doctorId)
                    && appointment.getDate().equals(date)
                    && appointment.getTime().equalsIgnoreCase(time)) {
                throw new InvalidInputException(
                        "Doctor is not available at the selected date and time.");
            }
        }
    }

    public void deleteAppointment(String id) throws InvalidInputException {
        Appointment a = findAppointmentById(id);
        if (a == null) throw new InvalidInputException("Appointment not found.");
        appointments.remove(a);
        saveAllAppointments();
    }

    private void saveAllAppointments() {
        List<String> lines = new ArrayList<>();
        for (Appointment a : appointments) lines.add(a.toCSV());
        FileHandler.writeLines(APPOINTMENT_FILE, lines);
    }

    public List<Appointment> getAppointmentsForPatient(String patientId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments) if (a.getPatientId().equalsIgnoreCase(patientId)) result.add(a);
        return result;
    }

    private void validateName(String name, String role) throws InvalidInputException {
        if (name == null || name.trim().isEmpty()) throw new InvalidInputException("Invalid input: " + role + " name cannot be empty.");
        if (!Pattern.matches("[\\p{L}][\\p{L} .'-]*", name.trim()))
            throw new InvalidInputException("Invalid input: Name must contain valid characters.");
    }

    private void validateGender(String gender) throws InvalidInputException {
        if (gender == null || !(gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase("Other")))
            throw new InvalidInputException("Please select Male, Female, or Other for gender.");
    }

    private void validateSchedule(String days, String start, String end) throws InvalidInputException {
        if (days == null || days.trim().isEmpty()) throw new InvalidInputException("Select at least one available day.");
        java.util.Set<String> validDays = java.util.Set.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        for (String day : days.split(";")) if (!validDays.contains(day.trim())) throw new InvalidInputException("Invalid day: " + day + ". Use full weekday names separated by semicolons.");
        try { if (!LocalTime.parse(start).isBefore(LocalTime.parse(end))) throw new InvalidInputException("Working start time must be before end time."); }
        catch (DateTimeParseException | NullPointerException ex) { throw new InvalidInputException("Working hours must use 24-hour format, e.g. 09:00 and 17:00."); }
    }

    private void ensureDoctorSchedule(String doctorId, String date, String time) throws InvalidInputException {
        Doctor doctor = findDoctorById(doctorId);
        try {
            LocalDate dayDate = LocalDate.parse(date);
            LocalTime appointmentTime;
            try { appointmentTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)); }
            catch (DateTimeParseException e) { appointmentTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH)); }
            String dayName = dayDate.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
            boolean dayAvailable = false;
            for (String day : doctor.getAvailableDays().split(";")) if (day.trim().equalsIgnoreCase(dayName)) dayAvailable = true;
            if (!dayAvailable) throw new InvalidInputException("Doctor is not available on " + dayName + ". Available days: " + doctor.getAvailableDays().replace(';', ',') + ".");
            if (appointmentTime.isBefore(LocalTime.parse(doctor.getStartTime())) || !appointmentTime.isBefore(LocalTime.parse(doctor.getEndTime())))
                throw new InvalidInputException("Appointment is outside doctor's working hours (" + doctor.getStartTime() + "–" + doctor.getEndTime() + ").");
        } catch (DateTimeParseException ex) { throw new InvalidInputException("Enter a valid date (yyyy-MM-dd) and time."); }
    }

    private String nextId(String prefix, List<String> ids) {
        long max = 1000;
        for (String id : ids) { java.util.regex.Matcher m = Pattern.compile("(?i)^" + Pattern.quote(prefix) + "(\\d+)$").matcher(id); if (m.matches()) try { max = Math.max(max, Long.parseLong(m.group(1))); } catch (NumberFormatException ignored) {} }
        return prefix + (max + 1);
    }

    // ---------- BILL OPERATIONS ----------
    public Bill generateBill(String billId, String appointmentId, double medicineCharges)
            throws InvalidInputException {
        if (billId == null || billId.trim().isEmpty()) billId = nextId("BILL-", bills.stream().map(Bill::getBillId).toList());
        if (!Double.isFinite(medicineCharges) || medicineCharges < 0) throw new InvalidInputException("Medicine charges must be a valid non-negative number.");
        if (findBillById(billId) != null)
            throw new InvalidInputException("Bill ID " + billId + " already exists.");

        Appointment ap = findAppointmentById(appointmentId);
        if (ap == null)
            throw new InvalidInputException("Appointment not found.");

        Doctor d = findDoctorById(ap.getDoctorId());
        if (d == null)
            throw new InvalidInputException("Doctor not found for this appointment.");

        double fee = d.getConsultationFee();
        Bill b = new Bill(billId, ap.getPatientId(), appointmentId, fee, medicineCharges);
        bills.add(b);
        FileHandler.appendLine(BILL_FILE, b.toCSV());
        return b;
    }

    public Bill findBillById(String id) {
        for (Bill b : bills) {
            if (b.getBillId().equals(id)) return b;
        }
        return null;
    }

    public List<Bill> getAllBills() { return bills; }

    public void deleteBill(String billId) throws InvalidInputException {
        Bill b = findBillById(billId);
        if (b == null) throw new InvalidInputException("Bill not found.");
        bills.remove(b);
        saveAllBills();
    }

    private void saveAllBills() {
        List<String> lines = new ArrayList<>();
        for (Bill b : bills) lines.add(b.toCSV());
        FileHandler.writeLines(BILL_FILE, lines);
    }
}