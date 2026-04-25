import exceptions.SmartHomeException;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class NotificationSystem implements Serializable {

    private List<String> alerts;
    private List<String> notificationLog;
    private static final long serialVersionUID = 1L;

    // ── Constructor ──────────────────────────────────────────
    public NotificationSystem() {
        this.alerts          = new ArrayList<>();
        this.notificationLog = new ArrayList<>();
    }

    // ── Getters ──────────────────────────────────────────────
    public List<String> getAlerts()          { return alerts; }
    public List<String> getNotificationLog() { return notificationLog; }

    // ── Business methods ─────────────────────────────────────

    // throws exception if message is null or empty
    public void sendAlert(String msg) throws SmartHomeException {
        if (msg == null || msg.trim().isEmpty()) {
            throw new SmartHomeException(
                    "[Notification] Alert message cannot be empty!"
            );
        }
        alerts.add(msg);
        notificationLog.add("[ALERT] " + msg);
        System.out.println("[Notification] ALERT: " + msg);
    }

    // throws exception if log is empty
    public void showNotifications() throws SmartHomeException {
        System.out.println("\n── Notification Log (" + notificationLog.size() + " entries) ──");
        if (notificationLog.isEmpty()) {
            throw new SmartHomeException(
                    "[Notification] No notifications found!"
            );
        }
        for (String n : notificationLog) System.out.println("  " + n);
        System.out.println("────────────────────────────────────────");
    }

    // throws exception if sendAlert fails
    public void motionDetected() throws SmartHomeException {
        sendAlert("Motion detected by security system!");
    }

    public void energyUsageHigh() throws SmartHomeException {
        sendAlert("Energy usage is unusually high!");
    }
}