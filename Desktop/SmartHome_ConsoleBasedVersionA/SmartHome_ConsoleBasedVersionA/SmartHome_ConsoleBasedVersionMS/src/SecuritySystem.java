import exceptions.InvalidDeviceStateException;
import exceptions.AuthenticationException;
import exceptions.SmartHomeException;

// smart security system — handles arming, disarming with PIN, and alert triggering
public class SecuritySystem extends Device {

    private boolean isArmed;                      // true = ARMED, false = DISARMED
    private NotificationSystem notificationSystem; // broadcasts alerts to the rest of the home
    private String adminPin;                       // PIN required to disarm
    private int failedDisarmAttempts;              // locks system after 3 wrong guesses


    // ── Constructors ─────────────────────────────────────────

    // full constructor — wires up a notification system for alert broadcasting
    public SecuritySystem(String name, String deviceId, NotificationSystem ns) {
        super(name, deviceId);
        this.isArmed              = false;
        this.notificationSystem   = ns;
        this.adminPin             = "1234"; // default PIN — should be changed after setup
        this.failedDisarmAttempts = 0;
    }

    // standalone constructor — alerts print to console only, no notification log
    public SecuritySystem(String name, String deviceId) {
        super(name, deviceId);
        this.isArmed              = false;
        this.notificationSystem   = null;   // no notification system attached
        this.adminPin             = "1234";
        this.failedDisarmAttempts = 0;
    }


    // ── Getters & Setters ────────────────────────────────────

    public boolean isArmed()           { return isArmed; }
    public void    setArmed(boolean a) { this.isArmed = a; } // for deserialization and tests only

    // updates the disarm PIN — rejects blank PINs so the system can't be locked permanently
    public void setAdminPin(String newPin) throws AuthenticationException {
        if (newPin == null || newPin.trim().isEmpty()) {
            throw new AuthenticationException("[" + name + "] PIN cannot be empty!");
        }
        this.adminPin = newPin;
        System.out.println("[" + name + "] PIN updated successfully.");
    }


    // ── Business methods ─────────────────────────────────────

    // arms the system — throws if already armed to prevent double-arming
    public void arm() throws InvalidDeviceStateException {
        if (isArmed) {
            throw new InvalidDeviceStateException("[" + name + "] Security system is already ARMED!");
        }
        this.isArmed = true;
        System.out.println("[" + name + "] Security system ARMED.");
    }

    // disarms with PIN — locks permanently after 3 wrong attempts
    public void disarm(String inputPin) throws AuthenticationException, InvalidDeviceStateException {
        if (!isArmed) {
            throw new InvalidDeviceStateException("[" + name + "] Security system is already DISARMED!");
        }
        if (failedDisarmAttempts >= 3) {
            throw new AuthenticationException("[" + name + "] Too many failed attempts! System locked.");
        }
        if (!this.adminPin.equals(inputPin)) {
            failedDisarmAttempts++; // increment before throwing so count is saved
            throw new AuthenticationException(
                    "[" + name + "] Wrong PIN! Attempts remaining: " + (3 - failedDisarmAttempts)
            );
        }
        this.isArmed              = false;
        this.failedDisarmAttempts = 0; // reset counter on success
        System.out.println("[" + name + "] Security system DISARMED.");
    }

    // triggers an alert — system must be armed first, then notifies the notification system
    public void triggerAlert() throws SmartHomeException {
        if (!isArmed) {
            throw new InvalidDeviceStateException("[" + name + "] Cannot trigger alert, system is not ARMED!");
        }
        System.out.println("[" + name + "] *** ALERT TRIGGERED! ***");
        if (notificationSystem != null) notificationSystem.motionDetected(); // null-safe check
    }


    // ── Override ─────────────────────────────────────────────

    @Override
    public void showStatus() {
        System.out.println("  Security Sys | " + name + " | Status: " + getStatusString()
                + " | Armed: " + (isArmed ? "YES" : "NO"));
    }
}