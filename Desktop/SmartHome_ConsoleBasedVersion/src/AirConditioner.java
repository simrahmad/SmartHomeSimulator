import exceptions.InvalidDeviceStateException;

// Smart air conditioner — controls temperature between 16°C and 30°C
public class AirConditioner extends Device {

    private int temperature; // target cooling temp in Celsius


    // ── Constructors ─────────────────────────────────────────

    // defaults to 24°C — a comfortable room temperature
    public AirConditioner(String name, String deviceId) {
        super(name, deviceId);
        this.temperature = 24;
    }

    // custom starting temperature — throws if outside 16–30 range
    public AirConditioner(String name, String deviceId, int temperature)
            throws InvalidDeviceStateException {
        super(name, deviceId);
        setTemperature(temperature); // setter runs the validation at construction time
    }


    // ── Getters & Setters ────────────────────────────────────

    public int getTemperature() { return temperature; }

    // rejects anything outside 16–30 — no silent clamping
    public void setTemperature(int temp) throws InvalidDeviceStateException {
        if (temp < 16 || temp > 30) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Invalid temperature: " + temp + "°C. Must be between 16-30°C!"
            );
        }
        this.temperature = temp;
    }

    // same as above but prints a confirmation if verbose is true
    public void setTemperature(int temp, boolean verbose) throws InvalidDeviceStateException {
        setTemperature(temp); // reuse validation — no duplicate logic
        if (verbose) System.out.println("[" + name + "] temperature set to " + temperature + "°C.");
    }


    // ── Overrides ────────────────────────────────────────────

    // turns AC on and announces the cooling temperature
    @Override
    public void turnOn() throws InvalidDeviceStateException {
        super.turnOn(); // throws if already ON
        System.out.println("[" + name + "] AC started cooling at " + temperature + "°C.");
    }

    // turns AC off
    @Override
    public void turnOff() throws InvalidDeviceStateException {
        super.turnOff(); // throws if already OFF
        System.out.println("[" + name + "] AC stopped.");
    }

    // prints a one-line status summary
    @Override
    public void showStatus() {
        System.out.println("  AC           | " + name + " | Status: " + getStatusString()
                + " | Temp: " + temperature + "°C");
    }
}