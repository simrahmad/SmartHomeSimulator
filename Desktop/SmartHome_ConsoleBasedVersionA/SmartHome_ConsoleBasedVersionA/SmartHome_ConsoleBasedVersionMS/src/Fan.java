// ============================================================
//  Fan.java  –  Represents a smart ceiling / room fan
//
//  PURPOSE:
//    Extends Device to add speed control (levels 1–5).
//    Speed 1 is the quietest setting (used in Sleep mode),
//    while speed 5 is the highest (used for rapid cooling).
//
//  DESIGN NOTES:
//    - Speed is validated strictly: values outside 1–5 throw
//      InvalidDeviceStateException rather than being silently
//      clamped, so callers are always aware of bad input.
//    - Two overloaded setSpeed() variants mirror the same
//      pattern used in Light and AirConditioner: the verbose
//      overload adds a confirmation message without duplicating
//      the validation logic.
//    - turnOn() and turnOff() call super first so the shared
//      "already ON/OFF" guard in Device runs before fan-specific
//      messages are printed.
// ============================================================

import exceptions.InvalidDeviceStateException;

public class Fan extends Device {

    /**
     * Current rotation speed of this fan.
     * Valid range: 1 (slowest / quietest) to 5 (fastest / loudest).
     * Defaults to 1 so the fan starts at its lowest speed when
     * first powered on.
     */
    private int speed;   // 1–5

    // ── Constructors ─────────────────────────────────────────

    /**
     * Creates a fan with the default speed of 1 (lowest setting).
     *
     * @param name     Display name (e.g. "Ceiling Fan").
     * @param deviceId Unique ID (e.g. "D003").
     */
    public Fan(String name, String deviceId) {
        super(name, deviceId);
        this.speed = 1;  // safest/quietest default
    }

    /**
     * Creates a fan with a custom initial speed.
     * Delegates to setSpeed() so the 1–5 range guard runs at
     * construction time rather than storing an invalid value silently.
     *
     * @param name     Display name.
     * @param deviceId Unique ID.
     * @param speed    Initial speed level (1–5).
     * @throws InvalidDeviceStateException if speed is outside 1–5.
     */
    public Fan(String name, String deviceId, int speed)
            throws InvalidDeviceStateException {
        super(name, deviceId);
        setSpeed(speed); // use setter so exception is thrown if invalid
    }

    // ── Getters & Setters ────────────────────────────────────

    /** @return Current speed level (1–5). */
    public int getSpeed() { return speed; }

    /**
     * Sets the fan speed.
     *
     * Validation: values below 1 or above 5 are rejected with an
     * exception. This replaces a previous approach that silently
     * clamped the value, which hid programming errors from callers.
     *
     * @param level Desired speed level (1 = slowest, 5 = fastest).
     * @throws InvalidDeviceStateException if level is out of the valid range.
     */
    public void setSpeed(int level) throws InvalidDeviceStateException {
        // Reject any speed value outside the hardware-supported range
        if (level < 1 || level > 5) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Invalid speed: " + level + ". Must be between 1-5!"
            );
        }
        this.speed = level;
    }

    // ── Overloaded setSpeed ──────────────────────────────────

    /**
     * Sets fan speed and optionally prints a confirmation message.
     * Delegates entirely to the single-arg overload so validation
     * logic is never duplicated.
     *
     * @param level   Desired speed level (1–5).
     * @param verbose If true, prints "speed set to X" after setting.
     * @throws InvalidDeviceStateException if level is out of the valid range.
     */
    public void setSpeed(int level, boolean verbose) throws InvalidDeviceStateException {
        setSpeed(level); // reuse above method — exception propagates if invalid
        // Only print the confirmation when the caller explicitly requests it
        if (verbose) System.out.println("[" + name + "] speed set to " + speed + ".");
    }

    // ── Override turnOn to add Fan specific message ──────────

    /**
     * Powers the fan ON and prints the speed it starts at.
     * Calls super.turnOn() first so the "already ON" guard in
     * Device runs — if the fan is already running, an exception is
     * thrown before the speed message is printed.
     *
     * @throws InvalidDeviceStateException if the fan is already ON.
     */
    @Override
    public void turnOn() throws InvalidDeviceStateException {
        super.turnOn(); // Device checks if already ON — throws exception if so
        // Report the speed level so the user knows how hard the fan is blowing
        System.out.println("[" + name + "] Fan started at speed " + speed + ".");
    }

    // ── Override turnOff to add Fan specific message ─────────

    /**
     * Powers the fan OFF.
     * Calls super.turnOff() first so the "already OFF" guard in
     * Device runs before the shutdown message is printed.
     *
     * @throws InvalidDeviceStateException if the fan is already OFF.
     */
    @Override
    public void turnOff() throws InvalidDeviceStateException {
        super.turnOff(); // Device checks if already OFF — throws exception if so
        System.out.println("[" + name + "] Fan stopped.");
    }

    // ── Override showStatus ──────────────────────────────────

    /**
     * Prints a formatted one-line status summary to stdout.
     * Format: "  Fan  | <name> | Status: ON/OFF | Speed: X/5"
     * Called by Admin.viewAllDevices() and SmartHome.showAllDevices().
     */
    @Override
    public void showStatus() {
        System.out.println("  Fan          | " + name + " | Status: " + getStatusString()
                + " | Speed: " + speed + "/5");
    }
}
