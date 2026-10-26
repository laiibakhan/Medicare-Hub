package hospital.gui;

import hospital.exception.InvalidInputException;
import hospital.model.Doctor;
import hospital.model.Patient;
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
 * Colorful Patients panel with Add / Update / Delete / Search / Export CSV.
 * Clicking a table row loads its data into the form fields.
 *
 * REFERENCES:
 * - JTable + DefaultTableModel pattern:
 *   https://docs.oracle.com/javase/tutorial/uiswing/components/table.html
 *
 * - JFileChooser for file save dialog:
 *   https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
 *
 * - Row selection listener to populate form fields (TableSelectionListener pattern):
 *   https://docs.oracle.com/javase/tutorial/uiswing/events/listdatalistener.html
 *
 * - CSV export via FileWriter (Java I/O):
 *   https://docs.oracle.com/javase/tutorial/essential/io/file.html
 *
 * All business logic, validation, and layout composition is my own original work.
 */
public class PatientPanel extends JPanel {

    private HospitalService service;
    private JTextField idF, nameF;
    private JComboBox<String> ageF;
    private JComboBox<String> genderF, diseaseF;
    private JComboBox<String> doctorIdF;
    private JTextField searchF;
    private DefaultTableModel tableModel;
    private JTable table;

    public PatientPanel(HospitalService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // ---------- Form ----------
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 7));
        Theme.styleCard(form);

        idF = new JTextField(); Theme.styleField(idF); idF.setEditable(false); form.add(fieldCard("Patient ID (auto):", idF));
        ageF = makeAgeCombo(); form.add(fieldCard("Age:", ageF));
        nameF = new JTextField(); Theme.styleField(nameF); form.add(fieldCard("Name:", nameF));
        genderF = makeGenderCombo(); form.add(fieldCard("Gender:", genderF));
        diseaseF = makeDiseaseCombo(); form.add(fieldCard("Medical Condition:", diseaseF));
        doctorIdF = new JComboBox<>();
        doctorIdF.setEditable(true);
        Theme.styleCombo(doctorIdF);
        doctorIdF.setPrototypeDisplayValue("Dr. Muhammad Hamza - Emergency Medicine Specialist");
        doctorIdF.setMaximumRowCount(12);
        form.add(fieldCard("Doctor:", doctorIdF));
        refreshDoctorChoices("");
        diseaseF.addActionListener(e -> refreshDoctorChoices(selectedDoctorId()));
        java.awt.Component diseaseEditor = diseaseF.getEditor().getEditorComponent();
        if (diseaseEditor instanceof JTextField) {
            ((JTextField) diseaseEditor).getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void changed() { SwingUtilities.invokeLater(() -> refreshDoctorChoices(selectedDoctorId())); }
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            });
        }
        doctorIdF.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                refreshDoctorChoices(selectedDoctorId());
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
        });

        add(form, BorderLayout.NORTH);

        // ---------- Table ----------
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Age", "Gender", "Medical Condition", "Doctor"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        Theme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        int[] widths = {100, 200, 70, 100, 230, 300};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
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
        searchF.addActionListener(e -> searchPatients());

        JButton searchBtn = new JButton("Search");
        Theme.styleButton(searchBtn, Theme.ACCENT_DARK);
        searchBtn.addActionListener(e -> searchPatients());
        searchPanel.add(searchBtn);
        searchF.addActionListener(e -> searchPatients());

        JButton clearSearch = new JButton("⟲ Clear");
        Theme.styleButton(clearSearch, Theme.UTILITY);
        clearSearch.addActionListener(e -> { searchF.setText(""); refreshTable(); });
        searchPanel.add(clearSearch);

        // ---------- Buttons ----------
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttons.setBackground(Theme.BACKGROUND);

        JButton addBtn     = new JButton("＋ Add");        Theme.styleButton(addBtn, Theme.PRIMARY);
        JButton updateBtn  = new JButton("✎ Update");     Theme.styleButton(updateBtn, Theme.ACCENT_DARK);
        JButton deleteBtn  = new JButton("▣ Delete");     Theme.styleButton(deleteBtn, Theme.DANGER);
        JButton refreshBtn = new JButton("↻ Refresh");    Theme.styleButton(refreshBtn, Theme.UTILITY);
        JButton clearBtn   = new JButton("⟲ Clear"); Theme.styleButton(clearBtn, Theme.UTILITY);
        JButton exportBtn  = new JButton("⇩ Export CSV"); Theme.styleButton(exportBtn, Theme.EXPORT);

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        buttons.add(clearBtn);
        buttons.add(exportBtn);
//        JButton newBtn = new JButton("New Button");
//        Theme.styleButton(newBtn, Theme.INFO);
//        newBtn.addActionListener(e -> {
//            JOptionPane.showMessageDialog(this, "Button clicked!");
//        });
//        buttons.add(newBtn);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Theme.BACKGROUND);
        bottomContainer.add(buttons, BorderLayout.SOUTH);
        JPanel topArea = new JPanel(new BorderLayout(0, 5));
        topArea.setBackground(Theme.BACKGROUND);
        remove(form);
        topArea.add(searchPanel, BorderLayout.NORTH);
        topArea.add(form, BorderLayout.CENTER);
        add(topArea, BorderLayout.NORTH);
        add(bottomContainer, BorderLayout.SOUTH);

        // ---------- Actions ----------
        addBtn.addActionListener(e -> addPatient());
        updateBtn.addActionListener(e -> updatePatient());
        deleteBtn.addActionListener(e -> deletePatient());
        refreshBtn.addActionListener(e -> {
            searchF.setText("");
            refreshTable();
            idF.setText(nextPatientId());
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
                diseaseF.setSelectedItem(tableModel.getValueAt(r, 4).toString());
                Patient selectedPatient = service.findPatientById(tableModel.getValueAt(r, 0).toString());
                refreshDoctorChoices(selectedPatient == null ? "" : selectedPatient.getAssignedDoctorId());
            }
        });

        refreshTable();
        idF.setText(nextPatientId());
    }

    private JPanel fieldCard(String label, Component input) {
        JPanel card = new JPanel(new BorderLayout(0, 3));
        card.setOpaque(false);
        JLabel title = makeLabel(label);
        title.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));
        card.add(title, BorderLayout.NORTH);
        input.setPreferredSize(new Dimension(180, 32));
        card.add(input, BorderLayout.CENTER);
        return card;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_DARK);
        return l;
    }

    private JComboBox<String> makeGenderCombo() {
        JComboBox<String> combo = new JComboBox<>(new String[]{"Select gender", "Male", "Female", "Other"});
        Theme.styleCombo(combo);
        return combo;
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

    private String nextPatientId() {
        BigInteger highestId = BigInteger.valueOf(1000);
        Pattern patientIdPattern = Pattern.compile("(?i)^(?:PAT-|P)(\\d+)$");
        for (Patient patient : service.getAllPatients()) {
            Matcher matcher = patientIdPattern.matcher(patient.getId());
            if (matcher.matches()) {
                BigInteger numericId = new BigInteger(matcher.group(1));
                if (numericId.compareTo(highestId) > 0) {
                    highestId = numericId;
                }
            }
        }
        return "PAT-" + highestId.add(BigInteger.ONE);
    }

    private JComboBox<String> makeDiseaseCombo() {
        JComboBox<String> combo = new JComboBox<>(new String[]{
                "Fever", "Flu", "Common cold", "Cough", "Headache", "Migraine",
                "Hypertension", "Heart disease", "Diabetes", "Thyroid disorder", "Asthma", "Bronchitis",
                "Skin rash", "Acne", "Root Canal", "Toothache", "Gum disease", "Ear infection",
                "Eye problem", "Stomach pain", "Kidney disease", "Joint pain", "Fracture", "Anxiety", "Cancer", "Infection", "Other"
        });
        combo.setEditable(true);
        Theme.styleCombo(combo);
        return combo;
    }

    private String selectedDisease() {
        Object selected = diseaseF.getEditor().getItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private void refreshDoctorChoices(String selectedId) {
        doctorIdF.removeAllItems();
        doctorIdF.addItem("Select doctor");
        java.util.List<Doctor> recommended = new java.util.ArrayList<>(service.getAllDoctors());
        String condition = selectedDisease().toLowerCase();
        boolean dentalCondition = condition.contains("root canal") || condition.contains("tooth") || condition.contains("teeth") || condition.contains("dental") || condition.contains("gum") || condition.contains("toothache");
        if (dentalCondition) recommended.removeIf(d -> !(d.getSpecialization().toLowerCase().contains("dent") || d.getSpecialization().toLowerCase().contains("oral")));
        recommended.sort((a, b) -> Integer.compare(relevance(b, condition), relevance(a, condition)));
        for (Doctor doctor : recommended) doctorIdF.addItem(doctorLabel(doctor));
        if (selectedId == null || selectedId.isEmpty()) {
            doctorIdF.setSelectedItem("Select doctor");
            return;
        }
        Doctor selectedDoctor = service.findDoctorById(selectedId);
        if (selectedDoctor == null) {
            for (Doctor doctor : service.getAllDoctors()) if (doctorLabel(doctor).equalsIgnoreCase(selectedId)) { selectedDoctor = doctor; break; }
        }
        doctorIdF.setSelectedItem(selectedDoctor == null ? selectedId : doctorLabel(selectedDoctor));
    }

    private int relevance(Doctor doctor, String condition) {
        if (condition.isEmpty()) return 0;
        String spec = doctor.getSpecialization().toLowerCase();
        if ((condition.contains("root canal") || condition.contains("tooth") || condition.contains("teeth") || condition.contains("dental") || condition.contains("gum") || condition.contains("toothache")) && (spec.contains("dent") || spec.contains("oral"))) return 20;
        if ((condition.contains("heart") || condition.contains("cardiac") || condition.contains("blood pressure") || condition.contains("hypertension")) && spec.contains("cardio")) return 10;
        if ((condition.contains("diabetes") || condition.contains("thyroid") || condition.contains("hormone")) && spec.contains("endocrin")) return 10;
        if ((condition.contains("skin") || condition.contains("rash") || condition.contains("acne")) && spec.contains("dermat")) return 10;
        if ((condition.contains("brain") || condition.contains("migraine") || condition.contains("headache") || condition.contains("nerve")) && spec.contains("neuro")) return 10;
        if ((condition.contains("bone") || condition.contains("joint") || condition.contains("fracture") || condition.contains("back pain")) && spec.contains("ortho")) return 10;
        if ((condition.contains("fever") || condition.contains("infection") || condition.contains("flu")) && (spec.contains("general") || spec.contains("infectious"))) return 8;
        if (spec.contains("general physician")) return 2;
        return 0;
    }

    private String doctorLabel(Doctor doctor) {
        String name = doctor.getName() == null ? "" : doctor.getName().trim();
        name = name.replaceFirst("(?i)^(dr\\.?\\s*)+", "").trim();
        return "Dr. " + name + " - " + doctor.getSpecialization();
    }

    private String selectedDoctorId() {
        Object selected = doctorIdF.getEditor().getItem();
        String value = selected == null ? "" : selected.toString().trim();
        if (value.isEmpty() || "Select doctor".equals(value)) {
            return "";
        }
        for (Doctor doctor : service.getAllDoctors()) {
            if (doctorLabel(doctor).equals(value) || doctor.getName().equalsIgnoreCase(value)) {
                return doctor.getId();
            }
        }
        return value;
    }

    private int selectedAge() {
        Object selected = ageF.getEditor().getItem();
        return Integer.parseInt(selected == null ? "" : selected.toString().trim());
    }

    private void addPatient() {
        try {
            int age = selectedAge();
            service.addPatient(idF.getText().trim(), nameF.getText().trim(), age,
                    selectedGender(), selectedDisease(), selectedDoctorId());
            showMsg("Patient added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (NumberFormatException ex) {
            showMsg("Age must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePatient() {
        try {
            int age = selectedAge();
            service.updatePatient(idF.getText().trim(),
                    nameF.getText().trim(),
                    age,
                    selectedGender(),
                    selectedDisease(),
                    selectedDoctorId());
            showMsg("Patient updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (NumberFormatException ex) {
            showMsg("Age must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePatient() {
        try {
            service.deletePatient(idF.getText().trim());
            showMsg("Patient deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String q = searchF.getText().trim().toLowerCase();
        List<Patient> list = service.getAllPatients();
        for (Patient p : list) {
            if (q.isEmpty() || p.getName().toLowerCase().contains(q) || p.getId().toLowerCase().contains(q) || p.getDisease().toLowerCase().contains(q)) {
                tableModel.addRow(new Object[]{
                        p.getId(), p.getName(), p.getAge(), p.getGender(),
                        p.getDisease(), doctorDisplayName(p.getAssignedDoctorId())
                });
            }
        }
    }

    private String doctorDisplayName(String doctorId) {
        Doctor doctor = service.findDoctorById(doctorId);
        if (doctor == null) return doctorId == null ? "" : doctorId;
        return doctorLabel(doctor);
    }

    private void searchPatients() {
        refreshTable();
        String query = searchF.getText().trim();
        if (!query.isEmpty() && tableModel.getRowCount() == 0) {
            showMsg("No patient record found for \"" + query + "\".",
                    "No Records Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearForm() {
        idF.setText(nextPatientId()); nameF.setText(""); ageF.setSelectedItem("");
        genderF.setSelectedIndex(0); diseaseF.setSelectedItem(""); refreshDoctorChoices("");
        table.clearSelection();
    }

    private String selectedGender() {
        Object selected = genderF.getSelectedItem();
        return selected == null || "Select gender".equals(selected) ? "" : selected.toString();
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("patients_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                fw.write("ID,Name,Age,Gender,Medical Condition,Doctor\n");
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