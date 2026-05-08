// ============================================================
//  Device.java  –  Abstract base class for all smart devices
//
//  PURPOSE:
//    This class serves as the foundation for every device in
//    the smart home system. It defines shared fields (name,
//    status, deviceId), common behaviours (turnOn / turnOff),
//    and declares the abstract method showStatus() that every
//    concrete device MUST implement.
//
//  DESIGN NOTES:
//    - Uses the Template Method pattern: turnOn/turnOff contain
//      the shared guard logic (already ON/OFF check), and each
//      subclass calls super.turnOn() / super.turnOff() before
//      adding its own device-specific message.
//    - Implements Serializable so SmartHome can persist all
//      devices to disk without extra boilerplate in subclasses.
// ============================================================

import exceptions.InvalidDeviceStateException;
import exceptions.DeviceException;
import exceptions.DeviceNotFoundException;
import java.io.Serializable;

public abstract class Device implements Serializable {

    // ── Fields ───────────────────────────────────────────────

    /** Human-readable label shown in status printouts (e.g. "Living Room Light"). */
    protected String name;

    /**
     * Current power state of this device.
     * true  = device is ON  (actively running / powered)
     * false = device is OFF (idle / unpowered)
     * Defaults to false — all devices start in the OFF state.
     */
    protected boolean status;

    /**
     * Unique identifier for this device within the smart home
     * (e.g. "D001", "D002"). Used for persistence and lookup.
     */
    protected String deviceId;

    /**
     * Required by the Serializable contract.
     * Changing this value would invalidate previously serialised
     * files, so it is kept as a fixed constant.
     */
    private static final long serialVersionUID = 1L;


    // ── Constructor ──────────────────────────────────────────

    /**
     * Initialises a device with a display name and a unique ID.
     * Status is explicitly set to false (OFF) to guarantee a
     * predictable starting state regardless of memory content.
     *
     * @param name     Human-readable name for this device.
     * @param deviceId Unique identifier string (e.g. "D003").
     */
    public Device(String name, String deviceId) {
        this.name     = name;
        this.deviceId = deviceId;
        this.status   = false;     // devices start OFF by default
    }

    // ── Getters & Setters ────────────────────────────────────

    /** @return The display name of this device. */
    public String getName()            { return name; }

    /** Renames the device (e.g. after a room reassignment). */
    public void   setName(String n)    { this.name = n; }

    /**
     * @return true if the device is currently ON, false if OFF.
     *         Prefer {@link #getStatusString()} for display purposes.
     */
    public boolean getStatus()         { return status; }

    /**
     * Directly sets the power state without triggering the
     * guard logic in turnOn()/turnOff(). Use sparingly — mainly
     * needed during deserialisation or test setup.
     */
    public void    setStatus(boolean s){ this.status = s; }

    /** @return The unique identifier string for this device. */
    public String getDeviceId()        { return deviceId; }

    /** Updates the device ID (e.g. after a hardware swap). */
    public void   setDeviceId(String id){ this.deviceId = id; }


    // ── Concrete shared behaviours ───────────────────────────

    /**
     * Powers the device ON.
     *
     * Guard check: throws an exception if the device is already ON,
     * preventing duplicate-on operations and giving callers a clear
     * signal about what went wrong.
     *
     * Subclasses should call super.turnOn() first, then print their
     * own device-specific message (e.g. "AC started cooling at 22°C").
     *
     * @throws InvalidDeviceStateException if the device is already ON.
     */
    public void turnOn() throws InvalidDeviceStateException {
        // Reject the call if the device is already running
        if (status) {
            throw new InvalidDeviceStateException("[" + name + "] is already ON!");
        }
        // Flip the status flag to ON
        this.status = true;
        System.out.println("[" + name + "] turned ON.");
    }

    /**
     * Powers the device OFF.
     *
     * Guard check: throws an exception if the device is already OFF,
     * preventing duplicate-off operations.
     *
     * Subclasses should call super.turnOff() first, then print their
     * own device-specific shutdown message.
     *
     * @throws InvalidDeviceStateException if the device is already OFF.
     */
    public void turnOff() throws InvalidDeviceStateException {
        // Reject the call if the device is already idle
        if (!status) {
            throw new InvalidDeviceStateException("[" + name + "] is already OFF!");
        }
        // Flip the status flag to OFF
        this.status = false;
        System.out.println("[" + name + "] turned OFF.");
    }

    /**
     * Converts the boolean status into a human-readable label.
     *
     * @return "ON" if the device is powered, "OFF" otherwise.
     *         Used in showStatus() implementations across all subclasses.
     */
    public String getStatusString() {
        return status ? "ON" : "OFF";
    }

    // ── Abstract method – every subclass must override ───────

    /**
     * Prints a one-line status summary for this device to stdout.
     *
     * Each subclass must provide its own implementation that includes
     * device-type-specific details (e.g. brightness for lights,
     * temperature for ACs, lock state for doors).
     *
     * Called by Admin.viewAllDevices() and SmartHome.showAllDevices().
     */
    public abstract void showStatus();
}
