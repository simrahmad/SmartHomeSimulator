// ============================================================
//  Door.java  –  Represents a smart door with a lock mechanism
//
//  PURPOSE:
//    Extends Device to add lock/unlock functionality.
//    Doors are used by ContextualController (Sleep mode locks
//    all doors; Away mode also locks them) and can be controlled
//    directly by Guest/Admin users.
//
//  DESIGN NOTES:
//    - The lock state (isLocked) is independent of the Device
//      power status. A door is "ON" when its smart module is
//      active and "LOCKED" when the physical bolt is engaged —
//      these are separate concerns.
//    - lock() and unlock() do NOT throw exceptions for already-
//      locked / already-unlocked states. This is intentional:
//      ContextualController iterates all devices and locks every
//      door it finds; if the door is already locked it should
//      silently do nothing rather than aborting the whole loop.
//    - Defaults to isLocked = true for safety: a door is always
//      locked when first added to the system.
// ============================================================

public class Door extends Device {

    /**
     * Physical lock state of this door.
     * true  = bolt is engaged (LOCKED)
     * false = bolt is retracted (UNLOCKED)
     * Defaults to true — doors start locked for safety.
     */
    private boolean isLocked;

    // ── Constructors (overloading) ───────────────────────────

    /**
     * Creates a door that starts in the LOCKED state (default, safest option).
     *
     * @param name     Display name (e.g. "Front Door").
     * @param deviceId Unique ID (e.g. "D005").
     */
    public Door(String name, String deviceId) {
        super(name, deviceId);
        this.isLocked = true;   // locked by default — security first
    }

    /**
     * Creates a door with an explicit initial lock state.
     * Useful when restoring a door's state from persisted data.
     *
     * @param name     Display name.
     * @param deviceId Unique ID.
     * @param isLocked true to start LOCKED, false to start UNLOCKED.
     */
    public Door(String name, String deviceId, boolean isLocked) {
        super(name, deviceId);
        this.isLocked = isLocked;
    }

    // ── Getters & Setters ────────────────────────────────────

    /**
     * @return true if the door's bolt is currently engaged (LOCKED),
     *         false if the door can be opened (UNLOCKED).
     */
    public boolean isLocked()           { return isLocked; }

    /**
     * Directly sets the lock state without printing a message.
     * Prefer lock() / unlock() for normal interactions; this setter
     * is provided for deserialisation and test scenarios.
     */
    public void    setLocked(boolean l) { this.isLocked = l; }

    // ── Business methods ─────────────────────────────────────

    /**
     * Engages the physical bolt, preventing the door from being opened.
     * Does NOT throw an exception if the door is already locked —
     * callers (e.g. ContextualController.setSleepMode) can safely
     * call this in a loop without needing to check state first.
     */
    public void lock() {
        this.isLocked = true;
        System.out.println("[" + name + "] is now LOCKED.");
    }

    /**
     * Retracts the physical bolt, allowing the door to be opened.
     * Does NOT throw an exception if the door is already unlocked —
     * consistent with the design decision in lock().
     */
    public void unlock() {
        this.isLocked = false;
        System.out.println("[" + name + "] is now UNLOCKED.");
    }

    // ── Override showStatus ──────────────────────────────────

    /**
     * Prints a formatted one-line status summary to stdout.
     * Format: "  Door  | <name> | Status: ON/OFF | Lock: LOCKED/UNLOCKED"
     * Called by Admin.viewAllDevices() and SmartHome.showAllDevices().
     */
    @Override
    public void showStatus() {
        System.out.println("  Door         | " + name + " | Status: " + getStatusString()
                + " | Lock: " + (isLocked ? "LOCKED" : "UNLOCKED"));
    }
}
