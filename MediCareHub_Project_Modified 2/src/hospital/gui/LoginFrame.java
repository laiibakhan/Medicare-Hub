package hospital.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Colorful login screen with gradient background.
 * Admin: admin / admin123
 * Receptionist: reception / rec123
 *
 * Features:
 * - Pre-filled credentials for easy demo
 * - Show/Hide password toggle (eye button)
 *
 * REFERENCES:
 * - Gradient background using Graphics2D and GradientPaint:
 *   https://docs.oracle.com/javase/tutorial/2d/overview/index.html
 *   (Oracle 2D Graphics tutorial)
 * - Event Dispatch Thread usage:
 *   https://docs.oracle.com/javase/tutorial/uiswing/concurrency/initial.html
 *
 * All other logic is my own original work.
 */
public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JButton togglePassBtn;   // show/hide password button
    private boolean passwordVisible = false;

    public LoginFrame() {
        setTitle("Medi Care Hub - Sign In");
        setSize(600, 640);
        setMinimumSize(new Dimension(540, 590));
        setLocationRelativeTo(null);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screen.width, screen.height);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ---------- Custom gradient background ----------
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x10243E),
                        getWidth(), getHeight(), new Color(0x315F91));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(getWidth() - 170, 32, 260, 260);
                g2.fillOval(-120, getHeight() - 220, 300, 300);
            }
        };
        bg.setLayout(new BorderLayout());

        // ---------- Title ----------
        JLabel title = new JLabel("MEDI CARE HUB");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel brandSubtitle = new JLabel("CARE OPERATIONS PORTAL");
        brandSubtitle.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        brandSubtitle.setForeground(new Color(0xD9E6EF));

        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setOpaque(false);
        brandText.add(title);
        brandText.add(Box.createVerticalStrut(3));
        brandText.add(brandSubtitle);

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(22, 10, 22, 10));
        brand.add(new JLabel(new MedicalLogoIcon()));
        brand.add(brandText);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(brand, BorderLayout.CENTER);
        bg.add(header, BorderLayout.NORTH);

        // ---------- Form ----------
        JPanel form = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(5, 20, 38, 45));
                g2.fillRoundRect(1, 7, getWidth() - 2, getHeight() - 8, 24, 24);
                g2.setColor(new Color(0xFFFFFF));
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 10, 24, 24);
                g2.setColor(new Color(0xE7EDF5));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 11, 24, 24);
                g2.dispose();
            }
        };
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));
        form.setPreferredSize(new Dimension(430, 390));

        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(Theme.FONT_TITLE);
        welcome.setForeground(Theme.TEXT_DARK);
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 26f));
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(welcome);

        JLabel instruction = new JLabel("Sign in to continue to your workspace");
        instruction.setFont(Theme.FONT_BODY);
        instruction.setForeground(Theme.TEXT_MUTED);
        instruction.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(4));
        form.add(instruction);
        form.add(Box.createVerticalStrut(22));

        // --- Username ---
        JLabel userLbl = new JLabel("Username:");
        userLbl.setForeground(Theme.TEXT_DARK);
        userLbl.setFont(Theme.FONT_BODY);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userLbl);
        form.add(Box.createVerticalStrut(6));

        userField = new JTextField();
        styleLoginField(userField);
        JPanel userPanel = createFieldShell(userField);
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userPanel);
        form.add(Box.createVerticalStrut(16));

        // --- Password with eye button ---
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Theme.TEXT_DARK);
        passLbl.setFont(Theme.FONT_BODY);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passLbl);
        form.add(Box.createVerticalStrut(6));

        // Password field + toggle button wrapped in a small panel
        JPanel passPanel = new JPanel(new BorderLayout(4, 0));
        passPanel.setBackground(new Color(0xF8FAFD));
        passPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD5DEE9), 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 4)));
        passPanel.setPreferredSize(new Dimension(346, 44));
        passPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passField = new JPasswordField();
        styleLoginField(passField);
        passField.setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 2));
        passField.setEchoChar('•');      // default masking
        passPanel.add(passField, BorderLayout.CENTER);

        // Eye toggle button — shows or hides the password
        togglePassBtn = new JButton(new PasswordVisibilityIcon(false));
        togglePassBtn.setFocusPainted(false);
        togglePassBtn.setBorderPainted(false);
        togglePassBtn.setContentAreaFilled(false);
        togglePassBtn.setPreferredSize(new Dimension(28, 28));
        togglePassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        togglePassBtn.setForeground(Theme.TEXT_DARK);
        togglePassBtn.setToolTipText("Show password");
        togglePassBtn.addActionListener(e -> togglePasswordVisibility());
        passPanel.add(togglePassBtn, BorderLayout.EAST);

        form.add(passPanel);
        form.add(Box.createVerticalStrut(22));

        JPanel formHolder = new JPanel(new GridBagLayout());
        formHolder.setOpaque(false);
        formHolder.setBorder(BorderFactory.createEmptyBorder(0, 24, 18, 24));
        formHolder.add(form);
        bg.add(formHolder, BorderLayout.CENTER);

        // ---------- Login button ----------
        JButton loginBtn = new JButton("SIGN IN");
        Theme.styleButton(loginBtn, Theme.PRIMARY_LIGHT);
        loginBtn.setPreferredSize(new Dimension(346, 46));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> handleLogin());
        form.add(loginBtn);
        getRootPane().setDefaultButton(loginBtn);

        JLabel footer = new JLabel("SECURE ACCESS  •  AUTHORIZED PERSONNEL ONLY",
                SwingConstants.CENTER);
        footer.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        footer.setForeground(new Color(0xD9E6EF));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 8, 18, 8));
        bg.add(footer, BorderLayout.SOUTH);

        setContentPane(bg);
        setVisible(true);
        setBounds(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void styleLoginField(JTextField field) {
        field.setFont(Theme.FONT_BODY.deriveFont(14f));
        field.setForeground(Theme.TEXT_DARK);
        field.setCaretColor(Theme.INFO);
        field.setBackground(new Color(0xF8FAFD));
        field.setOpaque(true);
        field.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        field.setPreferredSize(new Dimension(150, 38));
    }

    private JPanel createFieldShell(JTextField field) {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(new Color(0xF8FAFD));
        shell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD5DEE9), 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        shell.setPreferredSize(new Dimension(346, 44));
        shell.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        shell.add(field, BorderLayout.CENTER);
        return shell;
    }

    private void togglePasswordVisibility() {
        if (passwordVisible) {
            passField.setEchoChar('•');        // hide
            passwordVisible = false;
            togglePassBtn.setIcon(new PasswordVisibilityIcon(false));
            togglePassBtn.setToolTipText("Show password");
        } else {
            passField.setEchoChar((char) 0);   // show plain text
            passwordVisible = true;
            togglePassBtn.setIcon(new PasswordVisibilityIcon(true));
            togglePassBtn.setToolTipText("Hide password");
        }
    }

    private static final class PasswordVisibilityIcon implements Icon {
        private final boolean passwordVisible;

        private PasswordVisibilityIcon(boolean passwordVisible) {
            this.passwordVisible = passwordVisible;
        }

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(Theme.TEXT_DARK);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));

            Path2D eye = new Path2D.Float();
            eye.moveTo(1, 8);
            eye.curveTo(4.5, 3, 11.5, 3, 15, 8);
            eye.curveTo(11.5, 13, 4.5, 13, 1, 8);
            g2.draw(eye);
            g2.fillOval(6, 6, 4, 4);

            // Crossed-out eye means the password is hidden; open eye means visible.
            if (!passwordVisible) {
                g2.drawLine(2, 14, 14, 2);
            }
            g2.dispose();
        }
    }

    private void handleLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if ((user.equals("admin") && pass.equals("admin123")) ||
                (user.equals("reception") && pass.equals("rec123"))) {
            dispose();
            new MainFrame(user);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}