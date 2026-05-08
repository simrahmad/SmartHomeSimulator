// smart door — controls the physical lock independently of the device power state
public class Door extends Device {

    private boolean isLocked; // true = LOCKED, false = UNLOCKED


    // ── Constructors ─────────────────────────────────────────

    // locked by default — safety first
    public Door(String name, String deviceId) {
        super(name, deviceId);
        this.isLocked = true;
    }

    // custom lock state — useful when restoring from saved data
    public Door(String name, String deviceId, boolean isLocked) {
        super(name, deviceId);
        this.isLocked = isLocked;
    }


    // ── Getters & Setters ────────────────────────────────────

    public boolean isLocked()           { return isLocked; }
    public void    setLocked(boolean l) { this.isLocked = l; } // for deserialization and tests only


    // ── Business methods ─────────────────────────────────────

    // locks the door — silently does nothing if already locked so loops never break
    public void lock() {
        this.isLocked = true;
        System.out.println("[" + name + "] is now LOCKED.");
    }

    // unlocks the door — no exception if already unlocked, consistent with lock()
    public void unlock() {
        this.isLocked = false;
        System.out.println("[" + name + "] is now UNLOCKED.");
    }


    // ── Override ─────────────────────────────────────────────

    @Override
    public void showStatus() {
        System.out.println("  Door         | " + name + " | Status: " + getStatusString()
                + " | Lock: " + (isLocked ? "LOCKED" : "UNLOCKED"));
    }
}