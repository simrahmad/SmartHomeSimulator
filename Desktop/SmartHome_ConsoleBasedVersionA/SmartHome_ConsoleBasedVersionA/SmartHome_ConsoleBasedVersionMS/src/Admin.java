// ============================================================
//  Admin.java  –  Admin user with full system access
//
//  PURPOSE:
//    Represents a privileged user who can view and manage all
//    devices, and configure the Automation engine. Admin users
//    have "full" access level, whereas Guest users only see a
//    filtered subset of devices.
//
//  DESIGN NOTES:
//    - Access level is a final field set at compile time — it
//      never changes for an Admin instance, which avoids any
//      accidental privilege downgrade.
//    - Every public method calls checkLoggedIn() (inherited from
//      User) as the first step. This enforces the invariant that
//      admin operations can only be performed by authenticated
//      users, regardless of how the method is called.
//    - configureAutomation() duplicates the null/blank validation
//      that Automation.addSchedule() also performs. This is
//      intentional: failing fast at the Admin layer gives a
//      cleaner error message that identifies the admin by name,
//      before the request even reaches the Automation engine.
// ============================================================

import exceptions.UnauthorizedAccessException;
import exceptions.AutomationException;
import java.util.List;

public class Admin extends User {

    /**
     * Access level string for this user type.
     * "full" means the Admin can view and manage ALL devices,
     * unlike Guests who are limited to lights and fans.
     * Declared final because an Admin's access level never changes.
     */
    private final String accessLevel = "full";

    // ── Constructor ──────────────────────────────────────────

    /**
     * Creates a new Admin user.
     * Passes "Admin" as the role to the parent User constructor
     * so role-based checks in the rest of the system work correctly.
     *
     * @param name     The admin's display name (e.g. "Alice").
     * @param password The password used to authenticate via login().
     */
    public Admin(String name, String password) {
        super(name, password, "Admin");
    }

    // ── Getter ───────────────────────────────────────────────

    /**
     * @return "full" — the access level granted to all Admin users.
     *         Compared against "limited" (Guest) in access-control checks.
     */
    public String getAccessLevel() { return accessLevel; }

    // ── Admin-only methods ───────────────────────────────────

    /**
     * Iterates all devices in the home and prints their status.
     * Used as the admin's "management overview" — every device,
     * regardless of type, is shown.
     *
     * Requires the admin to be logged in (checked via checkLoggedIn()).
     * If the admin is not logged in, an UnauthorizedAccessException is
     * thrown before any device information is displayed.
     *
     * @param devices The complete list of home devices.
     * @throws UnauthorizedAccessException if the admin is not logged in.
     */
    public void manageDevices(List<Device> devices) throws UnauthorizedAccessException {
        checkLoggedIn(); // inherited from User — throws if not authenticated
        System.out.println("\n[Admin:" + name + "] Managing all devices (" + devices.size() + " total):");
        // Show every device — admins have unrestricted view
        for (Device d : devices) d.showStatus();
    }

    /**
     * Prints the full device list.
     * Functionally identical to manageDevices() — kept as a separate
     * method to give callers a semantically distinct "read-only view"
     * entry point vs. a management entry point.
     *
     * Requires the admin to be logged in.
     *
     * @param devices The complete list of home devices.
     * @throws UnauthorizedAccessException if the admin is not logged in.
     */
    public void viewAllDevices(List<Device> devices) throws UnauthorizedAccessException {
        checkLoggedIn(); // inherited from User — throws if not authenticated
        System.out.println("\n[Admin:" + name + "] Full device list:");
        for (Device d : devices) d.showStatus();
    }

    /**
     * Validates and forwards an automation schedule to the Automation engine.
     *
     * Two-level validation:
     *   1. Admin layer (here) — validates with admin name in the error message
     *      for clearer audit trails.
     *   2. Automation layer (Automation.addSchedule) — validates again as a
     *      second line of defence and checks for duplicates.
     *
     * Requires the admin to be logged in (checked first, before any validation).
     *
     * @param automation The Automation engine to configure.
     * @param time       Wall-clock time string (e.g. "22:00") — must not be blank.
     * @param action     Action description (e.g. "Turn off lights") — must not be blank.
     * @throws UnauthorizedAccessException if the admin is not logged in.
     * @throws AutomationException         if time or action is null/blank, or if a
     *                                     duplicate schedule already exists.
     */
    public void configureAutomation(Automation automation, String time, String action)
            throws UnauthorizedAccessException, AutomationException {

        // Authentication check must come before any business logic
        checkLoggedIn(); // inherited from User — throws if not authenticated

        // Validate time — a blank time makes the schedule un-triggerable
        if (time == null || time.trim().isEmpty()) {
            throw new AutomationException(
                    "[Admin:" + name + "] Automation time cannot be empty!"
            );
        }

        // Validate action — a blank action would do nothing when triggered
        if (action == null || action.trim().isEmpty()) {
            throw new AutomationException(
                    "[Admin:" + name + "] Automation action cannot be empty!"
            );
        }

        System.out.println("[Admin:" + name + "] Configuring automation: " + action + " at " + time);
        // Delegate to the Automation engine — it performs duplicate checking
        automation.addSchedule(time, action);
    }

    // ── Override toString ────────────────────────────────────

    /**
     * @return A debug-friendly string showing the admin's name,
     *         access level, and current login state.
     *         Useful in logs and test output.
     */
    @Override
    public String toString() {
        return "Admin{name='" + name + "', accessLevel='" + accessLevel + "', loggedIn=" + loggedIn + "}";
    }
}
