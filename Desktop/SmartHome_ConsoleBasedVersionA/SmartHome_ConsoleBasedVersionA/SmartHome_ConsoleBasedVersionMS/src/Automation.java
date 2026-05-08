// ============================================================
//  Automation.java  –  Schedule and rule engine for the smart home
//
//  PURPOSE:
//    Manages two types of automated behaviours:
//      1. Schedules – time-based actions (e.g. "turn off lights at 22:00")
//      2. Rules     – condition-based actions (e.g. "IF motion_detected
//                     THEN arm security system")
//    Admin users configure these via Admin.configureAutomation(), and
//    they can be executed / checked programmatically at runtime.
//
//  DESIGN NOTES:
//    - Schedule entries are stored in an ordered ArrayList so they
//      execute in the order they were added.
//    - Rules are stored in a HashMap keyed on condition string, so
//      each condition can only have one active action at a time.
//    - Duplicate detection in addSchedule() and addRule() prevents
//      accidental double-registrations, which would cause actions
//      to fire twice per trigger.
//    - autoTurnOffLights() integrates directly with the device list
//      and delegates to Light.autoTurnOff() so the "already OFF"
//      guard in Device is still honoured.
// ============================================================

import exceptions.AutomationException;
import exceptions.InvalidDeviceStateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Automation {

    // ── Inner class ──────────────────────────────────────────

    /**
     * Represents a single time-based automation entry.
     * Each Schedule binds a wall-clock time string to a human-readable
     * action description (e.g. "22:00" → "Turn off all lights").
     *
     * Stored as a static nested class so it can be used without an
     * outer Automation instance and serialises cleanly.
     */
    public static class Schedule {
        /** Wall-clock time string (e.g. "22:00", "06:30"). */
        String time;
        /** Human-readable description of the action to perform. */
        String action;

        /**
         * @param time   Trigger time string.
         * @param action Action description.
         */
        Schedule(String time, String action) {
            this.time   = time;
            this.action = action;
        }

        /** @return Readable representation used in executeSchedule() logs. */
        @Override public String toString() {
            return "  Schedule[time=" + time + ", action=" + action + "]";
        }
    }

    /**
     * Ordered list of time-based automation entries.
     * Insertion order is preserved so schedules execute in the order
     * they were registered.
     */
    private List<Schedule> schedules;

    /**
     * Condition-to-action mapping for event-driven rules.
     * Key:   condition string (e.g. "motion_detected")
     * Value: action string   (e.g. "arm security system")
     * Each condition maps to exactly one action — later calls to
     * addRule() with the same condition are rejected as duplicates.
     */
    private Map<String, String> rules;      // condition → action

    // ── Constructor ──────────────────────────────────────────

    /**
     * Creates a new Automation engine with empty schedules and rules.
     * Both collections are initialised eagerly so null checks are never
     * needed in the business methods.
     */
    public Automation() {
        this.schedules = new ArrayList<>();
        this.rules     = new HashMap<>();
    }

    // ── Getters ──────────────────────────────────────────────

    /** @return The list of all registered Schedule entries. */
    public List<Schedule>      getSchedules() { return schedules; }

    /** @return The map of all registered condition → action rules. */
    public Map<String, String> getRules()     { return rules; }

    // ── Business methods ─────────────────────────────────────

    /**
     * Registers a new time-based schedule.
     *
     * Validation steps (each throws AutomationException if violated):
     *   1. time must not be null or blank.
     *   2. action must not be null or blank.
     *   3. An identical time+action pair must not already exist.
     *
     * On success, the schedule is appended to the list and confirmed in stdout.
     *
     * @param time   Wall-clock time string (e.g. "22:00").
     * @param action Description of what should happen at that time.
     * @throws AutomationException if any validation fails.
     */
    public void addSchedule(String time, String action) throws AutomationException {
        // Validate time — a blank time makes the schedule un-triggerable
        if (time == null || time.trim().isEmpty()) {
            throw new AutomationException(
                    "[Automation] Schedule time cannot be empty!"
            );
        }
        // Validate action — a blank action is meaningless
        if (action == null || action.trim().isEmpty()) {
            throw new AutomationException(
                    "[Automation] Schedule action cannot be empty!"
            );
        }
        // Duplicate detection — identical time+action would fire the same action twice
        for (Schedule s : schedules) {
            if (s.time.equals(time) && s.action.equals(action)) {
                throw new AutomationException(
                        "[Automation] Schedule already exists: '" + action + "' at " + time
                );
            }
        }
        // All checks passed — register the schedule
        schedules.add(new Schedule(time, action));
        System.out.println("[Automation] Schedule added: '" + action + "' at " + time);
    }

    /**
     * Registers a new condition-based automation rule.
     *
     * Validation steps (each throws AutomationException if violated):
     *   1. condition must not be null or blank.
     *   2. action must not be null or blank.
     *   3. A rule for the same condition must not already exist.
     *
     * @param condition Event/sensor condition string (e.g. "motion_detected").
     * @param action    Action to take when the condition is met.
     * @throws AutomationException if any validation fails.
     */
    public void addRule(String condition, String action) throws AutomationException {
        // Validate condition — blank conditions could never be matched
        if (condition == null || condition.trim().isEmpty()) {
            throw new AutomationException(
                    "[Automation] Rule condition cannot be empty!"
            );
        }
        // Validate action — blank actions produce no effect
        if (action == null || action.trim().isEmpty()) {
            throw new AutomationException(
                    "[Automation] Rule action cannot be empty!"
            );
        }
        // Duplicate detection — each condition should have exactly one action
        if (rules.containsKey(condition)) {
            throw new AutomationException(
                    "[Automation] Rule already exists for condition: '" + condition + "'"
            );
        }
        // All checks passed — store the rule
        rules.put(condition, action);
        System.out.println("[Automation] Rule added: IF '" + condition + "' THEN '" + action + "'");
    }

    /**
     * Iterates and prints all registered rules.
     * Throws if no rules are present — calling this on an empty rule
     * set indicates a configuration error in the calling code.
     *
     * @throws AutomationException if no rules have been registered.
     */
    public void checkConditions() throws AutomationException {
        // Throw early — no rules means there is nothing to check
        if (rules.isEmpty()) {
            throw new AutomationException(
                    "[Automation] No rules found to check!"
            );
        }
        System.out.println("[Automation] Checking " + rules.size() + " rule(s)...");
        // Print each condition → action pair for transparency
        for (Map.Entry<String, String> e : rules.entrySet()) {
            System.out.println("  Rule: IF '" + e.getKey() + "' → '" + e.getValue() + "'");
        }
    }

    /**
     * Iterates and prints all registered schedules.
     * Throws if no schedules are present — consistent behaviour with
     * checkConditions() so callers always get an exception on empty state.
     *
     * @throws AutomationException if no schedules have been registered.
     */
    public void executeSchedule() throws AutomationException {
        // Throw early — nothing to execute if list is empty
        if (schedules.isEmpty()) {
            throw new AutomationException(
                    "[Automation] No schedules found to execute!"
            );
        }
        System.out.println("[Automation] Executing " + schedules.size() + " schedule(s):");
        // Print each schedule — Schedule.toString() provides the formatted output
        for (Schedule s : schedules) System.out.println(s);
    }

    /**
     * Finds all Light devices in the provided list and calls autoTurnOff()
     * on each one. This is the bridge between the Automation engine and the
     * actual device layer.
     *
     * Throws in the following cases:
     *   - devices list is null or empty (configuration error)
     *   - no Light devices exist in the list (misconfiguration)
     *   - a Light is already OFF when autoTurnOff() is called
     *     (Light throws InvalidDeviceStateException, which propagates up)
     *
     * @param devices The full list of home devices to scan.
     * @throws AutomationException         if the list is empty or no lights are found.
     * @throws InvalidDeviceStateException if a light is already OFF.
     */
    public void autoTurnOffLights(List<Device> devices)
            throws AutomationException, InvalidDeviceStateException {
        // Guard: can't operate on a null or empty device list
        if (devices == null || devices.isEmpty()) {
            throw new AutomationException(
                    "[Automation] No devices found!"
            );
        }
        boolean foundLight = false;  // track whether at least one Light was processed
        for (Device d : devices) {
            if (d instanceof Light) {
                foundLight = true;
                // Delegate to Light's own autoTurnOff() — keeps off-state guard in one place
                ((Light) d).autoTurnOff();
            }
        }
        // If no Light was found, the caller probably passed the wrong device list
        if (!foundLight) {
            throw new AutomationException(
                    "[Automation] No lights found in device list!"
            );
        }
    }
}
