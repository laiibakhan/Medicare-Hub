package hospital.gui;

import hospital.service.HospitalService;

import javax.swing.*;
import java.awt.*;

/**
 * Main dashboard containing tabs for Dashboard, Patients, Doctors,
 * Appointments and Bills.
 * The Dashboard tab auto-refreshes whenever it is opened.
 *
 * REFERENCES:
 * - JTabbedPane usage pattern:
 *   https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
 *   (Oracle Swing tutorial)
 *
 * - BorderLayout + header panel design pattern (common Swing layout idiom):
 *   https://docs.oracle.com/javase/tutorial/uiswing/layout/border.html
 *
 * All other logic (refresh strategy, tab structure) is my own original work.
 */
public class MainFrame extends JFrame {

    private HospitalService service;

    public MainFrame(String username) {
        service = new HospitalService();
        String displayUsername = username.isEmpty() ? username
                : Character.toUpperCase(username.charAt(0)) + username.substring(1);

        setTitle("Medi Care Hub");
        setSize(1440, 900);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screen.width, screen.height);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BACKGROUND);

        // ---------- Header bar ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);
        JLabel logoIcon = new JLabel(new MedicalLogoIcon());
        brand.add(logoIcon);

        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setOpaque(false);
        JLabel logo = new JLabel("MEDI CARE HUB");
        logo.setFont(new Font("SansSerif", Font.BOLD, 17));
        logo.setForeground(Color.WHITE);
        JLabel brandSubtitle = new JLabel("CARE OPERATIONS PORTAL");
        brandSubtitle.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        brandSubtitle.setForeground(new Color(0xB9C9D9));
        brandText.add(logo);
        brandText.add(Box.createVerticalStrut(3));
        brandText.add(brandSubtitle);
        brand.add(brandText);
        header.add(brand, BorderLayout.WEST);

        JPanel accountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        accountPanel.setOpaque(false);



        JButton logoutButton = new JButton("Logout");
        Theme.styleButton(logoutButton, Theme.PRIMARY_LIGHT);
        logoutButton.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 15f));
        logoutButton.setMargin(new Insets(10, 22, 10, 22));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        accountPanel.add(logoutButton);
        header.add(accountPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ---------- Tabs ----------
        JTabbedPane tabs = new JTabbedPane();
        Theme.styleTabs(tabs);

        DashboardPanel dashboard = new DashboardPanel(service);

        tabs.addTab("  Dashboard  ", dashboard);
        tabs.addTab("  Patients  ", new PatientPanel(service));
        tabs.addTab("  Doctors  ", new DoctorPanel(service));
        tabs.addTab("  Appointments  ", new AppointmentPanel(service));
        tabs.addTab("  Bills  ", new BillPanel(service));

        // Refresh dashboard every time any tab is selected
        // ensures counts stay up-to-date after add/delete operations
        tabs.addChangeListener(e -> dashboard.refresh());

        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }
}