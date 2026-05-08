// ============================================================
//  ContextualController.java  –  Scene / mode manager
//
//  PURPOSE:
//    Applies pre-defined "modes" that configure multiple devices
//    at once to match a usage scenario.  Supported modes:
//      • Cinema  – dim lights, cool room to 22°C, moderate fan
//      • Sleep   – lights off, AC to 20°C, quiet fan, lock doors
//      • Away    – all lights off, arm security, lock doors,
//                  switch off AC and fans
//
//  DESIGN NOTES:
//    - The scenes map is populated in configureScene() at
//      construction time and holds human-readable descriptions of
//      each mode. These descriptions are for documentation /
//      display purposes; actual device logic lives in the
//      set*Mode() helpers.
//    - Each set*Mode() helper is intentionally forgiving: if a
//      device is already in the desired state (e.g. a light that
//      is already off when Sleep mode fires), the resulting
//      InvalidDeviceStateException is caught and logged rather
//      than aborting the entire mode application. This ensures
//      that one stubborn device never prevents the rest from
//      being configured.
//    - applyMode() uses a switch expression (Java 14+) for
//      clean, readable dispatch. Unknown modes throw an exception
//      rather than silently falling through.
// ============================================================

import exceptions.AutomationException;
import exceptions.InvalidDeviceStateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextualController {

    /**
     * The mode that is currently active (e.g. "Cinema", "Sleep", "Normal").
     * Starts as "Normal" — the baseline, daytime configuration.
     */
    private String currentMode;

    /**
     * Map of mode name → human-readable description.
     * Used for display and documentation; actual configuration logic
     * lives in the individual set*Mode() methods.
     */
    private Map<String, String> scenes;   // scene name → description

    // ── Constructor ──────────────────────────────────────────

    /**
     * Creates a ContextualController in "Normal" mode and immediately
     * populates the scenes map with all default mode descriptions.
     * The default mode means no special configuration is applied —
     * devices remain in their individually managed states.
     */
    public ContextualController() {
        this.currentMode = "Normal";
        this.scenes      = new HashMap<>();
        configureScene(); // pre-load all default scene descriptions
    }

    // ── Getters & Setters ────────────────────────────────────

    /** @return The name of the currently active mode (e.g. "Cinema"). */
    public String getCurrentMode()         { return currentMode; }

    /**
     * Directly updates the current mode label without applying any
     * device changes. Used after applyMode() completes so the label
     * stays in sync, or during deserialisation.
     */
    public void   setCurrentMode(String m) { this.currentMode = m; }

    /** @return The full scenes map (mode name → description string). */
    public Map<String, String> getScenes() { return scenes; }

    // ── Business methods ─────────────────────────────────────

    /**
     * Populates the scenes map with human-readable descriptions for
     * each supported mode. Called once at construction; can be called
     * again to reset descriptions to defaults if they were altered.
     *
     * These strings are informational only — changing them does not
     * change what the set*Mode() methods actually do to devices.
     */
    public void configureScene() {
        scenes.put("Cinema", "Lights OFF, AC 22°C, Fan speed 2");
        scenes.put("Sleep",  "Lights OFF, AC 20°C, Fan speed 1, Door LOCKED");
        scenes.put("Away",   "All lights OFF, Security ARMED, Door LOCKED");
        scenes.put("Normal", "Lights 70%, AC 24°C, Fan speed 3");
    }

    /**
     * Applies a named mode to all devices in the home.
     *
     * Validation:
     *   - mode must not be null or blank.
     *   - devices must not be null or empty.
     *   - mode must match one of the known cases: cinema / sleep / away.
     *     (Normal is handled by the devices themselves; switching "off" a
     *     mode is done by manually adjusting individual devices.)
     *
     * @param mode    The mode to activate (case-insensitive).
     * @param devices The full list of home devices to configure.
     * @throws AutomationException         if mode is invalid or unknown.
     * @throws InvalidDeviceStateException propagated from set*Mode() helpers.
     */
    public void applyMode(String mode, List<Device> devices)
            throws AutomationException, InvalidDeviceStateException {

        // Guard: a null/blank mode string can't be dispatched
        if (mode == null || mode.trim().isEmpty()) {
            throw new AutomationException(
                    "[ContextualController] Mode cannot be empty!"
            );
        }
        // Guard: can't configure devices if there aren't any
        if (devices == null || devices.isEmpty()) {
            throw new AutomationException(
                    "[ContextualController] No devices found!"
            );
        }

        // Update the stored mode label BEFORE applying so it's accurate even if
        // a device throws partway through the configuration sequence
        this.currentMode = mode;
        System.out.println("\n[ContextualController] Applying mode: " + mode);

        // Dispatch to the appropriate helper — case-insensitive comparison
        switch (mode.toLowerCase()) {
            case "cinema" -> setCinemaMode(devices);
            case "sleep"  -> setSleepMode(devices);
            case "away"   -> setAwayMode(devices);
            default       -> throw new AutomationException(
                    "[ContextualController] Unknown mode: '" + mode + "'"
            );
        }
    }

    /**
     * Applies Cinema mode: dims all lights, sets AC to 22°C, fan to speed 2.
     *
     * Per-device errors (e.g. "Light already OFF") are caught and logged
     * rather than propagated, so one device in an unexpected state does not
     * prevent the rest from being configured.
     *
     * @param devices All home devices; non-matching types are skipped.
     * @throws InvalidDeviceStateException only re-thrown for truly unexpected errors
     *                                     (currently all errors are caught internally).
     */
    public void setCinemaMode(List<Device> devices) throws InvalidDeviceStateException {
        System.out.println("  [Cinema Mode] Dimming lights, cooling room...");
        for (Device d : devices) {
            try {
                if (d instanceof Light) {
                    // Turn off all lights — cinema experience requires darkness
                    d.turnOff();
                }
                if (d instanceof AirConditioner) {
                    // Cool the room to a comfortable cinema temperature
                    ((AirConditioner) d).setTemperature(22, true);
                    // Turn on if not already running
                    if (!d.getStatus()) d.turnOn();
                }
                if (d instanceof Fan) {
                    // Set a moderate fan speed — keeps air moving without noise
                    ((Fan) d).setSpeed(2, true);
                    // Turn on if not already running
                    if (!d.getStatus()) d.turnOn();
                }
            } catch (InvalidDeviceStateException e) {
                // Log the skip and continue — one device failure must not abort the whole mode
                System.out.println("  [Cinema Mode] Skipping " + d.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Applies Sleep mode: lights off, AC to 20°C, fan to speed 1 (quiet),
     * all doors locked.
     *
     * Per-device errors are caught and logged so the full device list
     * is always processed even if one device is in an unexpected state.
     *
     * @param devices All home devices; non-matching types are skipped.
     * @throws InvalidDeviceStateException only for truly unexpected errors
     *                                     (currently all errors are caught internally).
     */
    public void setSleepMode(List<Device> devices) throws InvalidDeviceStateException {
        System.out.println("  [Sleep Mode] Turning off lights, locking doors, cooling room...");
        for (Device d : devices) {
            try {
                if (d instanceof Light) {
                    // Darkness for sleeping
                    d.turnOff();
                }
                if (d instanceof AirConditioner) {
                    // Cooler temperature recommended for sleep quality
                    ((AirConditioner) d).setTemperature(20, true);
                    if (!d.getStatus()) d.turnOn();
                }
                if (d instanceof Fan) {
                    // Lowest speed — just enough airflow without waking the occupant
                    ((Fan) d).setSpeed(1, true);
                    if (!d.getStatus()) d.turnOn();
                }
                if (d instanceof Door) {
                    // Lock all doors for overnight security
                    ((Door) d).lock();
                }
            } catch (InvalidDeviceStateException e) {
                // Log and continue — never abort sleep mode due to a single device state conflict
                System.out.println("  [Sleep Mode] Skipping " + d.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Applies Away mode: all lights off, doors locked, security armed,
     * AC and fans switched off to save energy.
     *
     * Per-device errors are caught and logged so the full device list
     * is always processed.
     *
     * @param devices All home devices; non-matching types are skipped.
     * @throws InvalidDeviceStateException only for truly unexpected errors
     *                                     (currently all errors are caught internally).
     */
    public void setAwayMode(List<Device> devices) throws InvalidDeviceStateException {
        System.out.println("  [Away Mode] Securing home...");
        for (Device d : devices) {
            try {
                if (d instanceof Light) {
                    // All lights off — no one is home
                    d.turnOff();
                }
                if (d instanceof Door) {
                    // Lock every door for physical security
                    ((Door) d).lock();
                }
                if (d instanceof SecuritySystem) {
                    // Arm the alarm system to monitor for intrusions
                    ((SecuritySystem) d).arm();
                }
                if (d instanceof AirConditioner || d instanceof Fan) {
                    // No climate control needed when the home is unoccupied
                    d.turnOff();
                }
            } catch (InvalidDeviceStateException e) {
                // Log and continue — already-locked doors etc. should not halt the mode
                System.out.println("  [Away Mode] Skipping " + d.getName() + ": " + e.getMessage());
            }
        }
    }
}
