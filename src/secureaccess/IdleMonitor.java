/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
 *
 * @author shemeneroje
 */

/**
 * Tracks user inactivity and automatically logs out after a timeout.
 * Shows a modal warning dialog with a live countdown before logout.
 */
public class IdleMonitor {
    private final Timer idleTimer;
    private final Timer warnStartTimer;
    private final long timeoutMs;
    private final long warningMs;
    private final Runnable onTimeout;
    private JDialog warningDialog;

    public IdleMonitor(long timeoutMs, long warningMs, Runnable onTimeout) {
        if (warningMs >= timeoutMs) {
            throw new IllegalArgumentException("warningMs must be less than timeoutMs");
        }
        this.timeoutMs = timeoutMs;
        this.warningMs = warningMs;
        this.onTimeout = onTimeout;

        idleTimer = new Timer((int) timeoutMs, e -> onTimeout.run());
        idleTimer.setRepeats(false);

        warnStartTimer = new Timer((int) (timeoutMs - warningMs), e -> showWarning());
        warnStartTimer.setRepeats(false);

        attachActivityListeners();
        restart();
    }

    private void attachActivityListeners() {
        AWTEventListener listener = event -> {
            if (event instanceof MouseEvent || event instanceof KeyEvent) {
                restart();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(
            listener,
            AWTEvent.MOUSE_EVENT_MASK |
            AWTEvent.MOUSE_MOTION_EVENT_MASK |
            AWTEvent.KEY_EVENT_MASK
        );
    }

    public void restart() {
        idleTimer.restart();
        warnStartTimer.restart();
        closeWarning();
    }

    private void showWarning() {
        SwingUtilities.invokeLater(() -> {
            closeWarning();
            warningDialog = new JDialog((Frame) null, "Session Timeout Warning", true);
            warningDialog.setLayout(new BorderLayout(10, 10));

            JLabel label = new JLabel("", SwingConstants.CENTER);
            JButton stay = new JButton("Stay Logged In");
            stay.addActionListener(e -> {
                restart();
                closeWarning();
            });

            warningDialog.add(label, BorderLayout.CENTER);
            warningDialog.add(stay, BorderLayout.SOUTH);
            warningDialog.setSize(360, 160);
            warningDialog.setLocationRelativeTo(null);

            final int[] secsLeft = { (int) (warningMs / 1000) };
            label.setText("You will be logged out in " + secsLeft[0] + " seconds.");
            Timer countdown = new Timer(1000, ev -> {
                secsLeft[0]--;
                label.setText("You will be logged out in " + secsLeft[0] + " seconds.");
                if (secsLeft[0] <= 0) ((Timer) ev.getSource()).stop();
            });
            countdown.start();

            Timer finalizeLogout = new Timer((int) warningMs, ev -> {
                closeWarning();
                onTimeout.run();
            });
            finalizeLogout.setRepeats(false);
            finalizeLogout.start();

            warningDialog.setVisible(true);
        });
    }

    private void closeWarning() {
        if (warningDialog != null && warningDialog.isShowing()) {
            warningDialog.dispose();
        }
    }

    public void stop() {
        idleTimer.stop();
        warnStartTimer.stop();
        closeWarning();
    }
}
