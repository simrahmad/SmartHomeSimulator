import exceptions.InvalidDeviceStateException;

public class Fan extends Device {

    private int speed;   // 1–5

    // ── Constructors ─────────────────────────────────────────
    public Fan(String name, String deviceId) {
        super(name, deviceId);
        this.speed = 1;
    }

    public Fan(String name, String deviceId, int speed)
            throws InvalidDeviceStateException {
        super(name, deviceId);
        setSpeed(speed); // use setter so exception is thrown if invalid
    }

    // ── Getters & Setters ────────────────────────────────────
    public int getSpeed() { return speed; }

    // NOW throws exception instead of silently clamping
    public void setSpeed(int level) throws InvalidDeviceStateException {
        if (level < 1 || level > 5) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Invalid speed: " + level + ". Must be between 1-5!"
            );
        }
        this.speed = level;
    }

    // ── Overloaded setSpeed ──────────────────────────────────
    public void setSpeed(int level, boolean verbose) throws InvalidDeviceStateException {
        setSpeed(level); // reuse above method so exception is thrown
        if (verbose) System.out.println("[" + name + "] speed set to " + speed + ".");
    }

    // ── Override turnOn to add Fan specific message ──────────
    @Override
    public void turnOn() throws InvalidDeviceStateException {
        super.turnOn(); // Device checks if already ON
        System.out.println("[" + name + "] Fan started at speed " + speed + ".");
    }

    // ── Override turnOff to add Fan specific message ─────────
    @Override
    public void turnOff() throws InvalidDeviceStateException {
        super.turnOff(); // Device checks if already OFF
        System.out.println("[" + name + "] Fan stopped.");
    }

    // ── Override ─────────────────────────────────────────────
    @Override
    public void showStatus() {
        System.out.println("  Fan          | " + name + " | Status: " + getStatusString()
                + " | Speed: " + speed + "/5");
    }
}