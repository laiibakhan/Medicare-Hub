package hospital.gui;

import hospital.service.HospitalService;

import javax.swing.*;
import java.awt.*;

/**
 * DashboardPanel shows summary cards with total counts of
 * patients, doctors, appointments, and bills.
 * All logic in this class is my own original work.
 */
public class DashboardPanel extends JPanel {

    private HospitalService service;
    private JPanel cards;
    private JLabel[] valueLabels = new JLabel[4];
    private final String[] cardTitles = {
            "Registered patients", "Medical staff", "Appointments", "Bills generated"
    };
    private final Color[] cardColors = {
            Theme.ACCENT, Theme.INFO, Theme.WARNING, Theme.DANGER
    };

    public DashboardPanel(HospitalService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);
        JLabel eyebrow = new JLabel("HOSPITAL OPERATIONS");
        eyebrow.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        eyebrow.setForeground(Theme.ACCENT_DARK);
        JLabel title = new JLabel("Dashboard overview");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);
        JLabel subtitle = new JLabel("A quick snapshot of your hospital activity.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(eyebrow);
        heading.add(Box.createVerticalStrut(5));
        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitle);
        add(heading, BorderLayout.NORTH);

        cards = new JPanel(new GridLayout(1, 4, 18, 18));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        refreshValues();
        add(cards, BorderLayout.CENTER);
    }

    private JPanel makeCard(int index) {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(20, 18, 20, 18)));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(cardColors[index]);
        accentBar.setPreferredSize(new Dimension(42, 4));
        accentBar.setMaximumSize(new Dimension(42, 4));
        JPanel accentWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        accentWrap.setOpaque(false);
        accentWrap.add(accentBar);

        JLabel valueLabel = new JLabel("0");
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        valueLabel.setForeground(Theme.TEXT_DARK);
        valueLabels[index] = valueLabel;

        JLabel textLabel = new JLabel(cardTitles[index]);
        textLabel.setFont(Theme.FONT_BODY);
        textLabel.setForeground(Theme.TEXT_MUTED);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.add(valueLabel);
        text.add(Box.createVerticalStrut(6));
        text.add(textLabel);

        card.add(accentWrap, BorderLayout.NORTH);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        refreshValues();
        revalidate();
        repaint();
    }

    private void refreshValues() {
        int[] counts = {
                service.getAllPatients().size(),
                service.getAllDoctors().size(),
                service.getAllAppointments().size(),
                service.getAllBills().size()
        };
        if (cards.getComponentCount() == 0) {
            for (int i = 0; i < counts.length; i++) {
                cards.add(makeCard(i));
            }
        }
        for (int i = 0; i < counts.length; i++) {
            valueLabels[i].setText(Integer.toString(counts[i]));
        }
    }
}