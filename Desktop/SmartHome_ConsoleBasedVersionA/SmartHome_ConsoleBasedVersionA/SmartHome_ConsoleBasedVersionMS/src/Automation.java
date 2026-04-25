import exceptions.AutomationException;
import exceptions.InvalidDeviceStateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Automation {

    // ── Inner class to hold a schedule entry ─────────────────
    public static class Schedule {
        String time;
        String action;
        Schedule(String time, String action) {
            this.time   = time;
            this.action = action;
        }
        @Override public String toString() {
            return "  Schedule[time=" + time + ", action=" + action + "]";
        }
    }

    private List<Schedule>      schedules;
    private Map<String, String> rules;      // condition → action

    // ── Constructor ──────────────────────────────────────────
    public Automation() {
        this.schedules = new ArrayList<>();
        this.rules     = new HashMap<>();
    }

    // ── Getters ──────────────────────────────────────────────
    public List<Schedule>      getSchedules() { return schedules; }
    public Map<String, String> getRules()     { return rules; }

    // ── Business methods ─────────────────────────────────────

    // throws exception if time or action is invalid
    public void addSchedule(String time, String action) throws AutomationException {
        if (time == null || time.trim().isEmpty()) {
            throw new AutomationException(
                    "[Automation] Schedule time cannot be empty!"
            );
        }
        if (action == null || action.trim().isEmpty()) {
            throw new AutomationException(
                    "[Automation] Schedule action cannot be empty!"
            );
        }
        // check if same schedule already exists
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

    // throws exception if condition or action is invalid
    public void addRule(String condition, String action) throws AutomationException {
        if (condition == null || condition.trim().isEmpty()) {
            throw new AutomationException(
                    "[Automation] Rule condition cannot be empty!"
            );
        }
        if (action == null || action.trim().isEmpty()) {
            throw new AutomationException(
                    "[Automation] Rule action cannot be empty!"
            );
        }
        // check if rule already exists
        if (rules.containsKey(condition)) {
            throw new AutomationException(
                    "[Automation] Rule already exists for condition: '" + condition + "'"
            );
        }
        rules.put(condition, action);
        System.out.println("[Automation] Rule added: IF '" + condition + "' THEN '" + action + "'");
    }

    // throws exception if no rules exist
    public void checkConditions() throws AutomationException {
        if (rules.isEmpty()) {
            throw new AutomationException(
                    "[Automation] No rules found to check!"
            );
        }
        System.out.println("[Automation] Checking " + rules.size() + " rule(s)...");
        for (Map.Entry<String, String> e : rules.entrySet()) {
            System.out.println("  Rule: IF '" + e.getKey() + "' → '" + e.getValue() + "'");
        }
    }

    // throws exception if no schedules exist
    public void executeSchedule() throws AutomationException {
        if (schedules.isEmpty()) {
            throw new AutomationException(
                    "[Automation] No schedules found to execute!"
            );
        }
        System.out.println("[Automation] Executing " + schedules.size() + " schedule(s):");
        for (Schedule s : schedules) System.out.println(s);
    }

    // throws exception if no lights found or already OFF
    public void autoTurnOffLights(List<Device> devices)
            throws AutomationException, InvalidDeviceStateException {
        if (devices == null || devices.isEmpty()) {
            throw new AutomationException(
                    "[Automation] No devices found!"
            );
        }
        boolean foundLight = false;
        for (Device d : devices) {
            if (d instanceof Light) {
                foundLight = true;
                ((Light) d).autoTurnOff(); // throws exception if already OFF
            }
        }
        if (!foundLight) {
            throw new AutomationException(
                    "[Automation] No lights found in device list!"
            );
        }
    }
}