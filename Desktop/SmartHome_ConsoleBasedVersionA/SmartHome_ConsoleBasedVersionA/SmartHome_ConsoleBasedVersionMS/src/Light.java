import exceptions.InvalidDeviceStateException;

// smart light bulb — controls brightness between 0% and 100%
public class Light extends Device {

    private int brightness; // current brightness percentage 0–100


    // ── Constructors ─────────────────────────────────────────

    // defaults to 50% — a comfortable mid-level brightness
    public Light(String name, String deviceId) {
        super(name, deviceId);
        this.brightness = 50;
    }

    // custom starting brightness — throws if outside 0–100
    public Light(String name, String deviceId, int brightness)
            throws InvalidDeviceStateException {
        super(name, deviceId);
        setBrightness(brightness); // setter runs the validation at construction time
    }


    // ── Getters & Setters ────────────────────────────────────

    public int getBrightness() { return brightness; }

    // rejects anything outside 0–100 — no silent clamping
    public void setBrightness(int level) throws InvalidDeviceStateException {
        if (level < 0 || level > 100) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Invalid brightness: " + level + ". Must be between 0-100!"
            );
        }
        this.brightness = level;
    }

    // same as above but prints a confirmation if verbose is true
    public void setBrightness(int level, boolean verbose) throws InvalidDeviceStateException {
        setBrightness(level); // reuse validation — no duplicate logic
        if (verbose) System.out.println("[" + name + "] brightness set to " + brightness + "%.");
    }


    // ── Overrides ────────────────────────────────────────────

    // turns light on and announces the brightness level
    @Override
    public void turnOn() throws InvalidDeviceStateException {
        super.turnOn(); // throws if already ON
        System.out.println("[" + name + "] Light turned on at " + brightness + "% brightness.");
    }

    // turns light off
    @Override
    public void turnOff() throws InvalidDeviceStateException {
        super.turnOff(); // throws if already OFF
        System.out.println("[" + name + "] Light turned off.");
    }

    // called by the automation scheduler — delegates to turnOff() so the guard still applies
    public void autoTurnOff() throws InvalidDeviceStateException {
        turnOff(); // throws if already OFF
        System.out.println("[" + name + "] auto-turned OFF by schedule.");
    }

    // prints a one-line status summary
    @Override
    public void showStatus() {
        System.out.println("  Light        | " + name + " | Status: " + getStatusString()
                + " | Brightness: " + brightness + "%");
    }
}