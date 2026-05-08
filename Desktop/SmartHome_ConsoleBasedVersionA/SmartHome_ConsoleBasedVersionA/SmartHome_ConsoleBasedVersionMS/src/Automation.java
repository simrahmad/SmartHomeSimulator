import exceptions.AutomationException;
import exceptions.InvalidDeviceStateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// handles all time-based schedules and condition-based rules for the smart home
public class Automation {

    // ── Inner class ──────────────────────────────────────────

    // a single schedule entry — binds a time to an action e.g. "22:00 → turn off lights"
    public static class Schedule {
        String time;   // wall-clock time e.g. "22:00"
        String action; // what should happen at that time

        Schedule(String time, String action) {
            this.time   = time;
            this.action = action;
        }

        @Override public String toString() {
            return "  Schedule[time=" + time + ", action=" + action + "]";
        }
    }

    private List<Schedule>      schedules; // ordered list of time-based entries
    private Map<String, String> rules;     // condition → action pairs e.g. "motion_detected → arm security"


    // ── Constructor ──────────────────────────────────────────

    // starts with empty schedules and rules — nothing runs until something is added
    public Automation() {
        this.schedules = new ArrayList<>();
        this.rules     = new HashMap<>();
    }


    // ── Getters ──────────────────────────────────────────────

    public List<Schedule>      getSchedules() { return schedules; }
    public Map<String, String> getRules()     { return rules; }


    // ── Business methods ─────────────────────────────────────

    // adds a time-based schedule — rejects blanks and duplicates
    public void addSchedule(String time, String action) throws AutomationException {
        if (time == null || time.trim().isEmpty()) {
            throw new AutomationException("[Automation] Schedule time cannot be empty!");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new AutomationException("[Automation] Schedule action cannot be empty!");
        }
        // same time + action already exists — would fire twice otherwise
        for (Schedule s : schedules) {
            if (s.time.equals(time) && s.action.equals(action)) {
                throw new AutomationException(
                        "[Automation] Schedule already exists: '" + action + "' at " + time
                );
            }
        }
        schedules.add(new Schedule(time, action));
        System.out.println("[Automation] Schedule added: '" + action + "' at " + time);
    }

    // adds a condition-based rule — each condition can only have one action
    public void addRule(String condition, String action) throws AutomationException {
        if (condition == null || condition.trim().isEmpty()) {
            throw new AutomationException("[Automation] Rule condition cannot be empty!");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new AutomationException("[Automation] Rule action cannot be empty!");
        }
        if (rules.containsKey(condition)) {
            throw new AutomationException(
                    "[Automation] Rule already exists for condition: '" + condition + "'"
            );
        }
        rules.put(condition, action);
        System.out.println("[Automation] Rule added: IF '" + condition + "' THEN '" + action + "'");
    }

    // prints all registered rules — throws if none exist
    public void checkConditions() throws AutomationException {
        if (rules.isEmpty()) {
            throw new AutomationException("[Automation] No rules found to check!");
        }
        System.out.println("[Automation] Checking " + rules.size() + " rule(s)...");
        for (Map.Entry<String, String> e : rules.entrySet()) {
            System.out.println("  Rule: IF '" + e.getKey() + "' → '" + e.getValue() + "'");
        }
    }

    // prints all registered schedules — throws if none exist
    public void executeSchedule() throws AutomationException {
        if (schedules.isEmpty()) {
            throw new AutomationException("[Automation] No schedules found to execute!");
        }
        System.out.println("[Automation] Executing " + schedules.size() + " schedule(s):");
        for (Schedule s : schedules) System.out.println(s);
    }

    // finds all lights in the device list and turns them off automatically
    public void autoTurnOffLights(List<Device> devices)
            throws AutomationException, InvalidDeviceStateException {
        if (devices == null || devices.isEmpty()) {
            throw new AutomationException("[Automation] No devices found!");
        }
        boolean foundLight = false;
        for (Device d : devices) {
            if (d instanceof Light) {
                foundLight = true;
                ((Light) d).autoTurnOff(); // Light handles its own already-OFF guard
            }
        }
        if (!foundLight) {
            throw new AutomationException("[Automation] No lights found in device list!");
        }
    }
}