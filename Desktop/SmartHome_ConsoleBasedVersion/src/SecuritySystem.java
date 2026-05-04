// ============================================================
//  SecuritySystem.java  –  Smart security camera / alarm system
//
//  PURPOSE:
//    Extends Device to add arm/disarm logic, PIN authentication,
//    and alert triggering. When armed and an alert fires, the
//    system delegates to NotificationSystem to record and
//    broadcast the event to the rest of the smart home.
//
//  DESIGN NOTES:
//    - arm() and disarm() maintain their own isArmed flag rather
//      than reusing the Device.status boolean. This allows the
//      security module to be powered ON (smart chip active) while
//      not necessarily being armed (alarm not active).
//    - A brute-force lockout kicks in after 3 failed PIN attempts:
//      failedDisarmAttempts is incremented on each wrong guess and
//      the system permanently locks until manually reset.
//    - triggerAlert() requires the system to be armed first — this
//      prevents false alerts when the system is being serviced.
//    - Two constructors are provided: one that wires up a
//      NotificationSystem (production use) and one without (testing
//      or standalone use).
// ============================================================

import exceptions.InvalidDeviceStateException;
import exceptions.AuthenticationException;
import exceptions.SmartHomeException;

public class SecuritySystem extends Device {

    /**
     * Whether the alarm is currently active and monitoring for intrusions.
     * true  = alarm is ARMED   (will trigger on motion/alert)
     * false = alarm is DISARMED (passive / not monitoring)
     */
    private boolean isArmed;

    /**
     * Reference to the home's notification system used to broadcast
     * alerts (e.g. motion detected). May be null in standalone/test usage.
     */
    private NotificationSystem notificationSystem;

    /**
     * PIN required to disarm the security system.
     * Defaults to "1234" — users should change this immediately via
     * setAdminPin() after first setup for security.
     */
    private String adminPin;

    /**
     * Running count of incorrect PIN entries since the last successful disarm.
     * When this reaches 3, the system locks and rejects all further
     * disarm attempts until manually reset.
     */
    private int failedDisarmAttempts;

    // ── Constructors ─────────────────────────────────────────

    /**
     * Full constructor — wires up a NotificationSystem so alerts are
     * broadcast to the rest of the smart home.
     *
     * @param name     Display name (e.g. "Security Cam").
     * @param deviceId Unique ID (e.g. "D006").
     * @param ns       The home's NotificationSystem instance.
     */
    public SecuritySystem(String name, String deviceId, NotificationSystem ns) {
        super(name, deviceId);
        this.isArmed              = false;  // starts disarmed
        this.notificationSystem   = ns;     // store reference for alert broadcasting
        this.adminPin             = "1234"; // default pin — should be changed in production
        this.failedDisarmAttempts = 0;      // no failed attempts on fresh installation
    }

    /**
     * Minimal constructor — no NotificationSystem attached.
     * Alerts will still be printed to stdout but will NOT be added
     * to the notification log. Useful for unit tests or isolated demos.
     *
     * @param name     Display name.
     * @param deviceId Unique ID.
     */
    public SecuritySystem(String name, String deviceId) {
        super(name, deviceId);
        this.isArmed              = false;
        this.notificationSystem   = null;   // no notifications — standalone mode
        this.adminPin             = "1234";
        this.failedDisarmAttempts = 0;
    }

    // ── Getters & Setters ────────────────────────────────────

    /**
     * @return true if the alarm is currently armed and actively monitoring.
     */
    public boolean isArmed()           { return isArmed; }

    /**
     * Directly sets the armed state without PIN validation.
     * Use only for deserialisation or test setup; prefer arm()/disarm()
     * for normal operation to maintain security invariants.
     */
    public void    setArmed(boolean a) { this.isArmed = a; }

    /**
     * Updates the disarm PIN.
     * Rejects null or blank PINs to prevent the system from being
     * locked into an un-disarmable state.
     *
     * @param newPin The new PIN string (must be non-empty).
     * @throws AuthenticationException if newPin is null or blank.
     */
    public void setAdminPin(String newPin) throws AuthenticationException {
        // A blank PIN would make it impossible to ever disarm the system
        if (newPin == null || newPin.trim().isEmpty()) {
            throw new AuthenticationException(
                    "[" + name + "] PIN cannot be empty!"
            );
        }
        this.adminPin = newPin;
        System.out.println("[" + name + "] PIN updated successfully.");
    }

    // ── Business methods ─────────────────────────────────────

    /**
     * Arms the security system so it will respond to alerts.
     * Throws if already armed — prevents double-arming which could
     * confuse the state machine and hide bugs in callers.
     *
     * @throws InvalidDeviceStateException if the system is already ARMED.
     */
    public void arm() throws InvalidDeviceStateException {
        // Guard: reject if already in armed state
        if (isArmed) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Security system is already ARMED!"
            );
        }
        this.isArmed = true;
        System.out.println("[" + name + "] Security system ARMED.");
    }

    /**
     * Disarms the security system using PIN authentication.
     *
     * Three-strike rule:
     *   - After 3 failed PIN attempts the system permanently locks;
     *     no further disarm calls will succeed until the counter is
     *     reset (requires physical intervention / admin override).
     *   - On a correct PIN entry, the failed-attempt counter resets to 0.
     *
     * @param inputPin The PIN entered by the user.
     * @throws AuthenticationException     if the PIN is wrong or 3 attempts have been used.
     * @throws InvalidDeviceStateException if the system is already DISARMED.
     */
    public void disarm(String inputPin) throws AuthenticationException, InvalidDeviceStateException {
        // Cannot disarm something that isn't armed
        if (!isArmed) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Security system is already DISARMED!"
            );
        }
        // Lockout check — must happen BEFORE comparing the PIN
        if (failedDisarmAttempts >= 3) {
            throw new AuthenticationException(
                    "[" + name + "] Too many failed attempts! System locked."
            );
        }
        // PIN comparison
        if (!this.adminPin.equals(inputPin)) {
            failedDisarmAttempts++;   // increment BEFORE throwing so the count is persisted
            throw new AuthenticationException(
                    "[" + name + "] Wrong PIN! Attempts remaining: " + (3 - failedDisarmAttempts)
            );
        }
        // Correct PIN — disarm and reset lockout counter
        this.isArmed              = false;
        this.failedDisarmAttempts = 0;    // reset so the user gets 3 fresh attempts next time
        System.out.println("[" + name + "] Security system DISARMED.");
    }

    /**
     * Triggers an intrusion alert.
     *
     * Requires the system to be armed — if called while disarmed, an
     * exception is thrown rather than firing a false alarm. When the
     * system IS armed, the alert is printed to stdout and, if a
     * NotificationSystem is attached, the event is also logged there.
     *
     * @throws SmartHomeException         if the NotificationSystem call fails.
     * @throws InvalidDeviceStateException if the system is not currently ARMED.
     *
     * NOTE: The duplicate 'throws SmartHomeException' in the original signature
     *       is kept here for backward compatibility but is redundant.
     */
    public void triggerAlert() throws SmartHomeException, SmartHomeException {
        // Only armed systems should fire alerts — disarmed = no monitoring
        if (!isArmed) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Cannot trigger alert, system is not ARMED!"
            );
        }
        System.out.println("[" + name + "] *** ALERT TRIGGERED! ***");
        // Delegate to NotificationSystem if one is wired up — null-safe check
        if (notificationSystem != null) {
            notificationSystem.motionDetected();  // logs alert and sends notification
        }
    }

    // ── Override showStatus ──────────────────────────────────

    /**
     * Prints a formatted one-line status summary to stdout.
     * Format: "  Security Sys | <name> | Status: ON/OFF | Armed: YES/NO"
     * Called by Admin.viewAllDevices() and SmartHome.showAllDevices().
     */
    @Override
    public void showStatus() {
        System.out.println("  Security Sys | " + name + " | Status: " + getStatusString()
                + " | Armed: " + (isArmed ? "YES" : "NO"));
    }
}
