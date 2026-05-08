import exceptions.InvalidDeviceStateException;

// smart fan — controls rotation speed between 1 (quietest) and 5 (fastest)
public class Fan extends Device {

    private int speed; // current speed level 1–5


    // ── Constructors ─────────────────────────────────────────

    // defaults to speed 1 — quietest setting
    public Fan(String name, String deviceId) {
        super(name, deviceId);
        this.speed = 1;
    }

    // custom starting speed — throws if outside 1–5
    public Fan(String name, String deviceId, int speed)
            throws InvalidDeviceStateException {
        super(name, deviceId);
        setSpeed(speed); // setter runs the validation at construction time
    }


    // ── Getters & Setters ────────────────────────────────────

    public int getSpeed() { return speed; }

    // rejects anything outside 1–5 — no silent clamping
    public void setSpeed(int level) throws InvalidDeviceStateException {
        if (level < 1 || level > 5) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Invalid speed: " + level + ". Must be between 1-5!"
            );
        }
        this.speed = level;
    }

    // same as above but prints a confirmation if verbose is true
    public void setSpeed(int level, boolean verbose) throws InvalidDeviceStateException {
        setSpeed(level); // reuse validation — no duplicate logic
        if (verbose) System.out.println("[" + name + "] speed set to " + speed + ".");
    }


    // ── Overrides ────────────────────────────────────────────

    // turns fan on and announces the starting speed
    @Override
    public void turnOn() throws InvalidDeviceStateException {
        super.turnOn(); // throws if already ON
        System.out.println("[" + name + "] Fan started at speed " + speed + ".");
    }

    // turns fan off
    @Override
    public void turnOff() throws InvalidDeviceStateException {
        super.turnOff(); // throws if already OFF
        System.out.println("[" + name + "] Fan stopped.");
    }

    // prints a one-line status summary
    @Override
    public void showStatus() {
        System.out.println("  Fan          | " + name + " | Status: " + getStatusString()
                + " | Speed: " + speed + "/5");
    }
}