import exceptions.SmartHomeException;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

// central message hub — any subsystem can send alerts through here
public class NotificationSystem implements Serializable {

    private List<String> alerts;          // raw alert messages for programmatic access
    private List<String> notificationLog; // formatted "[ALERT] ..." entries for display

    private static final long serialVersionUID = 1L; // keeps saved files compatible


    // ── Constructor ──────────────────────────────────────────

    // starts with empty lists — gets populated as the system runs
    public NotificationSystem() {
        this.alerts          = new ArrayList<>();
        this.notificationLog = new ArrayList<>();
    }


    // ── Getters ──────────────────────────────────────────────

    public List<String> getAlerts()          { return alerts; }
    public List<String> getNotificationLog() { return notificationLog; }


    // ── Business methods ─────────────────────────────────────

    // records and immediately prints an alert — rejects blank messages
    public void sendAlert(String msg) throws SmartHomeException {
        if (msg == null || msg.trim().isEmpty()) {
            throw new SmartHomeException("[Notification] Alert message cannot be empty!");
        }
        alerts.add(msg);                        // raw message for code access
        notificationLog.add("[ALERT] " + msg);  // formatted entry for display
        System.out.println("[Notification] ALERT: " + msg);
    }

    // prints all recorded notifications — throws if log is empty so caller can handle the UX
    public void showNotifications() throws SmartHomeException {
        System.out.println("\n── Notification Log (" + notificationLog.size() + " entries) ──");
        if (notificationLog.isEmpty()) {
            throw new SmartHomeException("[Notification] No notifications found!");
        }
        for (String n : notificationLog) System.out.println("  " + n);
        System.out.println("────────────────────────────────────────");
    }

    // fires a motion detection alert — called by SecuritySystem.triggerAlert()
    public void motionDetected() throws SmartHomeException {
        sendAlert("Motion detected by security system!");
    }

    // fires a high energy usage alert — called by SmartHome.enablePowerSavingMode()
    public void energyUsageHigh() throws SmartHomeException {
        sendAlert("Energy usage is unusually high!");
    }
}