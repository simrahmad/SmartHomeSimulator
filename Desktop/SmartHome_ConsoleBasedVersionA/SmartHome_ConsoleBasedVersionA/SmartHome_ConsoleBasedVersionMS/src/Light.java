// ============================================================
//  Light.java  –  Represents a controllable smart light bulb
//
//  PURPOSE:
//    Extends Device to add brightness control (0–100%) and
//    provides an autoTurnOff() helper used by the Automation
//    scheduler to switch lights off at a scheduled time.
//
//  DESIGN NOTES:
//    - Brightness is validated strictly: values outside 0–100
//      throw InvalidDeviceStateException rather than being
//      silently clamped, so callers are always aware of bad input.
//    - Two overloaded setBrightness() variants follow the same
//      pattern as Fan.setSpeed() and AC.setTemperature(): the
//      verbose overload adds a confirmation message without
//      duplicating the validation logic.
//    - turnOn() and turnOff() call super first so the shared
//      "already ON/OFF" guard in Device runs before the
//      Light-specific messages are printed.
// ============================================================

import exceptions.InvalidDeviceStateException;

public class Light extends Device {

    /**
     * Brightness level of this light, expressed as a percentage.
     * Valid range: 0 (off/dim) to 100 (full brightness).
     * Default value of 50 provides a comfortable mid-point.
     */
    private int brightness;   // 0–100

    // ── Constructors ─────────────────────────────────────────

    /**
     * Creates a light with the default brightness of 50%.
     *
     * @param name     Display name (e.g. "Living Room Light").
     * @param deviceId Unique ID (e.g. "D001").
     */
    public Light(String name, String deviceId) {
        super(name, deviceId);
        this.brightness = 50;   // default brightness — comfortable mid-level
    }

    /**
     * Creates a light with a custom initial brightness.
     * Delegates to setBrightness() so the 0–100 guard runs at
     * construction time rather than silently storing an invalid value.
     *
     * @param name       Display name.
     * @param deviceId   Unique ID.
     * @param brightness Initial brightness (0–100).
     * @throws InvalidDeviceStateException if brightness is outside 0–100.
     */
    public Light(String name, String deviceId, int brightness)
            throws InvalidDeviceStateException {
        super(name, deviceId);
        setBrightness(brightness); // use setter so exception is thrown if invalid
    }

    // ── Getters & Setters ────────────────────────────────────

    /** @return Current brightness level (0–100). */
    public int getBrightness() { return brightness; }

    /**
     * Sets the brightness of this light.
     *
     * Validation: values below 0 or above 100 are rejected with an
     * exception. This replaces a previous approach that silently
     * clamped the value, which hid programming errors from callers.
     *
     * @param level Desired brightness percentage (0–100).
     * @throws InvalidDeviceStateException if level is out of the valid range.
     */
    public void setBrightness(int level) throws InvalidDeviceStateException {
        // Reject any value that falls outside the hardware-supported range
        if (level < 0 || level > 100) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Invalid brightness: " + level + ". Must be between 0-100!"
            );
        }
        this.brightness = level;
    }

    // ── Overloaded setBrightness ─────────────────────────────

    /**
     * Sets brightness and optionally prints a confirmation message.
     * Delegates entirely to the single-arg overload so validation
     * logic is never duplicated.
     *
     * @param level   Desired brightness percentage (0–100).
     * @param verbose If true, prints "brightness set to X%" after setting.
     * @throws InvalidDeviceStateException if level is out of the valid range.
     */
    public void setBrightness(int level, boolean verbose) throws InvalidDeviceStateException {
        setBrightness(level); // reuse above method so exception is thrown if invalid
        // Only print the confirmation when the caller explicitly requests it
        if (verbose) System.out.println("[" + name + "] brightness set to " + brightness + "%.");
    }

    // ── Override turnOn to add Light specific message ────────

    /**
     * Powers the light ON and prints its current brightness.
     * Calls super.turnOn() first so the "already ON" guard in
     * Device runs — if the light is already on, an exception is
     * thrown before the brightness message is printed.
     *
     * @throws InvalidDeviceStateException if the light is already ON.
     */
    @Override
    public void turnOn() throws InvalidDeviceStateException {
        super.turnOn(); // Device checks if already ON — throws exception if so
        // Inform the user of the brightness level at which the light came on
        System.out.println("[" + name + "] Light turned on at " + brightness + "% brightness.");
    }

    // ── Override turnOff to add Light specific message ───────

    /**
     * Powers the light OFF.
     * Calls super.turnOff() first so the "already OFF" guard in
     * Device runs before the shutdown message is printed.
     *
     * @throws InvalidDeviceStateException if the light is already OFF.
     */
    @Override
    public void turnOff() throws InvalidDeviceStateException {
        super.turnOff(); // Device checks if already OFF — throws exception if so
        System.out.println("[" + name + "] Light turned off.");
    }

    // ── autoTurnOff ──────────────────────────────────────────

    /**
     * Turns the light off automatically as part of an Automation schedule.
     * Internally delegates to turnOff() so the "already OFF" guard still
     * applies — if the light is already off when the schedule fires, an
     * exception propagates up to Automation.autoTurnOffLights().
     *
     * The extra log line distinguishes automated shutdowns from
     * manual ones in the notification log.
     *
     * @throws InvalidDeviceStateException if the light is already OFF.
     */
    public void autoTurnOff() throws InvalidDeviceStateException {
        turnOff(); // properly throws exception if the light is already OFF
        // Additional log entry so it's clear this was triggered by the scheduler
        System.out.println("[" + name + "] auto-turned OFF by schedule.");
    }

    // ── Override showStatus ──────────────────────────────────

    /**
     * Prints a formatted one-line status summary to stdout.
     * Format: "  Light  | <name> | Status: ON/OFF | Brightness: X%"
     * Called by Admin.viewAllDevices() and SmartHome.showAllDevices().
     */
    @Override
    public void showStatus() {
        System.out.println("  Light        | " + name + " | Status: " + getStatusString()
                + " | Brightness: " + brightness + "%");
    }
}
