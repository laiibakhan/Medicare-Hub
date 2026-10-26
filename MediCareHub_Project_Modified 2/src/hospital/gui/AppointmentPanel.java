package hospital.gui;

import hospital.exception.InvalidInputException;
import hospital.model.Appointment;
import hospital.model.Doctor;
import hospital.model.Patient;
import hospital.service.HospitalService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Colorful Appointments panel with Book / Update / Delete / Search / Export CSV.
 * Clicking a table row loads its data into the form fields.
 * All logic in this class is my own original work.
 */
public class AppointmentPanel extends JPanel {

    private HospitalService service;
    private JTextField apptIdF;
    private JComboBox<String> patientIdF, doctorNameF, specializationF;
    private JTextField dateF;
    private JComboBox<String> timeF;
    private JPopupMenu datePopup;
    private JLabel calendarMonthLabel;
    private JPanel calendarDays;
    private YearMonth calendarMonth;
    private JLabel patientDetails, doctorDetails;
    private boolean updatingDoctorChoices;
    private JTextField searchF;
    private DefaultTableModel tableModel;
    private JTable table;

    public AppointmentPanel(HospitalService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // ---------- Form ----------
        JPanel form = new JPanel(new GridLayout(3, 4, 8, 5));
        Theme.styleCard(form);

        form.add(makeLabel("Appointment ID:"));
        apptIdF = new JTextField(); Theme.styleField(apptIdF); apptIdF.setEditable(false); form.add(apptIdF);

        form.add(makeLabel("Patient ID:"));
        patientIdF = new JComboBox<>(); patientIdF.setEditable(true); Theme.styleCombo(patientIdF);
        form.add(patientIdF);

        form.add(makeLabel("Doctor:"));
        doctorNameF = new JComboBox<>(); doctorNameF.setEditable(true); Theme.styleCombo(doctorNameF);
        doctorNameF.setPrototypeDisplayValue("Dr. Muhammad Hamza - Emergency Medicine Specialist");
        doctorNameF.setMaximumRowCount(12);
        form.add(doctorNameF);

        form.add(makeLabel("Specialization:"));
        specializationF = new JComboBox<>(); specializationF.setEditable(true);
        Theme.styleCombo(specializationF); form.add(specializationF);

        form.add(makeLabel("Date:"));
        dateF = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        Theme.styleField(dateF);
        dateF.setEditable(false);
        dateF.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton calendarButton = new JButton("▼");
        Theme.styleButton(calendarButton, Theme.ACCENT_DARK);
        calendarButton.setMargin(new Insets(4, 10, 4, 10));
        JPanel datePicker = new JPanel(new BorderLayout(5, 0));
        datePicker.setOpaque(false);
        datePicker.add(dateF, BorderLayout.CENTER);
        datePicker.add(calendarButton, BorderLayout.EAST);
        form.add(datePicker);
        createCalendarPopup();
        dateF.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                showCalendar();
            }
        });
        calendarButton.addActionListener(e -> showCalendar());

        form.add(makeLabel("Time:"));
        timeF = new JComboBox<>(availableTimes()); Theme.styleCombo(timeF); form.add(timeF);

        refreshPatientChoices("");
        refreshSpecializationChoices("");
        refreshDoctorChoices("");

        JPanel details = new JPanel(new GridLayout(1, 2, 12, 0));
        details.setOpaque(false);
        details.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        patientDetails = makeDetailsLabel();
        doctorDetails = makeDetailsLabel();
        details.add(makeDetailsCard("PATIENT RECORD", patientDetails));
        details.add(makeDetailsCard("DOCTOR RECORD", doctorDetails));

        JPanel formArea = new JPanel(new BorderLayout(0, 5));
        formArea.setOpaque(false);
        formArea.add(form, BorderLayout.NORTH);
        formArea.add(details, BorderLayout.CENTER);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(formArea, BorderLayout.NORTH);
        add(north, BorderLayout.NORTH);

        patientIdF.addActionListener(e -> updatePatientDetails());
        patientIdF.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                refreshPatientChoices(selectedPatientId());
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
        });
        patientIdF.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                updatePatientDetails();
            }
        });
        specializationF.addActionListener(e -> {
            if (!updatingDoctorChoices) {
                refreshDoctorChoices(selectedDoctorName());
            }
        });
        doctorNameF.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                refreshSpecializationChoices(selectedSpecialization());
                refreshDoctorChoices(selectedDoctorName());
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
        });
        doctorNameF.addActionListener(e -> updateDoctorDetails());
        doctorNameF.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                updateDoctorDetails();
            }
        });

        // ---------- Table ----------
        tableModel = new DefaultTableModel(
                new String[]{"Appointment ID", "Patient ID", "Doctor ID", "Date", "Time", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        Theme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ---------- Search ----------
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchPanel.setBackground(Theme.BACKGROUND);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        searchPanel.add(makeLabel("🔍 Search by Patient ID:"));
        searchF = new JTextField(20);
        Theme.styleField(searchF);
        searchPanel.add(searchF);
        searchF.addActionListener(e -> refreshTable());

        JButton searchBtn = new JButton("Search");
        Theme.styleButton(searchBtn, Theme.ACCENT_DARK);
        searchBtn.addActionListener(e -> refreshTable());
        searchPanel.add(searchBtn);

        JButton clearSearch = new JButton("⟲ Clear");
        Theme.styleButton(clearSearch, Theme.UTILITY);
        clearSearch.addActionListener(e -> { searchF.setText(""); refreshTable(); });
        searchPanel.add(clearSearch);

        // ---------- Buttons ----------
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttons.setBackground(Theme.BACKGROUND);

        JButton bookBtn    = new JButton("Book");       Theme.styleButton(bookBtn, Theme.PRIMARY);
        JButton updateBtn  = new JButton("✎ Update");     Theme.styleButton(updateBtn, Theme.ACCENT_DARK);
        JButton deleteBtn  = new JButton("▣ Delete");     Theme.styleButton(deleteBtn, Theme.DANGER);
        JButton refreshBtn = new JButton("↻ Refresh");    Theme.styleButton(refreshBtn, Theme.UTILITY);
        JButton clearBtn   = new JButton("⟲ Clear"); Theme.styleButton(clearBtn, Theme.UTILITY);
        JButton exportBtn  = new JButton("⇩ Export CSV"); Theme.styleButton(exportBtn, Theme.EXPORT);

        buttons.add(bookBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        buttons.add(clearBtn);
        buttons.add(exportBtn);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Theme.BACKGROUND);
        bottomContainer.add(buttons, BorderLayout.SOUTH);
        JPanel topArea = new JPanel(new BorderLayout(0, 10));
        topArea.setBackground(Theme.BACKGROUND);
        remove(north);
        topArea.add(searchPanel, BorderLayout.NORTH);
        topArea.add(formArea, BorderLayout.CENTER);
        add(topArea, BorderLayout.NORTH);
        add(bottomContainer, BorderLayout.SOUTH);

        // ---------- Actions ----------
        bookBtn.addActionListener(e -> book());
        updateBtn.addActionListener(e -> updateAppointment());
        deleteBtn.addActionListener(e -> deleteAppointment());
        refreshBtn.addActionListener(e -> refreshTable());
        clearBtn.addActionListener(e -> clearForm());
        exportBtn.addActionListener(e -> exportCSV());

        // ---------- Row click → fill form ----------
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                apptIdF.setText(tableModel.getValueAt(r, 0).toString());
                selectComboValue(patientIdF, tableModel.getValueAt(r, 1).toString());
                updatePatientDetails();
                Doctor doctor = service.findDoctorById(tableModel.getValueAt(r, 2).toString());
                selectComboValue(specializationF,
                        doctor == null ? "" : doctor.getSpecialization());
                refreshDoctorChoices(doctor == null ? tableModel.getValueAt(r, 2).toString()
                        : doctor.getName());
                updateDoctorDetails();
                dateF.setText(tableModel.getValueAt(r, 3).toString());
                selectComboValue(timeF, tableModel.getValueAt(r, 4).toString());
            }
        });

        refreshTable();
        apptIdF.setText(nextAppointmentId());
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_DARK);
        return l;
    }

    private JLabel makeDetailsLabel() {
        JLabel label = new JLabel("Select a record to view saved details");
        label.setFont(Theme.FONT_SMALL);
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }

    private JPanel makeDetailsCard(String title, JLabel detailsLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(9, 12, 11, 12)));
        card.setBackground(Theme.CARD);
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        heading.setForeground(Theme.ACCENT_DARK);
        card.add(heading, BorderLayout.NORTH);
        card.add(detailsLabel, BorderLayout.CENTER);
        return card;
    }

    private void refreshPatientChoices(String selectedId) {
        patientIdF.removeAllItems();
        patientIdF.addItem("Select patient ID");
        for (Patient patient : service.getAllPatients()) {
            patientIdF.addItem(patient.getId());
        }
        patientIdF.setSelectedItem(selectedId == null || selectedId.isEmpty()
                ? "Select patient ID" : selectedId);
    }

    private void refreshSpecializationChoices(String selectedSpecialization) {
        Set<String> specializations = new LinkedHashSet<>();
        for (String specialization : Specializations.ALL) {
            specializations.add(specialization);
        }
        for (Doctor doctor : service.getAllDoctors()) {
            if (doctor.getSpecialization() != null && !doctor.getSpecialization().trim().isEmpty()) {
                specializations.add(doctor.getSpecialization().trim());
            }
        }
        updatingDoctorChoices = true;
        specializationF.removeAllItems();
        specializationF.addItem("All specializations");
        for (String specialization : specializations) {
            specializationF.addItem(specialization);
        }
        specializationF.setSelectedItem(
                selectedSpecialization == null || selectedSpecialization.isEmpty()
                        ? "All specializations" : selectedSpecialization);
        updatingDoctorChoices = false;
    }

    private void refreshDoctorChoices(String selectedName) {
        Object specializationSelection = specializationF.getEditor().getItem();
        String specialization = specializationSelection == null
                ? "" : specializationSelection.toString().trim();
        boolean filterBySpecialization = !specialization.isEmpty()
                && !"All specializations".equalsIgnoreCase(specialization);

        updatingDoctorChoices = true;
        doctorNameF.removeAllItems();
        doctorNameF.addItem("Select doctor");
        boolean selectedDoctorAvailable = false;
        for (Doctor doctor : service.getAllDoctors()) {
            if (!filterBySpecialization
                    || doctor.getSpecialization().equalsIgnoreCase(specialization)) {
                String displayName = displayDoctorName(doctor.getName());
                doctorNameF.addItem(displayName);
                if (displayName.equalsIgnoreCase(selectedName) || doctor.getName().equalsIgnoreCase(selectedName)) {
                    selectedDoctorAvailable = true;
                }
            }
        }
        doctorNameF.setSelectedItem(selectedDoctorAvailable ? selectedName : "Select doctor");
        updatingDoctorChoices = false;
    }

    private String displayDoctorName(String name) {
        String clean = name == null ? "" : name.trim();
        clean = clean.replaceFirst("(?i)^(dr\\.?\\s*)+", "").trim();
        return "Dr. " + clean;
    }

    private String selectedDoctorName() {
        Object selected = doctorNameF.getEditor().getItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private String selectedDoctorId() {
        Doctor doctor = findSelectedDoctor();
        if (doctor != null) {
            return doctor.getId();
        }
        String typed = selectedDoctorName();
        Doctor doctorById = service.findDoctorById(typed);
        return doctorById == null ? "" : doctorById.getId();
    }

    private Doctor findSelectedDoctor() {
        String selected = selectedDoctorName();
        for (Doctor doctor : service.getAllDoctors()) {
            if (doctor.getName().equalsIgnoreCase(selected)
                    || displayDoctorName(doctor.getName()).equalsIgnoreCase(selected)
                    || doctor.getId().equalsIgnoreCase(selected)) {
                return doctor;
            }
        }
        return null;
    }

    private void updatePatientDetails() {
        Object selected = patientIdF.getEditor().getItem();
        String patientId = selected == null ? "" : selected.toString().trim();
        Patient patient = service.findPatientById(patientId);
        if (patient == null) {
            patientDetails.setText(patientId.isEmpty() || "Select patient ID".equals(patientId)
                    ? "Select a patient to view saved details" : "No saved patient matches this ID");
            return;
        }
        patientDetails.setText("<html><b>" + escapeHtml(patient.getName()) + "</b>"
                + " &nbsp;|&nbsp; Age " + patient.getAge()
                + " &nbsp;|&nbsp; " + escapeHtml(patient.getGender())
                + "<br>Medical Condition: " + escapeHtml(patient.getDisease())
                + " &nbsp;|&nbsp; Assigned doctor: "
                + escapeHtml(patient.getAssignedDoctorId()) + "<br><b>Appointment history:</b> "
                + escapeHtml(patientHistorySummary(patientId)) + "</html>");

        Doctor assignedDoctor = service.findDoctorById(patient.getAssignedDoctorId());
        if (assignedDoctor != null) {
            refreshSpecializationChoices(assignedDoctor.getSpecialization());
            refreshDoctorChoices(assignedDoctor.getName());
            updateDoctorDetails();
        }
    }

    private String patientHistorySummary(String patientId) {
        List<Appointment> history = service.getAppointmentsForPatient(patientId);
        if (history.isEmpty()) return "No previous appointments";
        StringBuilder result = new StringBuilder();
        int shown = 0;
        for (Appointment a : history) {
            if (shown++ > 0) result.append("; ");
            result.append(a.getDate()).append(" ").append(a.getTime()).append(" (").append(a.getStatus()).append(")");
            if (shown >= 3) break;
        }
        return result.toString();
    }

    private void updateDoctorDetails() {
        Doctor doctor = findSelectedDoctor();
        if (doctor == null) {
            doctorDetails.setText(selectedDoctorName().isEmpty()
                    || "Select doctor".equals(selectedDoctorName())
                    ? "Select a doctor to view saved details"
                    : "No saved doctor matches this name");
            return;
        }
        doctorDetails.setText("<html><b>" + escapeHtml(displayDoctorName(doctor.getName())) + "</b>"
                + " &nbsp;|&nbsp; ID " + escapeHtml(doctor.getId())
                + " &nbsp;|&nbsp; Age " + doctor.getAge()
                + " &nbsp;|&nbsp; " + escapeHtml(doctor.getGender())
                + "<br>" + escapeHtml(doctor.getSpecialization())
                + " &nbsp;|&nbsp; Consultation fee: "
                + String.format("%.2f", doctor.getConsultationFee())
                + "<br>Available: " + escapeHtml(doctor.getAvailableDays().replace(';', ','))
                + " | " + escapeHtml(to12Hour(doctor.getStartTime())) + "–" + escapeHtml(to12Hour(doctor.getEndTime())) + "</html>");
    }

    private String to12Hour(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        String time = value.trim();
        for (java.time.format.DateTimeFormatter formatter : new java.time.format.DateTimeFormatter[]{
                java.time.format.DateTimeFormatter.ofPattern("H:mm"),
                java.time.format.DateTimeFormatter.ofPattern("HH:mm"),
                java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH),
                java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH)
        }) {
            try {
                return java.time.LocalTime.parse(time, formatter)
                        .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH));
            } catch (java.time.format.DateTimeParseException ignored) {
                // Try the next supported time format.
            }
        }
        return time;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String nextAppointmentId() {
        BigInteger highestId = BigInteger.valueOf(1000);
        Pattern idPattern = Pattern.compile("(?i)^(?:APT-|A)(\\d+)$");
        for (Appointment appointment : service.getAllAppointments()) {
            Matcher matcher = idPattern.matcher(appointment.getAppointmentId());
            if (matcher.matches()) {
                BigInteger numericId = new BigInteger(matcher.group(1));
                if (numericId.compareTo(highestId) > 0) {
                    highestId = numericId;
                }
            }
        }
        return "APT-" + highestId.add(BigInteger.ONE);
    }

    private String[] availableTimes() {
        String[] times = new String[61];
        for (int i = 0; i < times.length; i++) {
            int totalMinutes = 8 * 60 + i * 15;
            int hour = (totalMinutes / 60) % 12;
            if (hour == 0) {
                hour = 12;
            }
            String meridiem = totalMinutes < 12 * 60 ? "AM" : "PM";
            times[i] = String.format("%d:%02d %s", hour, totalMinutes % 60, meridiem);
        }
        return times;
    }

    private void createCalendarPopup() {
        datePopup = new JPopupMenu();
        JPanel calendar = new JPanel(new BorderLayout(6, 8));
        calendar.setBackground(Color.WHITE);
        calendar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel navigation = new JPanel(new BorderLayout());
        navigation.setOpaque(false);
        JButton previous = new JButton("<");
        JButton next = new JButton(">");
        styleCalendarNavigation(previous);
        styleCalendarNavigation(next);
        previous.addActionListener(e -> {
            calendarMonth = calendarMonth.minusMonths(1);
            refreshCalendarDays();
        });
        next.addActionListener(e -> {
            calendarMonth = calendarMonth.plusMonths(1);
            refreshCalendarDays();
        });
        calendarMonthLabel = new JLabel("", SwingConstants.CENTER);
        calendarMonthLabel.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));
        calendarMonthLabel.setForeground(Theme.TEXT_DARK);
        navigation.add(previous, BorderLayout.WEST);
        navigation.add(calendarMonthLabel, BorderLayout.CENTER);
        navigation.add(next, BorderLayout.EAST);
        calendar.add(navigation, BorderLayout.NORTH);

        JPanel calendarBody = new JPanel(new BorderLayout(0, 5));
        calendarBody.setOpaque(false);
        JPanel weekdays = new JPanel(new GridLayout(1, 7, 2, 2));
        weekdays.setOpaque(false);
        for (String weekday : new String[]{"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"}) {
            JLabel label = new JLabel(weekday, SwingConstants.CENTER);
            label.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
            label.setForeground(Theme.TEXT_MUTED);
            weekdays.add(label);
        }
        calendarBody.add(weekdays, BorderLayout.NORTH);
        calendarDays = new JPanel(new GridLayout(0, 7, 2, 2));
        calendarDays.setOpaque(false);
        calendarBody.add(calendarDays, BorderLayout.CENTER);
        calendar.add(calendarBody, BorderLayout.CENTER);
        calendar.setPreferredSize(new Dimension(280, 250));
        datePopup.add(calendar);
    }

    private void styleCalendarNavigation(JButton button) {
        button.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));
        button.setForeground(Theme.PRIMARY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void showCalendar() {
        try {
            calendarMonth = YearMonth.from(LocalDate.parse(dateF.getText()));
        } catch (java.time.format.DateTimeParseException ex) {
            calendarMonth = YearMonth.from(LocalDate.now());
        }
        refreshCalendarDays();
        datePopup.show(dateF.getParent(), 0, dateF.getParent().getHeight());
    }

    private void refreshCalendarDays() {
        calendarMonthLabel.setText(calendarMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy")));
        calendarDays.removeAll();

        LocalDate firstDay = calendarMonth.atDay(1);
        for (int blank = 1; blank < firstDay.getDayOfWeek().getValue(); blank++) {
            calendarDays.add(new JLabel());
        }

        LocalDate today = LocalDate.now();
        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(dateF.getText());
        } catch (java.time.format.DateTimeParseException ex) {
            selectedDate = today;
        }

        for (int day = 1; day <= calendarMonth.lengthOfMonth(); day++) {
            LocalDate date = calendarMonth.atDay(day);
            JButton dayButton = new JButton(Integer.toString(day));
            dayButton.setFont(Theme.FONT_SMALL);
            dayButton.setFocusPainted(false);
            dayButton.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
            dayButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dayButton.setEnabled(!date.isBefore(today));
            dayButton.setBackground(date.equals(selectedDate)
                    ? Theme.INFO : Color.WHITE);
            dayButton.setForeground(date.equals(selectedDate)
                    ? Color.WHITE : Theme.TEXT_DARK);
            dayButton.addActionListener(e -> {
                dateF.setText(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                datePopup.setVisible(false);
            });
            calendarDays.add(dayButton);
        }
        calendarDays.revalidate();
        calendarDays.repaint();
    }

    private void selectComboValue(JComboBox<String> combo, String value) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equals(value)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.addItem(value);
        combo.setSelectedItem(value);
    }

    private void book() {
        try {
            // Refresh the ID immediately before booking so a stale form value cannot
            // cause a duplicate-ID error after another appointment has been added.
            apptIdF.setText(nextAppointmentId());
            service.bookAppointment(apptIdF.getText().trim(),
                    selectedPatientId(),
                    selectedDoctorId(),
                    dateF.getText().trim(),
                    timeF.getSelectedItem().toString());
            showMsg("Appointment booked successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAppointment() {
        try {
            service.updateAppointment(apptIdF.getText().trim(),
                    selectedPatientId(),
                    selectedDoctorId(),
                    dateF.getText().trim(),
                    timeF.getSelectedItem().toString(),
                    "Booked");
            showMsg("Appointment updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAppointment() {
        try {
            service.deleteAppointment(apptIdF.getText().trim());
            showMsg("Appointment deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String q = searchF.getText().trim().toLowerCase();
        List<Appointment> list = service.getAllAppointments();
        for (Appointment a : list) {
            if (q.isEmpty() || a.getPatientId().toLowerCase().contains(q) || a.getAppointmentId().toLowerCase().contains(q) || a.getDoctorId().toLowerCase().contains(q) || a.getDate().toLowerCase().contains(q) || a.getStatus().toLowerCase().contains(q)) {
                tableModel.addRow(new Object[]{
                        a.getAppointmentId(), a.getPatientId(), a.getDoctorId(),
                        a.getDate(), a.getTime(), a.getStatus()
                });
            }
        }
    }

    private String selectedPatientId() {
        Object selected = patientIdF.getEditor().getItem();
        String patientId = selected == null ? "" : selected.toString().trim();
        return "Select patient ID".equals(patientId) ? "" : patientId;
    }

    private String selectedSpecialization() {
        Object selected = specializationF.getEditor().getItem();
        String specialization = selected == null ? "" : selected.toString().trim();
        return "All specializations".equalsIgnoreCase(specialization) ? "" : specialization;
    }

    private void clearForm() {
        apptIdF.setText(nextAppointmentId());
        refreshPatientChoices("");
        patientDetails.setText("Select a patient to view saved details");
        refreshSpecializationChoices("");
        refreshDoctorChoices("");
        doctorDetails.setText("Select a doctor to view saved details");
        dateF.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (timeF.getItemCount() > 0) timeF.setSelectedIndex(0);
        table.clearSelection();
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("appointments_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                fw.write("ApptID,PatientID,DoctorID,Date,Time,Status\n");
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        fw.write(tableModel.getValueAt(r, c).toString());
                        if (c < tableModel.getColumnCount() - 1) fw.write(",");
                    }
                    fw.write("\n");
                }
                showMsg("Exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                showMsg("Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showMsg(String msg, String title, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }
}