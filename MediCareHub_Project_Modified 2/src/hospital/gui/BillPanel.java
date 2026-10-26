package hospital.gui;

import hospital.exception.InvalidInputException;
import hospital.model.Appointment;
import hospital.model.Bill;
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
 * Colorful Bills panel with Generate / Delete / Search / Export CSV.
 * Clicking a table row loads its data into the form fields.
 * All logic in this class is my own original work.
 */
public class BillPanel extends JPanel {

    private HospitalService service;
    private JTextField billIdF, medicineF;
    private JComboBox<String> apptIdF, patientIdF;
    private JTextField searchF;
    private DefaultTableModel tableModel;
    private JTable table;
    private boolean refreshingAppointmentChoices;
    private boolean syncingLinkedIds;

    public BillPanel(HospitalService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ---------- Form ----------
        JPanel form = new JPanel(new GridLayout(2, 4, 8, 8));
        Theme.styleCard(form);

        form.add(makeLabel("Bill ID:"));
        billIdF = new JTextField(); Theme.styleField(billIdF); billIdF.setEditable(false); form.add(billIdF);

        form.add(makeLabel("Appointment ID:"));
        apptIdF = new JComboBox<>();
        apptIdF.setEditable(true);
        Theme.styleCombo(apptIdF);
        form.add(apptIdF);
        refreshAppointmentChoices("");

        form.add(makeLabel("Patient ID:"));
        patientIdF = new JComboBox<>();
        patientIdF.setEditable(true);
        Theme.styleCombo(patientIdF);
        form.add(patientIdF);
        refreshPatientChoices("");

        form.add(makeLabel("Medicine Charges:"));
        medicineF = new JTextField(); Theme.styleField(medicineF); form.add(medicineF);

        add(form, BorderLayout.NORTH);

        apptIdF.addActionListener(e -> {
            if (!refreshingAppointmentChoices && !syncingLinkedIds) {
                syncPatientFromAppointment();
            }
        });
        apptIdF.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                syncPatientFromAppointment();
            }
        });
        apptIdF.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                refreshAppointmentChoices(selectedAppointmentId());
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
        });
        patientIdF.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                refreshPatientChoices(selectedPatientId());
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
        });
        patientIdF.addActionListener(e -> {
            if (!syncingLinkedIds) {
                syncAppointmentFromPatient();
            }
        });
        patientIdF.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                syncAppointmentFromPatient();
            }
        });

        // ---------- Table ----------
        tableModel = new DefaultTableModel(
                new String[]{"Bill ID", "Patient ID", "Appointment ID",
                        "Consultation Fee", "Medicine Charges", "Total"}, 0) {
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

        JButton genBtn     = new JButton("Generate Bill"); Theme.styleButton(genBtn, Theme.PRIMARY);
        JButton deleteBtn  = new JButton("▣ Delete");        Theme.styleButton(deleteBtn, Theme.DANGER);
        JButton refreshBtn = new JButton("↻ Refresh");       Theme.styleButton(refreshBtn, Theme.UTILITY);
        JButton clearBtn   = new JButton("⟲ Clear");    Theme.styleButton(clearBtn, Theme.UTILITY);
        JButton exportBtn  = new JButton("⇩ Export CSV");    Theme.styleButton(exportBtn, Theme.EXPORT);

        buttons.add(genBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        buttons.add(clearBtn);
        buttons.add(exportBtn);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Theme.BACKGROUND);
        bottomContainer.add(buttons, BorderLayout.SOUTH);
        JPanel topArea = new JPanel(new BorderLayout(0, 10));
        topArea.setBackground(Theme.BACKGROUND);
        remove(form);
        topArea.add(searchPanel, BorderLayout.NORTH);
        topArea.add(form, BorderLayout.CENTER);
        add(topArea, BorderLayout.NORTH);
        add(bottomContainer, BorderLayout.SOUTH);

        // ---------- Actions ----------
        genBtn.addActionListener(e -> generate());
        deleteBtn.addActionListener(e -> deleteBill());
        refreshBtn.addActionListener(e -> {
            refreshTable();
            billIdF.setText(nextBillId());
        });
        clearBtn.addActionListener(e -> clearForm());
        exportBtn.addActionListener(e -> exportCSV());

        // ---------- Row click → fill form ----------
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                billIdF.setText(tableModel.getValueAt(r, 0).toString());
                refreshAppointmentChoices(tableModel.getValueAt(r, 2).toString());
                refreshPatientChoices(tableModel.getValueAt(r, 1).toString());
                medicineF.setText(tableModel.getValueAt(r, 4).toString());
            }
        });

        refreshTable();
        billIdF.setText(nextBillId());
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_DARK);
        return l;
    }

    private String nextBillId() {
        BigInteger highestId = BigInteger.valueOf(1000);
        Pattern idPattern = Pattern.compile("(?i)^(?:BILL-|B)(\\d+)$");
        for (Bill bill : service.getAllBills()) {
            Matcher matcher = idPattern.matcher(bill.getBillId());
            if (matcher.matches()) {
                BigInteger numericId = new BigInteger(matcher.group(1));
                if (numericId.compareTo(highestId) > 0) {
                    highestId = numericId;
                }
            }
        }
        return "BILL-" + highestId.add(BigInteger.ONE);
    }

    private void refreshPatientChoices(String selectedId) {
        syncingLinkedIds = true;
        try {
            patientIdF.removeAllItems();
            patientIdF.addItem("Select patient ID");
            for (Patient patient : service.getAllPatients()) {
                patientIdF.addItem(patient.getId());
            }
            patientIdF.setSelectedItem(selectedId == null || selectedId.isEmpty()
                    ? "Select patient ID" : selectedId);
        } finally {
            syncingLinkedIds = false;
        }
    }

    private void refreshAppointmentChoices(String selectedId) {
        refreshingAppointmentChoices = true;
        apptIdF.removeAllItems();
        apptIdF.addItem("Select appointment ID");
        for (Appointment appointment : service.getAllAppointments()) {
            apptIdF.addItem(appointment.getAppointmentId());
        }
        apptIdF.setSelectedItem(selectedId == null || selectedId.isEmpty()
                ? "Select appointment ID" : selectedId);
        refreshingAppointmentChoices = false;
    }

    private String selectedAppointmentId() {
        Object selected = apptIdF.getEditor().getItem();
        String appointmentId = selected == null ? "" : selected.toString().trim();
        return "Select appointment ID".equals(appointmentId) ? "" : appointmentId;
    }

    private String selectedPatientId() {
        Object selected = patientIdF.getEditor().getItem();
        String patientId = selected == null ? "" : selected.toString().trim();
        return "Select patient ID".equals(patientId) ? "" : patientId;
    }

    private void syncPatientFromAppointment() {
        Appointment appointment = service.findAppointmentById(selectedAppointmentId());
        if (appointment != null) {
            refreshPatientChoices(appointment.getPatientId());
        }
    }

    private void syncAppointmentFromPatient() {
        String patientId = selectedPatientId();
        String appointmentId = "";
        for (Appointment appointment : service.getAllAppointments()) {
            if (appointment.getPatientId().equals(patientId)) {
                appointmentId = appointment.getAppointmentId();
                break;
            }
        }

        syncingLinkedIds = true;
        try {
            refreshAppointmentChoices(appointmentId);
        } finally {
            syncingLinkedIds = false;
        }
    }

    private void generate() {
        try {
            String selectedPatientId = selectedPatientId();
            Patient patient = service.findPatientById(selectedPatientId);
            if (patient == null) {
                showMsg("Select or enter a saved patient ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Appointment appointment = service.findAppointmentById(selectedAppointmentId());
            if (appointment != null && !appointment.getPatientId().equals(selectedPatientId)) {
                showMsg("The selected patient does not match the appointment.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            double med = Double.parseDouble(medicineF.getText().trim());
            Bill b = service.generateBill(billIdF.getText().trim(),
                    selectedAppointmentId(), med);
            showMsg("Bill Generated!\nTotal Amount: Rs. " + b.getTotalAmount(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (NumberFormatException ex) {
            showMsg("Medicine charges must be numeric.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBill() {
        try {
            service.deleteBill(billIdF.getText().trim());
            showMsg("Bill deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (InvalidInputException ex) {
            showMsg(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String q = searchF.getText().trim().toLowerCase();
        List<Bill> list = service.getAllBills();
        for (Bill b : list) {
            if (q.isEmpty() || b.getPatientId().toLowerCase().contains(q) || b.getBillId().toLowerCase().contains(q) || b.getAppointmentId().toLowerCase().contains(q)) {
                tableModel.addRow(new Object[]{
                        b.getBillId(), b.getPatientId(), b.getAppointmentId(),
                        b.getConsultationFee(), b.getMedicineCharges(),
                        b.getTotalAmount()
                });
            }
        }
    }

    private void clearForm() {
        billIdF.setText(nextBillId());
        refreshAppointmentChoices("");
        refreshPatientChoices("");
        medicineF.setText("");
        table.clearSelection();
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("bills_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                fw.write("BillID,PatientID,Appointment ID,Consultation Fee,Medicine Charges,Total\n");
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