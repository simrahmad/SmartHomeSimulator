// ============================================================
//  AirConditioner.java  –  Represents a smart air conditioner
//
//  PURPOSE:
//    Extends Device to add temperature control (16–30 °C).
//    The valid range reflects a realistic hardware constraint:
//    below 16 °C risks condensation damage; above 30 °C would
//    just be a heating function most ACs don't support.
//
//  DESIGN NOTES:
//    - Temperature is validated strictly: values outside 16–30
//      throw InvalidDeviceStateException rather than being
//      silently clamped, so callers are always aware of bad input.
//    - Two overloaded setTemperature() variants follow the same
//      pattern as Fan.setSpeed() and Light.setBrightness().
//    - turnOn() and turnOff() call super first so the shared
//      "already ON/OFF" guard in Device runs before AC-specific
//      messages are printed.
//    - Default temperature is 24 °C — a widely recognised
//      comfortable indoor temperature.
// ============================================================

import exceptions.InvalidDeviceStateException;

public class AirConditioner extends Device {

    /**
     * Target cooling temperature in degrees Celsius.
     * Valid range: 16°C (coldest) to 30°C (warmest).
     * Default of 24°C is the standard "comfortable" setpoint.
     */
    private int temperature;   // in Celsius

    // ── Constructors ─────────────────────────────────────────

    /**
     * Creates an AC unit with the default temperature of 24°C.
     *
     * @param name     Display name (e.g. "Main AC").
     * @param deviceId Unique ID (e.g. "D004").
     */
    public AirConditioner(String name, String deviceId) {
        super(name, deviceId);
        this.temperature = 24;   // comfortable default — no exception possible here
    }

    /**
     * Creates an AC unit with a custom initial temperature.
     * Delegates to setTemperature() so the 16–30 range guard runs
     * at construction time rather than storing an invalid value.
     *
     * @param name        Display name.
     * @param deviceId    Unique ID.
     * @param temperature Initial temperature in °C (16–30).
     * @throws InvalidDeviceStateException if temperature is outside 16–30.
     */
    public AirConditioner(String name, String deviceId, int temperature)
            throws InvalidDeviceStateException {
        super(name, deviceId);
        setTemperature(temperature); // use setter so exception is thrown if invalid
    }

    // ── Getters & Setters ────────────────────────────────────

    /** @return Current target temperature in °C. */
    public int getTemperature() { return temperature; }

    /**
     * Sets the target cooling temperature.
     *
     * Validation: values below 16 or above 30 are rejected with an
     * exception. This replaces a previous approach that silently
     * clamped the value, which hid programming errors from callers.
     *
     * @param temp Desired temperature in °C (16–30).
     * @throws InvalidDeviceStateException if temp is out of the valid range.
     */
    public void setTemperature(int temp) throws InvalidDeviceStateException {
        // Reject any temperature outside the hardware-supported range
        if (temp < 16 || temp > 30) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Invalid temperature: " + temp + "°C. Must be between 16-30°C!"
            );
        }
        this.temperature = temp;
    }

    // ── Overloaded setTemperature ────────────────────────────

    /**
     * Sets temperature and optionally prints a confirmation message.
     * Delegates entirely to the single-arg overload so validation
     * logic is never duplicated.
     *
     * @param temp    Desired temperature in °C (16–30).
     * @param verbose If true, prints "temperature set to X°C" after setting.
     * @throws InvalidDeviceStateException if temp is out of the valid range.
     */
    public void setTemperature(int temp, boolean verbose) throws InvalidDeviceStateException {
        setTemperature(temp); // reuse above method — exception propagates if invalid
        // Only print the confirmation when the caller explicitly requests it
        if (verbose) System.out.println("[" + name + "] temperature set to " + temperature + "°C.");
    }

    // ── Override turnOn to add AC specific message ───────────

    /**
     * Powers the AC ON and announces the target cooling temperature.
     * Calls super.turnOn() first so the "already ON" guard in
     * Device runs — if the AC is already running, an exception is
     * thrown before the temperature message is printed.
     *
     * @throws InvalidDeviceStateException if the AC is already ON.
     */
    @Override
    public void turnOn() throws InvalidDeviceStateException {
        super.turnOn(); // Device checks if already ON — throws exception if so
        // Inform the user at which temperature the AC will maintain the room
        System.out.println("[" + name + "] AC started cooling at " + temperature + "°C.");
    }

    // ── Override turnOff to add AC specific message ──────────

    /**
     * Powers the AC OFF.
     * Calls super.turnOff() first so the "already OFF" guard in
     * Device runs before the shutdown message is printed.
     *
     * @throws InvalidDeviceStateException if the AC is already OFF.
     */
    @Override
    public void turnOff() throws InvalidDeviceStateException {
        super.turnOff(); // Device checks if already OFF — throws exception if so
        System.out.println("[" + name + "] AC stopped.");
    }

    // ── Override showStatus ──────────────────────────────────

    /**
     * Prints a formatted one-line status summary to stdout.
     * Format: "  AC  | <name> | Status: ON/OFF | Temp: X°C"
     * Called by Admin.viewAllDevices() and SmartHome.showAllDevices().
     */
    @Override
    public void showStatus() {
        System.out.println("  AC           | " + name + " | Status: " + getStatusString()
                + " | Temp: " + temperature + "°C");
    }
}
