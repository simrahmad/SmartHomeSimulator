// ============================================================
//  NotificationSystem.java  –  Alert and notification log hub
//
//  PURPOSE:
//    Acts as the central message bus for the smart home.
//    Any subsystem (security, automation, power) that needs to
//    surface an event to the user sends it through here.
//    Messages are stored in two lists:
//      • alerts         – raw alert message strings
//      • notificationLog – prefixed "[ALERT] ..." entries for display
//
//  DESIGN NOTES:
//    - Implements Serializable so the full notification history
//      is preserved when the SmartHome state is saved to disk.
//    - sendAlert() validates the message before storing it, so
//      the log never contains null/blank entries.
//    - showNotifications() throws a SmartHomeException when the
//      log is empty rather than printing "No notifications" to
//      stdout — this lets callers decide how to handle the empty
//      case (e.g. show a different UI message).
//    - motionDetected() and energyUsageHigh() are named helpers
//      that keep calling code expressive and free of hard-coded
//      string literals.
// ============================================================

import exceptions.SmartHomeException;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class NotificationSystem implements Serializable {

    /**
     * Raw alert messages (without prefix).
     * Stored separately so callers can programmatically inspect
     * individual alerts without parsing the formatted log strings.
     */
    private List<String> alerts;

    /**
     * Display-ready log entries, each prefixed with "[ALERT] ".
     * Shown to the user via showNotifications() and persisted
     * alongside the rest of the smart home state.
     */
    private List<String> notificationLog;

    /**
     * Required by the Serializable contract.
     * Changing this value would invalidate previously serialised
     * files, so it is kept as a fixed constant.
     */
    private static final long serialVersionUID = 1L;

    // ── Constructor ──────────────────────────────────────────

    /**
     * Creates a notification system with empty alert and log lists.
     * Called once when a SmartHome is first instantiated; after that
     * the state is rehydrated from disk via deserialization.
     */
    public NotificationSystem() {
        this.alerts          = new ArrayList<>();
        this.notificationLog = new ArrayList<>();
    }

    // ── Getters ──────────────────────────────────────────────

    /**
     * @return The list of raw alert message strings.
     *         Useful for programmatic checks (e.g. "has a motion alert fired?").
     */
    public List<String> getAlerts()          { return alerts; }

    /**
     * @return The list of prefixed log entries for display.
     *         Each entry is of the form "[ALERT] <message>".
     */
    public List<String> getNotificationLog() { return notificationLog; }

    // ── Business methods ─────────────────────────────────────

    /**
     * Records and immediately prints an alert message.
     *
     * The message is added to both lists:
     *   1. alerts          — raw string, for programmatic access
     *   2. notificationLog — prefixed string, for user display
     *
     * Validation: null or blank messages are rejected to keep the
     * log clean and prevent confusing empty entries.
     *
     * @param msg The alert text to record (must be non-empty).
     * @throws SmartHomeException if msg is null or blank.
     */
    public void sendAlert(String msg) throws SmartHomeException {
        // Reject empty alerts — they would pollute the log with useless entries
        if (msg == null || msg.trim().isEmpty()) {
            throw new SmartHomeException(
                    "[Notification] Alert message cannot be empty!"
            );
        }
        // Store the raw message for programmatic access
        alerts.add(msg);
        // Store the formatted entry for display
        notificationLog.add("[ALERT] " + msg);
        // Immediately print to console so the user sees real-time alerts
        System.out.println("[Notification] ALERT: " + msg);
    }

    /**
     * Prints all recorded notifications to stdout.
     *
     * Throws an exception if the log is empty so callers can
     * distinguish between "no notifications" and a genuine display
     * request — the caller decides how to handle the empty case.
     *
     * @throws SmartHomeException if the notification log is empty.
     */
    public void showNotifications() throws SmartHomeException {
        System.out.println("\n── Notification Log (" + notificationLog.size() + " entries) ──");
        // Throw rather than printing "empty" — lets callers handle the UX themselves
        if (notificationLog.isEmpty()) {
            throw new SmartHomeException(
                    "[Notification] No notifications found!"
            );
        }
        // Print every log entry in chronological order (ArrayList preserves insertion order)
        for (String n : notificationLog) System.out.println("  " + n);
        System.out.println("────────────────────────────────────────");
    }

    /**
     * Convenience method: fires a pre-worded motion-detection alert.
     * Called by SecuritySystem.triggerAlert() so the alert text is
     * consistent across the codebase rather than being a loose string literal.
     *
     * @throws SmartHomeException if sendAlert() fails (should not happen with a static string).
     */
    public void motionDetected() throws SmartHomeException {
        sendAlert("Motion detected by security system!");
    }

    /**
     * Convenience method: fires a pre-worded high-energy-usage alert.
     * Called by SmartHome.enablePowerSavingMode() when energy consumption
     * exceeds an acceptable threshold.
     *
     * @throws SmartHomeException if sendAlert() fails (should not happen with a static string).
     */
    public void energyUsageHigh() throws SmartHomeException {
        sendAlert("Energy usage is unusually high!");
    }
}
