import exceptions.AutomationException;
import exceptions.InvalidDeviceStateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// manages pre-defined modes that configure multiple devices at once
public class ContextualController {

    private String currentMode;          // currently active mode e.g. "Cinema", "Sleep"
    private Map<String, String> scenes;  // mode name → description for display purposes


    // ── Constructor ──────────────────────────────────────────

    // starts in Normal mode and loads all default scene descriptions
    public ContextualController() {
        this.currentMode = "Normal";
        this.scenes      = new HashMap<>();
        configureScene();
    }


    // ── Getters & Setters ────────────────────────────────────

    public String getCurrentMode()         { return currentMode; }
    public void   setCurrentMode(String m) { this.currentMode = m; } // updates label without touching devices
    public Map<String, String> getScenes() { return scenes; }


    // ── Business methods ─────────────────────────────────────

    // loads human-readable descriptions for each mode — informational only, not actual device logic
    public void configureScene() {
        scenes.put("Cinema", "Lights OFF, AC 22°C, Fan speed 2");
        scenes.put("Sleep",  "Lights OFF, AC 20°C, Fan speed 1, Door LOCKED");
        scenes.put("Away",   "All lights OFF, Security ARMED, Door LOCKED");
        scenes.put("Normal", "Lights 70%, AC 24°C, Fan speed 3");
    }

    // applies a named mode to all devices — case-insensitive, throws on unknown mode
    public void applyMode(String mode, List<Device> devices)
            throws AutomationException, InvalidDeviceStateException {

        if (mode == null || mode.trim().isEmpty()) {
            throw new AutomationException("[ContextualController] Mode cannot be empty!");
        }
        if (devices == null || devices.isEmpty()) {
            throw new AutomationException("[ContextualController] No devices found!");
        }

        this.currentMode = mode; // update label before applying so it stays in sync
        System.out.println("\n[ContextualController] Applying mode: " + mode);

        switch (mode.toLowerCase()) {
            case "cinema" -> setCinemaMode(devices);
            case "sleep"  -> setSleepMode(devices);
            case "away"   -> setAwayMode(devices);
            default       -> throw new AutomationException(
                    "[ContextualController] Unknown mode: '" + mode + "'"
            );
        }
    }

    // cinema mode — lights off, AC to 22°C, fan to speed 2
    public void setCinemaMode(List<Device> devices) throws InvalidDeviceStateException {
        System.out.println("  [Cinema Mode] Dimming lights, cooling room...");
        for (Device d : devices) {
            try {
                if (d instanceof Light)          d.turnOff(); // darkness for the cinema experience
                if (d instanceof AirConditioner) {
                    ((AirConditioner) d).setTemperature(22, true);
                    if (!d.getStatus()) d.turnOn();
                }
                if (d instanceof Fan) {
                    ((Fan) d).setSpeed(2, true); // moderate speed — airflow without noise
                    if (!d.getStatus()) d.turnOn();
                }
            } catch (InvalidDeviceStateException e) {
                System.out.println("  [Cinema Mode] Skipping " + d.getName() + ": " + e.getMessage()); // one device failure never aborts the whole mode
            }
        }
    }

    // sleep mode — lights off, AC to 20°C, fan to speed 1, all doors locked
    public void setSleepMode(List<Device> devices) throws InvalidDeviceStateException {
        System.out.println("  [Sleep Mode] Turning off lights, locking doors, cooling room...");
        for (Device d : devices) {
            try {
                if (d instanceof Light)          d.turnOff();
                if (d instanceof AirConditioner) {
                    ((AirConditioner) d).setTemperature(20, true); // cooler temp improves sleep quality
                    if (!d.getStatus()) d.turnOn();
                }
                if (d instanceof Fan) {
                    ((Fan) d).setSpeed(1, true); // lowest speed — just enough airflow
                    if (!d.getStatus()) d.turnOn();
                }
                if (d instanceof Door) ((Door) d).lock(); // lock all doors overnight
            } catch (InvalidDeviceStateException e) {
                System.out.println("  [Sleep Mode] Skipping " + d.getName() + ": " + e.getMessage());
            }
        }
    }

    // away mode — lights off, doors locked, security armed, AC and fans off
    public void setAwayMode(List<Device> devices) throws InvalidDeviceStateException {
        System.out.println("  [Away Mode] Securing home...");
        for (Device d : devices) {
            try {
                if (d instanceof Light)          d.turnOff(); // no one is home
                if (d instanceof Door)           ((Door) d).lock();
                if (d instanceof SecuritySystem) ((SecuritySystem) d).arm(); // monitor for intrusions
                if (d instanceof AirConditioner || d instanceof Fan) d.turnOff(); // save energy
            } catch (InvalidDeviceStateException e) {
                System.out.println("  [Away Mode] Skipping " + d.getName() + ": " + e.getMessage());
            }
        }
    }
}