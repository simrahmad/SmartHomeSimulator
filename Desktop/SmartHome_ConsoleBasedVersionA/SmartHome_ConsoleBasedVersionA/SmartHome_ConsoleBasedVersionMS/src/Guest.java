import exceptions.InvalidDeviceStateException;
import exceptions.UnauthorizedAccessException;
import java.util.List;

// guest user — can only see and control lights and fans, everything else is hidden
public class Guest extends User {

    private final String accessLevel = "limited"; // never changes for a guest


    // ── Constructor ──────────────────────────────────────────

    // sets role to "Guest" so the rest of the system can identify this user type
    public Guest(String name, String password) {
        super(name, password, "Guest");
    }


    // ── Getter ───────────────────────────────────────────────

    public String getAccessLevel() { return accessLevel; } // returns "limited"


    // ── Guest-only methods ───────────────────────────────────

    // shows only lights and fans — all other device types are silently skipped
    public void viewLimitedDevices(List<Device> devices) throws UnauthorizedAccessException {
        checkLoggedIn(); // throws if not authenticated
        System.out.println("\n[Guest:" + name + "] Visible devices (lights & fans only):");
        for (Device d : devices) {
            if (d instanceof Light || d instanceof Fan) d.showStatus(); // restricted view
        }
    }

    // turns a named light or fan on or off — throws if device not found or not allowed
    public void controlAllowedDevices(List<Device> devices, String deviceName, String action)
            throws UnauthorizedAccessException, InvalidDeviceStateException {

        checkLoggedIn(); // always check auth before touching any device

        for (Device d : devices) {
            if ((d instanceof Light || d instanceof Fan)
                    && d.getName().equalsIgnoreCase(deviceName)) {

                if      (action.equalsIgnoreCase("on"))  d.turnOn();  // throws if already ON
                else if (action.equalsIgnoreCase("off")) d.turnOff(); // throws if already OFF
                else    System.out.println("[Guest] Unknown action: " + action);
                return; // stop after first match
            }
        }

        // same exception whether device doesn't exist or is restricted — avoids info leaking
        throw new UnauthorizedAccessException(
                "[Guest:" + name + "] Access denied or device not found: " + deviceName
        );
    }


    // ── Override toString ────────────────────────────────────

    @Override
    public String toString() {
        return "Guest{name='" + name + "', accessLevel='" + accessLevel + "', loggedIn=" + loggedIn + "}";
    }
}