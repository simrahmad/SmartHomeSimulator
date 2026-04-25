import exceptions.InvalidDeviceStateException;

public class Light extends Device {

    private int brightness;   // 0–100

    // ── Constructors ─────────────────────────────────────────
    public Light(String name, String deviceId) {
        super(name, deviceId);
        this.brightness = 50;   // default brightness
    }

    public Light(String name, String deviceId, int brightness)
            throws InvalidDeviceStateException {
        super(name, deviceId);
        setBrightness(brightness); // use setter so exception is thrown if invalid
    }

    // ── Getters & Setters ────────────────────────────────────
    public int getBrightness() { return brightness; }

    // NOW throws exception instead of silently clamping
    public void setBrightness(int level) throws InvalidDeviceStateException {
        if (level < 0 || level > 100) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Invalid brightness: " + level + ". Must be between 0-100!"
            );
        }
        this.brightness = level;
    }

    // ── Overloaded setBrightness ─────────────────────────────
    public void setBrightness(int level, boolean verbose) throws InvalidDeviceStateException {
        setBrightness(level); // reuse above method so exception is thrown
        if (verbose) System.out.println("[" + name + "] brightness set to " + brightness + "%.");
    }

    // ── Override turnOn to add Light specific message ────────
    @Override
    public void turnOn() throws InvalidDeviceStateException {
        super.turnOn(); // Device checks if already ON
        System.out.println("[" + name + "] Light turned on at " + brightness + "% brightness.");
    }

    // ── Override turnOff to add Light specific message ───────
    @Override
    public void turnOff() throws InvalidDeviceStateException {
        super.turnOff(); // Device checks if already OFF
        System.out.println("[" + name + "] Light turned off.");
    }

    // ── autoTurnOff ──────────────────────────────────────────
    public void autoTurnOff() throws InvalidDeviceStateException {
        turnOff(); // now properly throws exception if already OFF
        System.out.println("[" + name + "] auto-turned OFF by schedule.");
    }

    // ── Override ─────────────────────────────────────────────
    @Override
    public void showStatus() {
        System.out.println("  Light        | " + name + " | Status: " + getStatusString()
                + " | Brightness: " + brightness + "%");
    }
}