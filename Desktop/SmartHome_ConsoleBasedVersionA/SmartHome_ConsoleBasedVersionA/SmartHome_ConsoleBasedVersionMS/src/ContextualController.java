import exceptions.AutomationException;
import exceptions.InvalidDeviceStateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextualController {

    private String              currentMode;
    private Map<String, String> scenes;   // scene name → description

    // ── Constructor ──────────────────────────────────────────
    public ContextualController() {
        this.currentMode = "Normal";
        this.scenes      = new HashMap<>();
        configureScene(); // load default scenes
    }

    // ── Getters & Setters ────────────────────────────────────
    public String getCurrentMode()         { return currentMode; }
    public void   setCurrentMode(String m) { this.currentMode = m; }
    public Map<String, String> getScenes() { return scenes; }

    // ── Business methods ─────────────────────────────────────
    public void configureScene() {
        scenes.put("Cinema", "Lights OFF, AC 22°C, Fan speed 2");
        scenes.put("Sleep",  "Lights OFF, AC 20°C, Fan speed 1, Door LOCKED");
        scenes.put("Away",   "All lights OFF, Security ARMED, Door LOCKED");
        scenes.put("Normal", "Lights 70%, AC 24°C, Fan speed 3");
    }

    // throws exception if mode is null or empty or unknown
    public void applyMode(String mode, List<Device> devices)
            throws AutomationException, InvalidDeviceStateException {

        if (mode == null || mode.trim().isEmpty()) {
            throw new AutomationException(
                    "[ContextualController] Mode cannot be empty!"
            );
        }
        if (devices == null || devices.isEmpty()) {
            throw new AutomationException(
                    "[ContextualController] No devices found!"
            );
        }

        this.currentMode = mode;
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

    public void setCinemaMode(List<Device> devices) throws InvalidDeviceStateException {
        System.out.println("  [Cinema Mode] Dimming lights, cooling room...");
        for (Device d : devices) {
            try {
                if (d instanceof Light) {
                    d.turnOff();
                }
                if (d instanceof AirConditioner) {
                    ((AirConditioner) d).setTemperature(22, true);
                    if (!d.getStatus()) d.turnOn();
                }
                if (d instanceof Fan) {
                    ((Fan) d).setSpeed(2, true);
                    if (!d.getStatus()) d.turnOn();
                }
            } catch (InvalidDeviceStateException e) {
                // skip already ON/OFF devices and continue with rest
                System.out.println("  [Cinema Mode] Skipping " + d.getName() + ": " + e.getMessage());
            }
        }
    }

    public void setSleepMode(List<Device> devices) throws InvalidDeviceStateException {
        System.out.println("  [Sleep Mode] Turning off lights, locking doors, cooling room...");
        for (Device d : devices) {
            try {
                if (d instanceof Light) {
                    d.turnOff();
                }
                if (d instanceof AirConditioner) {
                    ((AirConditioner) d).setTemperature(20, true);
                    if (!d.getStatus()) d.turnOn();
                }
                if (d instanceof Fan) {
                    ((Fan) d).setSpeed(1, true);
                    if (!d.getStatus()) d.turnOn();
                }
                if (d instanceof Door) {
                    ((Door) d).lock();
                }
            } catch (InvalidDeviceStateException e) {
                // skip already ON/OFF/LOCKED devices and continue with rest
                System.out.println("  [Sleep Mode] Skipping " + d.getName() + ": " + e.getMessage());
            }
        }
    }

    public void setAwayMode(List<Device> devices) throws InvalidDeviceStateException {
        System.out.println("  [Away Mode] Securing home...");
        for (Device d : devices) {
            try {
                if (d instanceof Light) {
                    d.turnOff();
                }
                if (d instanceof Door) {
                    ((Door) d).lock();
                }
                if (d instanceof SecuritySystem) {
                    ((SecuritySystem) d).arm();
                }
                if (d instanceof AirConditioner || d instanceof Fan) {
                    d.turnOff();
                }
            } catch (InvalidDeviceStateException e) {
                // skip already handled devices and continue with rest
                System.out.println("  [Away Mode] Skipping " + d.getName() + ": " + e.getMessage());
            }
        }
    }
}