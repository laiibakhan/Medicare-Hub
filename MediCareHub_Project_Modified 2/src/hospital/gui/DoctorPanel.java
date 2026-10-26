package hospital.gui;

import hospital.exception.InvalidInputException;
import hospital.model.Doctor;
import hospital.service.HospitalService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Colorful Doctors panel with Add / Update / Delete / Search / Export CSV.
 * Clicking a table row loads its data into the form fields.
 *
 * REFERENCES:
 * - JTable + DefaultTableModel pattern:
 *   https://docs.oracle.com/javase/tutorial/uiswing/components/table.html
 * - JFileChooser for save dialog:
 *   https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
 *
 * All business logic, validation, and layout is my own original work.
 */
public class DoctorPanel extends JPanel {

    private HospitalService service;
    private JTextField idF, nameF, feeF;
    private JButton daysF;
    private JComboBox<String> startTimeF, endTimeF;
    private JComboBox<String> ageF, genderF, specF;
    private JTextField searchF;
    private DefaultTableModel tableModel;
    private JTable table;
    private boolean updatingDaysChoice;
    private String selectedDaysCache = "Monday, Tuesday, Wednesday, Thursday, Friday";
    private java.util.Map<String, JCheckBox> dayChecks = new java.util.LinkedHashMap<>();

    public DoctorPanel(HospitalService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // ---------- Form ----------
        JPanel form = new JPanel(new GridLayout(0, 3, 12, 7));
        Theme.styleCard(form);

        idF = new JTextField(); Theme.styleField(idF); idF.setEditable(false); form.add(fieldCard("Doctor ID (auto):", idF));
        nameF = new JTextField(); Theme.styleField(nameF); form.add(fieldCard("Name:", nameF));
        ageF = makeAgeCombo(); form.add(fieldCard("Age:", ageF));
        genderF = new JComboBox<>(new String[]{"Select gender", "Male", "Female", "Other"}); Theme.styleCombo(genderF); form.add(fieldCard("Gender:", genderF));
        specF = new JComboBox<>(Specializations.ALL); specF.setEditable(true); Theme.styleCombo(specF); form.add(fieldCard("Specialization:", specF));
        feeF = new JTextField(); Theme.styleField(feeF); form.add(fieldCard("Fee:", feeF));
        String[] dayOptions = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        selectedDaysCache = "Monday, Tuesday, Wednesday, Thursday, Friday";
        daysF = new JButton("Monday–Friday  ▾");
        Theme.styleComboButton(daysF);
        JPopupMenu daysPopup = new JPopupMenu();
        JPanel daysPopupPanel = new JPanel(new BorderLayout(8, 8));
        daysPopupPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        daysPopupPanel.setBackground(Theme.BACKGROUND);
        JPanel checksPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        checksPanel.setBackground(Theme.BACKGROUND);
        dayChecks.clear();
        for (String day : dayOptions) {
            JCheckBox item = new JCheckBox(day, java.util.Arrays.asList(selectedDaysCache.split(", ")).contains(day));
            item.setOpaque(false);
            item.setFocusPainted(false);
            dayChecks.put(day, item);
            checksPanel.add(item);
            // Update the compact summary immediately, but keep the popup open for multi-select.
            item.addActionListener(ev -> updateSelectedDays(dayChecks));
        }
        daysPopupPanel.add(checksPanel, BorderLayout.CENTER);
        JPanel daysActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        daysActions.setOpaque(false);
        JButton clearDaysBtn = new JButton("Clear");
        JButton okDaysBtn = new JButton("OK");
        Theme.styleButton(clearDaysBtn, Theme.UTILITY);
        Theme.styleButton(okDaysBtn, Theme.ACCENT_DARK);
        clearDaysBtn.addActionListener(ev -> {
            for (JCheckBox check : dayChecks.values()) check.setSelected(false);
            updateSelectedDays(dayChecks);
        });
        okDaysBtn.addActionListener(ev -> daysPopup.setVisible(false));
        daysActions.add(clearDaysBtn);
        daysActions.add(okDaysBtn);
        daysPopupPanel.add(daysActions, BorderLayout.SOUTH);
        daysPopup.add(daysPopupPanel);
        daysF.addActionListener(ev -> {
            syncDayChecks();
            daysPopup.show(daysF, 0, daysF.getHeight());
        });
        form.add(fieldCard("Available days (select multiple):", daysF));
        String[] times = new String[29];
        int ti = 0;
        for (int h = 8; h <= 22; h++) { times[ti++] = String.format(java.util.Locale.ENGLISH, "%d:00 %s", h % 12 == 0 ? 12 : h % 12, h < 12 ? "AM" : "PM"); if (h < 22) times[ti++] = String.format(java.util.Locale.ENGLISH, "%d:30 %s", h % 12 == 0 ? 12 : h % 12, h < 12 ? "AM" : "PM"); }
        startTimeF = new JComboBox<>(times); startTimeF.setEditable(true); startTimeF.setSelectedItem("9:00 AM"); Theme.styleCombo(startTimeF); form.add(fieldCard("Start time:", startTimeF));
        endTimeF = new JComboBox<>(times); endTimeF.setEditable(true); endTimeF.setSelectedItem("5:00 PM"); Theme.styleCombo(endTimeF); form.add(fieldCard("End time:", endTimeF));

        add(form, BorderLayout.NORTH);

        // ---------- Table ----------
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Age", "Gender", "Specialization", "Fee"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        Theme.styleTable(table);
        int[] doctorWidths = {120, 220, 70, 100, 240, 100};
        for (int i = 0; i < doctorWidths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(doctorWidths[i]);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ---------- Search ----------
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchPanel.setBackground(Theme.BACKGROUND);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        searchPanel.add(makeLabel("🔍 Search by Name:"));
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

        JButton addBtn     = new JButton("＋ Add Doctor");  Theme.styleButton(addBtn, Theme.PRIMARY);
        JButton updateBtn  = new JButton("✎ Update");      Theme.styleButton(updateBtn, Theme.ACCENT_DARK);
        JButton deleteBtn  = new JButton("▣ Delete");      Theme.styleButton(deleteBtn, Theme.DANGER);
        JButton refreshBtn = new JButton("↻ Refresh");     Theme.styleButton(refreshBtn, Theme.UTILITY);
        JButton clearBtn   = new JButton("⟲ Clear");  Theme.styleButton(clearBtn, Theme.UTILITY);
        JButton exportBtn  = new JButton("⇩ Export CSV");  Theme.styleButton(exportBtn, Theme.EXPORT);

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        buttons.add(clearBtn);
        buttons.add(exportBtn);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Theme.BACKGROUND);
        bottomContainer.add(buttons, BorderLayout.SOUTH);
        JPanel topArea = new JPanel(new BorderLayout(0, 5)); topArea.setBackground(Theme.BACKGROUND);
        remove(form); topArea.add(searchPanel, BorderLayout.NORTH); topArea.add(form, BorderLayout.CENTER); add(topArea, BorderLayout.NORTH);
        add(bottomContainer, BorderLayout.SOUTH);

        // ---------- Actions ----------
        addBtn.addActionListener(e -> addDoctor());
        updateBtn.addActionListener(e -> updateDoctor());
        deleteBtn.addActionListener(e -> deleteDoctor());
        refreshBtn.addActionListener(e -> {
            refreshTable();
            idF.setText(nextDoctorId());
        });
        clearBtn.addActionListener(e -> clearForm());
        exportBtn.addActionListener(e -> exportCSV());

        // ---------- Row click → fill form ----------
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                idF.setText(tableModel.getValueAt(r, 0).toString());
                nameF.setText(tableModel.getValueAt(r, 1).toString());
                ageF.setSelectedItem(tableModel.getValueAt(r, 2).toString());
                genderF.setSelectedItem(tableModel.getValueAt(r, 3).toString());
                specF.setSelectedItem(tableModel.getValueAt(r, 4).toString());
                feeF.setText(tableModel.getValueAt(r, 5).toString());
                hospital.model.Doctor selectedDoctor = service.findDoctorById(tableModel.getValueAt(r, 0).toString());
                if (selectedDoctor != null) { selectedDaysCache = selectedDoctor.getAvailableDays().replace(";", ", "); syncDayChecks(); updateDaysButtonText(); startTimeF.setSelectedItem(to12Hour(selectedDoctor.getStartTime())); endTimeF.setSelectedItem(to12Hour(selectedDoctor.getEndTime())); }
            }
        });

        refreshTable();
        idF.setText(nextDoctorId());
    }

    private void updateSelectedDays(java.util.Map<String, JCheckBox> checks) {
        java.util.List<String> selected = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, JCheckBox> entry : checks.entrySet())
            if (entry.getValue().isSelected()) selected.add(entry.getKey());
        selectedDaysCache = String.join(", ", selected);
        updateDaysButtonText();
    }

    private void syncDayChecks() {
        java.util.List<String> selected = java.util.Arrays.asList((selectedDaysCache == null ? "" : selectedDaysCache).split(",\s*"));
        for (java.util.Map.Entry<String, JCheckBox> entry : dayChecks.entrySet())
            entry.getValue().setSelected(selected.contains(entry.getKey()));
    }

    private void updateDaysButtonText() {
        if (daysF == null) return;
        String text = formatSelectedDays(selectedDaysCache);
        daysF.setText(text + "  ▾");
        daysF.setToolTipText(selectedDaysCache == null || selectedDaysCache.isBlank()
                ? "No available days selected" : selectedDaysCache);
    }

    /** Shows consecutive weekdays as a range without changing the saved day list. */
    private String formatSelectedDays(String rawDays) {
        if (rawDays == null || rawDays.trim().isEmpty()) return "Select available days";
        List<String> week = java.util.Arrays.asList(
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        java.util.Set<String> selected = new java.util.HashSet<>();
        for (String part : rawDays.replace(';', ',').split(",\\s*")) {
            String day = part.trim();
            if (week.contains(day)) selected.add(day);
        }
        if (selected.isEmpty()) return "Select available days";
        if (selected.size() == 7) return "Every day";

        List<String> ordered = new java.util.ArrayList<>();
        for (String day : week) if (selected.contains(day)) ordered.add(day);
        List<String> displayParts = new java.util.ArrayList<>();
        int i = 0;
        while (i < week.size()) {
            if (!selected.contains(week.get(i))) { i++; continue; }
            int start = i;
            while (i + 1 < week.size() && selected.contains(week.get(i + 1))) i++;
            int end = i;
            if (end - start >= 1) displayParts.add(week.get(start) + "–" + week.get(end));
            else displayParts.add(week.get(start));
            i++;
        }
        return String.join(", ", displayParts);
    }

    private JPanel fieldCard(String label, Component input) {
        JPanel card = new JPanel(new BorderLayout(0, 3));
        card.setOpaque(false);
        JLabel title = makeLabel(label); title.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));
        card.add(title, BorderLayout.NORTH); input.setPreferredSize(new Dimension(170, 32)); card.add(input, BorderLayout.CENTER);
        return card;
    }

    private String comboText(JComboBox<String> combo) { Object value = combo.isEditable() ? combo.getEditor().getItem() : combo.getSelectedItem(); return value == null ? "" : value.toString().trim(); }
    private String to24Hour(String value) { try { return java.time.LocalTime.parse(value, java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH)).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")); } catch (Exception ignored) { return value; } }
    private String to12Hour(String value) { try { return java.time.LocalTime.parse(value, java.time.format.DateTimeFormatter.ofPattern("H:mm")).format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH)); } catch (Exception ignored) { return value; } }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_DARK);
        return l;
    }

    private JComboBox<String> makeAgeCombo() {
        String[] ages = new String[150];
        for (int i = 0; i < ages.length; i++) {
            ages[i] = Integer.toString(i + 1);
        }
        JComboBox<String> combo = new JComboBox<>(ages);
        combo.setEditable(true);
        combo.setSelectedItem("");
        Theme.styleCombo(combo);
        return combo;
    }

    private int selectedAge() {
        Object selected = ageF.getEditor().getItem();
        return Integer.parseInt(selected == null ? "" : selected.toString().trim());
    }

    private String nextDoctorId() {
        BigInteger highestId = BigInteger.valueOf(1000);
        Pattern doctorIdPattern = Pattern.compile("(?i)^(?:DOC-|D)(\\d+)$");
        for (Doctor doctor : service.getAllDoctors()) {
            Matcher matcher = doctorIdPattern.matcher(doctor.getId());
            if (matcher.matches()) {
                BigInteger numericId = new BigInteger(matcher.group(1));
                if (numericId.compareTo(highestId) > 0) {
                    highestId = numericId;
                }
            }
        }
        return "DOC-" + highestId.add(BigInteger.ONE);
    }

    private void addDoctor() {
        try {
            int age = selectedAge();
            double fee = Double.parseDouble(feeF.getText().trim());
            service.addDoctor(idF.getText().trim(), nameF.getText().trim(), age,
                    selectedGender(), selectedSpecialization(), fee, selectedDaysCache.replace(", ", ";").replace(",", ";"), to24Hour(comboText(startTimeF)), to24Hour(comboText(endTimeF)));
            showMsg("Doctor added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (NumberFormatException ex) {
            showMsg("Age and Fee must be numeric.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDoctor() {
        try {
            int age = selectedAge();
            double fee = Double.parseDouble(feeF.getText().trim());
            service.updateDoctor(idF.getText().trim(),
                    nameF.getText().trim(),
                    age,
                    selectedGender(),
                    selectedSpecialization(),
                    fee, selectedDaysCache.replace(", ", ";").replace(",", ";"), to24Hour(comboText(startTimeF)), to24Hour(comboText(endTimeF)));
            showMsg("Doctor updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (NumberFormatException ex) {
            showMsg("Age and Fee must be numeric.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDoctor() {
        try {
            service.deleteDoctor(idF.getText().trim());
            showMsg("Doctor deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String q = searchF.getText().trim().toLowerCase();
        List<Doctor> list = service.getAllDoctors();
        for (Doctor d : list) {
            if (q.isEmpty() || d.getName().toLowerCase().contains(q) || d.getId().toLowerCase().contains(q) || d.getSpecialization().toLowerCase().contains(q)) {
                tableModel.addRow(new Object[]{
                        d.getId(), displayDoctorName(d.getName()), d.getAge(), d.getGender(),
                        d.getSpecialization(), d.getConsultationFee()
                });
            }
        }
    }

    private String displayDoctorName(String name) {
        String clean = name == null ? "" : name.trim();
        clean = clean.replaceFirst("(?i)^(dr\\.?\\s*)+", "").trim();
        return "Dr. " + clean;
    }

    private void clearForm() {
        idF.setText(nextDoctorId()); nameF.setText(""); ageF.setSelectedItem("");
        genderF.setSelectedIndex(0); specF.setSelectedItem(""); feeF.setText("");
        selectedDaysCache = "Monday, Tuesday, Wednesday, Thursday, Friday"; syncDayChecks(); updateDaysButtonText(); startTimeF.setSelectedItem("9:00 AM"); endTimeF.setSelectedItem("5:00 PM");
        table.clearSelection();
    }

    private String selectedGender() {
        Object selected = genderF.getSelectedItem();
        return selected == null || "Select gender".equals(selected) ? "" : selected.toString();
    }

    private String selectedSpecialization() {
        Object selected = specF.getEditor().getItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("doctors_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                fw.write("ID,Name,Age,Gender,Specialization,Fee\n");
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