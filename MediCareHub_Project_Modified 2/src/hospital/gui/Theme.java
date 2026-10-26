package hospital.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Theme class holds all colors, fonts, and styling helpers used across the GUI.
 * Centralizing styles keeps the UI consistent and easy to change.
 *
 * REFERENCES:
 * - Swing JTable custom header renderer pattern:
 *   https://docs.oracle.com/javase/tutorial/uiswing/components/table.html
 *   (Oracle's official Swing tutorial — used for setDefaultRenderer approach)
 *
 * All other logic (method structure, styling helpers) is my own original work.
 */
public class Theme {

    // ==================== COLOR REFERENCE ====================
    // STANDARD JAVA COLORS
    //   Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY,
    //   Color.GRAY, Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA,
    //   Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW
    //
    // SOFT COLORS (hex codes):
    //   Light Red     = new Color(0xFF6B6B)
    //   Soft Green    = new Color(0x6BCB77)
    //   Soft Blue     = new Color(0x4D96FF)
    //   Soft Orange   = new Color(0xFFA500)
    //   Soft Purple   = new Color(0xB983FF)
    //   Soft Pink     = new Color(0xFF8FAB)
    //   Soft Yellow   = new Color(0xFFD93D)
    //   Soft Teal     = new Color(0x1ABC9C)
    //   Dark Navy     = new Color(0x2C3E50)
    //   Light Grey    = new Color(0xECF0F1)
    // =========================================================

    // ---------------- ACTIVE COLOR PALETTE ----------------
    public static final Color PRIMARY        = new Color(0x142B4A);
    public static final Color PRIMARY_LIGHT  = new Color(0x294B70);
    public static final Color ACCENT         = new Color(0x0FAE9B);
    public static final Color ACCENT_DARK    = new Color(0x087F8C);
    public static final Color UTILITY       = new Color(0xE8F1F5);
    public static final Color EXPORT        = new Color(0x167D68);
    public static final Color DANGER         = new Color(0xD94F5C);
    public static final Color WARNING        = new Color(0xE69A32);
    public static final Color INFO           = new Color(0x3478C5);
    public static final Color BACKGROUND     = new Color(0xF3F6FA);
    public static final Color CARD           = Color.WHITE;
    public static final Color TEXT_DARK      = new Color(0x1D2C3D);
    public static final Color TEXT_MUTED     = new Color(0x738195);
    public static final Color BORDER         = new Color(0xDFE6EE);
    public static final Color TEXT_LIGHT     = Color.WHITE;

    // ---------------- FONTS ----------------
    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);

    /**
     * Styles a JButton with a background color and white text.
     * No preferredSize is set — that was breaking click events inside FlowLayout.
     * Padding is applied via setMargin instead, which keeps the clickable area
     * matching the visual area exactly.
     */
    public static void styleButton(JButton b, Color bg) {
        final Color normal = bg.equals(UTILITY) ? new Color(0xEDF2F7) : bg;
        final Color foreground = bg.equals(UTILITY) ? PRIMARY : Color.WHITE;
        b.setForeground(foreground);
        b.setFont(FONT_BODY.deriveFont(Font.BOLD));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(8, 18, 8, 18));
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        b.setRolloverEnabled(true);
        b.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics graphics, JComponent component) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton button = (AbstractButton) component;
                ButtonModel model = button.getModel();
                Color fill = normal;
                if (!model.isEnabled()) fill = normal.brighter();
                else if (model.isPressed()) fill = normal.darker().darker();
                else if (model.isRollover()) fill = normal.darker();
                int arc = Math.max(28, component.getHeight());
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, component.getWidth() - 1, component.getHeight() - 1, arc, arc);
                g2.dispose();
                super.paint(graphics, component);
            }
        });
    }

    public static void styleComboButton(JButton b) {
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT_DARK);
        b.setFont(FONT_BODY);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        b.setPreferredSize(new Dimension(170, 36));
    }

    /**
     * Styles a JTextField for a clean modern look.
     */
    public static void styleField(JTextField f) {
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_DARK);
        f.setCaretColor(ACCENT_DARK);
        f.setBackground(Color.WHITE);
        f.setOpaque(true);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        f.setPreferredSize(new Dimension(150, 36));
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setFont(FONT_BODY);
        combo.setForeground(TEXT_DARK);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(150, 36));
        combo.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        combo.setFocusable(true);
        Component editor = combo.isEditable() ? combo.getEditor().getEditorComponent() : null;
        if (editor instanceof JTextField) {
            ((JTextField) editor).setFont(FONT_BODY);
            ((JTextField) editor).setForeground(TEXT_DARK);
            ((JTextField) editor).setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 4));
        }
    }

    public static void styleCard(JPanel panel) {
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
    }

    public static void styleTabs(JTabbedPane tabs) {
        tabs.setFont(FONT_BODY.deriveFont(Font.BOLD));
        tabs.setBackground(BACKGROUND);
        tabs.setForeground(TEXT_DARK);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 14, 0, 14));
        tabs.setFocusable(false);
    }

    /**
     * Styles a JTable with a custom header renderer so the column names
     * appear as white text on a navy background.
     */
    public static void styleTable(JTable t) {
        t.setFont(FONT_BODY);
        t.setRowHeight(34);
        t.setGridColor(new Color(0xE8EDF2));
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);
        t.setForeground(TEXT_DARK);
        t.setSelectionBackground(new Color(0xDDF4F0));
        t.setSelectionForeground(Color.WHITE);
        t.setSelectionForeground(TEXT_DARK);
        t.setRowMargin(0);

        // Custom header renderer — guarantees visible header text
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                c.setBackground(PRIMARY);
                c.setForeground(Color.WHITE);
                c.setFont(FONT_BODY.deriveFont(Font.BOLD));
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    ((JLabel) c).setOpaque(true);
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return c;
            }
        };

        t.getTableHeader().setDefaultRenderer(headerRenderer);
        t.getTableHeader().setPreferredSize(new Dimension(0, 40));
        t.getTableHeader().setBackground(PRIMARY);
        t.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                }
                c.setForeground(isSelected ? TEXT_DARK : TEXT_DARK);
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return c;
            }
        };
        t.setDefaultRenderer(Object.class, cellRenderer);
        t.setDefaultRenderer(Number.class, cellRenderer);

        JTableHeader header = t.getTableHeader();
        header.setReorderingAllowed(false);
    }
}