package hospital;

import hospital.gui.LoginFrame;

import javax.swing.*;

/**
 * Main class - launches the application.
 * Sets Swing look and feel to the system default for a modern appearance.
 * All logic in this class is my own original work.
 */
public class Main {
    public static void main(String[] args) {
        // Use the OS's native look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore - will fall back to default
        }

        // Start the GUI on the Event Dispatch Thread (Swing best practice)
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}