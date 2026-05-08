import exceptions.UnauthorizedAccessException;
import exceptions.AutomationException;
import java.util.List;

// Admin user — has full access to all devices and automation settings
public class Admin extends User {

    private final String accessLevel = "full"; // admins always have full access, never changes


    // ── Constructor ──────────────────────────────────────────

    // sets role to "Admin" so the rest of the system can identify this user type
    public Admin(String name, String password) {
        super(name, password, "Admin");
    }


    // ── Getter ───────────────────────────────────────────────

    public String getAccessLevel() { return accessLevel; } // returns "full"


    // ── Admin-only methods ───────────────────────────────────

    // shows all devices with a "managing" label — admin must be logged in
    public void manageDevices(List<Device> devices) throws UnauthorizedAccessException {
        checkLoggedIn(); // throws if not authenticated
        System.out.println("\n[Admin:" + name + "] Managing all devices (" + devices.size() + " total):");
        for (Device d : devices) d.showStatus(); // admins see every device, no filtering
    }

    // same as manageDevices() but labelled as a read-only view
    public void viewAllDevices(List<Device> devices) throws UnauthorizedAccessException {
        checkLoggedIn(); // throws if not authenticated
        System.out.println("\n[Admin:" + name + "] Full device list:");
        for (Device d : devices) d.showStatus();
    }

    // validates and sends a schedule to the automation engine — admin must be logged in
    public void configureAutomation(Automation automation, String time, String action)
            throws UnauthorizedAccessException, AutomationException {

        checkLoggedIn(); // always check auth before any business logic

        if (time == null || time.trim().isEmpty()) {
            throw new AutomationException("[Admin:" + name + "] Automation time cannot be empty!");
        }

        if (action == null || action.trim().isEmpty()) {
            throw new AutomationException("[Admin:" + name + "] Automation action cannot be empty!");
        }

        System.out.println("[Admin:" + name + "] Configuring automation: " + action + " at " + time);
        automation.addSchedule(time, action); // automation engine handles duplicate checking
    }


    // ── Override toString ────────────────────────────────────

    @Override
    public String toString() {
        return "Admin{name='" + name + "', accessLevel='" + accessLevel + "', loggedIn=" + loggedIn + "}";
    }
}