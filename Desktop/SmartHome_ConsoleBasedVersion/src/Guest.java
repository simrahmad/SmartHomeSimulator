// ============================================================
//  Guest.java  –  Guest user with restricted device access
//
//  PURPOSE:
//    Represents a limited-privilege user who can only see and
//    control Lights and Fans.  All other device types (AC,
//    SecuritySystem, Door) are hidden from Guest users.
//
//  DESIGN NOTES:
//    - Access level is a final field set at compile time —
//      "limited" never changes for a Guest instance.
//    - checkLoggedIn() (inherited from User) is called as the
//      very first step of every method, so unauthenticated
//      guests are rejected before any device information leaks.
//    - controlAllowedDevices() searches by name AND type, so a
//      guest cannot manipulate a device by passing the name of
//      an allowed device type that shares a name with a
//      restricted one (e.g. no AC named "My Light").
//    - If the named device is not found, or it is found but its
//      type is not allowed, the method throws
//      UnauthorizedAccessException — both cases are treated the
//      same to avoid revealing information about which devices
//      exist but are off-limits.
// ============================================================

import exceptions.InvalidDeviceStateException;
import exceptions.UnauthorizedAccessException;
import java.util.List;

public class Guest extends User {

    /**
     * Access level for this user type.
     * "limited" means the Guest can only interact with Lights and Fans;
     * all other device types are invisible or inaccessible.
     * Declared final because a Guest's access level never changes.
     */
    private final String accessLevel = "limited";

    // ── Constructor ──────────────────────────────────────────

    /**
     * Creates a new Guest user.
     * Passes "Guest" as the role to the parent User constructor
     * so role-based checks elsewhere in the system work correctly.
     *
     * @param name     The guest's display name (e.g. "Bob").
     * @param password The password used to authenticate via login().
     */
    public Guest(String name, String password) {
        super(name, password, "Guest");
    }

    // ── Getter ───────────────────────────────────────────────

    /**
     * @return "limited" — the access level granted to all Guest users.
     *         Compared against "full" (Admin) in access-control checks.
     */
    public String getAccessLevel() { return accessLevel; }

    // ── Guest-only methods ───────────────────────────────────

    /**
     * Prints the status of all Lights and Fans in the home.
     * Devices of any other type (AC, Door, SecuritySystem, etc.)
     * are silently skipped — guests should not know they exist.
     *
     * Requires the guest to be logged in.
     *
     * @param devices The complete list of home devices to scan.
     * @throws UnauthorizedAccessException if the guest is not logged in.
     */
    public void viewLimitedDevices(List<Device> devices) throws UnauthorizedAccessException {
        checkLoggedIn(); // inherited from User — throws if not authenticated
        System.out.println("\n[Guest:" + name + "] Visible devices (lights & fans only):");
        for (Device d : devices) {
            // instanceof check ensures only allowed device types are displayed
            if (d instanceof Light || d instanceof Fan) {
                d.showStatus();
            }
            // All other device types are silently ignored
        }
    }

    /**
     * Turns a named Light or Fan on or off.
     *
     * Lookup logic:
     *   1. Iterate the full device list.
     *   2. Accept only devices of type Light or Fan (type check comes first).
     *   3. Match by name (case-insensitive, so "ceiling fan" == "Ceiling Fan").
     *   4. Dispatch "on"/"off" to the device's turnOn()/turnOff().
     *   5. Return immediately after the first match.
     *
     * If the loop completes without finding a matching, allowed device,
     * an UnauthorizedAccessException is thrown — intentionally using the
     * same exception type whether the device doesn't exist or is off-limits,
     * to avoid leaking information about restricted devices.
     *
     * Requires the guest to be logged in (checked before any device access).
     *
     * @param devices    The complete list of home devices.
     * @param deviceName The display name of the device to control (case-insensitive).
     * @param action     "on" to power up, "off" to power down.
     * @throws UnauthorizedAccessException if not logged in, or device not found/allowed.
     * @throws InvalidDeviceStateException if the device is already in the requested state.
     */
    public void controlAllowedDevices(List<Device> devices, String deviceName, String action)
            throws UnauthorizedAccessException, InvalidDeviceStateException {

        checkLoggedIn(); // inherited from User — must authenticate before device access

        for (Device d : devices) {
            // Only process Lights and Fans — skip all other device types immediately
            if ((d instanceof Light || d instanceof Fan)
                    && d.getName().equalsIgnoreCase(deviceName)) {

                // Match found — dispatch the action
                if (action.equalsIgnoreCase("on")) {
                    d.turnOn();  // throws InvalidDeviceStateException if already ON
                } else if (action.equalsIgnoreCase("off")) {
                    d.turnOff(); // throws InvalidDeviceStateException if already OFF
                } else {
                    // Unknown action string — log but do not throw (non-fatal)
                    System.out.println("[Guest] Unknown action: " + action);
                }
                return; // stop iterating after first name match
            }
        }

        // Loop finished without a match — device not found OR it's a restricted type
        // Using UnauthorizedAccessException for both cases intentionally avoids
        // revealing information about devices that exist but are off-limits
        throw new UnauthorizedAccessException(
                "[Guest:" + name + "] Access denied or device not found: " + deviceName
        );
    }

    // ── Override toString ────────────────────────────────────

    /**
     * @return A debug-friendly string showing the guest's name,
     *         access level, and current login state.
     *         Useful in logs and test output.
     */
    @Override
    public String toString() {
        return "Guest{name='" + name + "', accessLevel='" + accessLevel + "', loggedIn=" + loggedIn + "}";
    }
}
