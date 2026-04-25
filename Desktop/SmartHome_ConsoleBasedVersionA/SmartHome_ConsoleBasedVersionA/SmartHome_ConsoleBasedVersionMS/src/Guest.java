import exceptions.InvalidDeviceStateException;
import exceptions.UnauthorizedAccessException;
import java.util.List;

public class Guest extends User {

    private final String accessLevel = "limited";

    // ── Constructor ──────────────────────────────────────────
    public Guest(String name, String password) {
        super(name, password, "Guest");
    }

    // ── Getter ───────────────────────────────────────────────
    public String getAccessLevel() { return accessLevel; }

    // ── Guest-only methods ───────────────────────────────────

    // throws exception if guest is not logged in
    public void viewLimitedDevices(List<Device> devices) throws UnauthorizedAccessException {
        checkLoggedIn(); // inherited from User
        System.out.println("\n[Guest:" + name + "] Visible devices (lights & fans only):");
        for (Device d : devices) {
            if (d instanceof Light || d instanceof Fan) {
                d.showStatus();
            }
        }
    }

    public void controlAllowedDevices(List<Device> devices, String deviceName, String action)
            throws UnauthorizedAccessException, InvalidDeviceStateException {

        checkLoggedIn(); // inherited from User — must be logged in first

        for (Device d : devices) {
            if ((d instanceof Light || d instanceof Fan)
                    && d.getName().equalsIgnoreCase(deviceName)) {

                if (action.equalsIgnoreCase("on")) {
                    d.turnOn();  // throws InvalidDeviceStateException if already ON
                } else if (action.equalsIgnoreCase("off")) {
                    d.turnOff(); // throws InvalidDeviceStateException if already OFF
                } else {
                    System.out.println("[Guest] Unknown action: " + action);
                }
                return;
            }
        }

        // if we reach here, device was not found or not allowed
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