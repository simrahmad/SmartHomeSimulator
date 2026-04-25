// ============================================================
//  Door.java  –  Extends Device
// ============================================================
public class Door extends Device {

    private boolean isLocked;

    // ── Constructors (overloading) ───────────────────────────
    public Door(String name, String deviceId) {
        super(name, deviceId);
        this.isLocked = true;   // locked by default
    }

    public Door(String name, String deviceId, boolean isLocked) {
        super(name, deviceId);
        this.isLocked = isLocked;
    }

    // ── Getters & Setters ────────────────────────────────────
    public boolean isLocked()           { return isLocked; }
    public void    setLocked(boolean l) { this.isLocked = l; }

    // ── Business methods ─────────────────────────────────────
    public void lock() {
        this.isLocked = true;
        System.out.println("[" + name + "] is now LOCKED.");
    }

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
