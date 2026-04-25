import exceptions.UnauthorizedAccessException;
import exceptions.AutomationException;
import java.util.List;

public class Admin extends User {

    private final String accessLevel = "full";

    // ── Constructor ──────────────────────────────────────────
    public Admin(String name, String password) {
        super(name, password, "Admin");
    }

    // ── Getter ───────────────────────────────────────────────
    public String getAccessLevel() { return accessLevel; }

    // ── Admin-only methods ───────────────────────────────────

    // throws exception if admin is not logged in
    public void manageDevices(List<Device> devices) throws UnauthorizedAccessException {
        checkLoggedIn(); // inherited from User
        System.out.println("\n[Admin:" + name + "] Managing all devices (" + devices.size() + " total):");
        for (Device d : devices) d.showStatus();
    }

    // throws exception if admin is not logged in
    public void viewAllDevices(List<Device> devices) throws UnauthorizedAccessException {
        checkLoggedIn(); // inherited from User
        System.out.println("\n[Admin:" + name + "] Full device list:");
        for (Device d : devices) d.showStatus();
    }

    // throws exception if admin is not logged in or automation config is invalid
    public void configureAutomation(Automation automation, String time, String action)
            throws UnauthorizedAccessException, AutomationException {

        checkLoggedIn(); // inherited from User — must be logged in first

        // validate time format
        if (time == null || time.trim().isEmpty()) {
            throw new AutomationException(
                    "[Admin:" + name + "] Automation time cannot be empty!"
            );
        }

        // validate action
        if (action == null || action.trim().isEmpty()) {
            throw new AutomationException(
                    "[Admin:" + name + "] Automation action cannot be empty!"
            );
        }

        System.out.println("[Admin:" + name + "] Configuring automation: " + action + " at " + time);
        automation.addSchedule(time, action);
    }

    // ── Override toString ────────────────────────────────────
    @Override
    public String toString() {
        return "Admin{name='" + name + "', accessLevel='" + accessLevel + "', loggedIn=" + loggedIn + "}";
    }
}