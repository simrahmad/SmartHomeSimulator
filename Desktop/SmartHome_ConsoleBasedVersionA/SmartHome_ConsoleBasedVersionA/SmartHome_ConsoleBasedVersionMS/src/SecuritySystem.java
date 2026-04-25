import exceptions.InvalidDeviceStateException;
import exceptions.AuthenticationException;
import exceptions.SmartHomeException;

public class SecuritySystem extends Device {

    private boolean isArmed;
    private NotificationSystem notificationSystem;
    private String adminPin;          // pin to disarm the system
    private int failedDisarmAttempts; // track wrong pin attempts

    // ── Constructors ─────────────────────────────────────────
    public SecuritySystem(String name, String deviceId, NotificationSystem ns) {
        super(name, deviceId);
        this.isArmed             = false;
        this.notificationSystem  = ns;
        this.adminPin            = "1234"; // default pin
        this.failedDisarmAttempts = 0;
    }

    public SecuritySystem(String name, String deviceId) {
        super(name, deviceId);
        this.isArmed             = false;
        this.notificationSystem  = null;
        this.adminPin            = "1234"; // default pin
        this.failedDisarmAttempts = 0;
    }

    // ── Getters & Setters ────────────────────────────────────
    public boolean isArmed()           { return isArmed; }
    public void    setArmed(boolean a) { this.isArmed = a; }

    // change pin — throws exception if new pin is null or empty
    public void setAdminPin(String newPin) throws AuthenticationException {
        if (newPin == null || newPin.trim().isEmpty()) {
            throw new AuthenticationException(
                    "[" + name + "] PIN cannot be empty!"
            );
        }
        this.adminPin = newPin;
        System.out.println("[" + name + "] PIN updated successfully.");
    }

    // ── Business methods ─────────────────────────────────────

    // throws exception if already armed
    public void arm() throws InvalidDeviceStateException {
        if (isArmed) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Security system is already ARMED!"
            );
        }
        this.isArmed = true;
        System.out.println("[" + name + "] Security system ARMED.");
    }

    // throws exception if wrong pin or too many failed attempts
    public void disarm(String inputPin) throws AuthenticationException, InvalidDeviceStateException {
        if (!isArmed) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Security system is already DISARMED!"
            );
        }
        if (failedDisarmAttempts >= 3) {
            throw new AuthenticationException(
                    "[" + name + "] Too many failed attempts! System locked."
            );
        }
        if (!this.adminPin.equals(inputPin)) {
            failedDisarmAttempts++;
            throw new AuthenticationException(
                    "[" + name + "] Wrong PIN! Attempts remaining: " + (3 - failedDisarmAttempts)
            );
        }
        // correct pin
        this.isArmed              = false;
        this.failedDisarmAttempts = 0; // reset failed attempts on success
        System.out.println("[" + name + "] Security system DISARMED.");
    }

    // throws exception if system is not armed
    // FIXED CODE
    public void triggerAlert() throws SmartHomeException, SmartHomeException {
        if (!isArmed) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Cannot trigger alert, system is not ARMED!"
            );
        }
        System.out.println("[" + name + "] *** ALERT TRIGGERED! ***");
        if (notificationSystem != null) {
            notificationSystem.motionDetected();  // now properly declared
        }
    }

    // ── Override ─────────────────────────────────────────────
    @Override
    public void showStatus() {
        System.out.println("  Security Sys | " + name + " | Status: " + getStatusString()
                + " | Armed: " + (isArmed ? "YES" : "NO"));
    }
}