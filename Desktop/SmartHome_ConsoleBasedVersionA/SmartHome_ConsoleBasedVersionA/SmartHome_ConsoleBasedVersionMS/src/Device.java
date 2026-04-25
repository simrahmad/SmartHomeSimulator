// ============================================================
//  Device.java  –  Abstract base class for all smart devices
// ============================================================
import exceptions.InvalidDeviceStateException;
import exceptions.DeviceException;
import exceptions.DeviceNotFoundException;
import java.io.Serializable;

public abstract class Device implements Serializable {

    // ── Fields ───────────────────────────────────────────────
    protected String name;
    protected boolean status;      // true = ON, false = OFF
    protected String deviceId;
    private static final long serialVersionUID = 1L;


    // ── Constructor ──────────────────────────────────────────
    public Device(String name, String deviceId) {
        this.name     = name;
        this.deviceId = deviceId;
        this.status   = false;     // devices start OFF by default

    }

    // ── Getters & Setters ────────────────────────────────────
    public String getName()            { return name; }
    public void   setName(String n)    { this.name = n; }

    public boolean getStatus()         { return status; }
    public void    setStatus(boolean s){ this.status = s; }

    public String getDeviceId()        { return deviceId; }
    public void   setDeviceId(String id){ this.deviceId = id; }


    // ── Concrete shared behaviours ───────────────────────────
    public void turnOn() throws InvalidDeviceStateException {
        if (status) {
            throw new InvalidDeviceStateException("[" + name + "] is already ON!");
        }
        this.status = true;
        System.out.println("[" + name + "] turned ON.");
    }

    public void turnOff() throws InvalidDeviceStateException {
        if (!status) {
            throw new InvalidDeviceStateException("[" + name + "] is already OFF!");
        }
        this.status = false;
        System.out.println("[" + name + "] turned OFF.");
    }

    public String getStatusString() {
        return status ? "ON" : "OFF";
    }

    // ── Abstract method – every subclass must override ───────
    public abstract void showStatus();


}


