import exceptions.InvalidDeviceStateException;
import exceptions.DeviceException;
import exceptions.DeviceNotFoundException;
import java.io.Serializable;

// Base class for all smart devices — every device inherits from here
public abstract class Device implements Serializable {

    // ── Fields ───────────────────────────────────────────────

    protected String name;      // device label e.g. "Living Room Light"
    protected boolean status;   // true = ON, false = OFF
    protected String deviceId;  // unique ID e.g. "D001"

    private static final long serialVersionUID = 1L; // keeps saved files compatible


    // ── Constructor ──────────────────────────────────────────

    // every device starts OFF by default
    public Device(String name, String deviceId) {
        this.name     = name;
        this.deviceId = deviceId;
        this.status   = false;
    }

    // ── Getters & Setters ────────────────────────────────────

    public String getName()             { return name; }
    public void   setName(String n)     { this.name = n; }

    public boolean getStatus()          { return status; }
    public void    setStatus(boolean s) { this.status = s; } // use only for testing or loading from file

    public String getDeviceId()         { return deviceId; }
    public void   setDeviceId(String id){ this.deviceId = id; }


    // ── Shared behaviours ────────────────────────────────────

    // turns device ON — throws if it's already ON
    public void turnOn() throws InvalidDeviceStateException {
        if (status) {
            throw new InvalidDeviceStateException("[" + name + "] is already ON!");
        }
        this.status = true;
        System.out.println("[" + name + "] turned ON.");
    }

    // turns device OFF — throws if it's already OFF
    public void turnOff() throws InvalidDeviceStateException {
        if (!status) {
            throw new InvalidDeviceStateException("[" + name + "] is already OFF!");
        }
        this.status = false;
        System.out.println("[" + name + "] turned OFF.");
    }

    // returns "ON" or "OFF" as a readable string
    public String getStatusString() {
        return status ? "ON" : "OFF";
    }

    // ── Abstract ─────────────────────────────────────────────

    // each device prints its own status line — must be implemented by subclasses
    public abstract void showStatus();
}