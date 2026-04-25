import exceptions.InvalidDeviceStateException;

public class AirConditioner extends Device {

    private int temperature;   // in Celsius

    // ── Constructors ─────────────────────────────────────────
    public AirConditioner(String name, String deviceId) {
        super(name, deviceId);
        this.temperature = 24;   // comfortable default
    }

    public AirConditioner(String name, String deviceId, int temperature)
            throws InvalidDeviceStateException {
        super(name, deviceId);
        setTemperature(temperature); // use setter so exception is thrown if invalid
    }

    // ── Getters & Setters ────────────────────────────────────
    public int getTemperature() { return temperature; }

    // NOW throws exception instead of silently clamping
    public void setTemperature(int temp) throws InvalidDeviceStateException {
        if (temp < 16 || temp > 30) {
            throw new InvalidDeviceStateException(
                    "[" + name + "] Invalid temperature: " + temp + "°C. Must be between 16-30°C!"
            );
        }
        this.temperature = temp;
    }

    // ── Overloaded setTemperature ────────────────────────────
    public void setTemperature(int temp, boolean verbose) throws InvalidDeviceStateException {
        setTemperature(temp); // reuse above method so exception is thrown
        if (verbose) System.out.println("[" + name + "] temperature set to " + temperature + "°C.");
    }

    // ── Override turnOn to add AC specific message ───────────
    @Override
    public void turnOn() throws InvalidDeviceStateException {
        super.turnOn(); // Device checks if already ON
        System.out.println("[" + name + "] AC started cooling at " + temperature + "°C.");
    }

    // ── Override turnOff to add AC specific message ──────────
    @Override
    public void turnOff() throws InvalidDeviceStateException {
        super.turnOff(); // Device checks if already OFF
        System.out.println("[" + name + "] AC stopped.");
    }

    // ── Override ─────────────────────────────────────────────
    @Override
    public void showStatus() {
        System.out.println("  AC           | " + name + " | Status: " + getStatusString()
                + " | Temp: " + temperature + "°C");
    }
}