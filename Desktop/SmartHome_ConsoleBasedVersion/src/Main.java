// ============================================================
//  Main.java  –  Application entry point
//
//  PURPOSE:
//    Bootstraps the entire smart home system:
//      1. Creates (or reloads) a SmartHome instance from disk.
//      2. If no persisted data exists, runs a full demo that
//         registers devices, registers users, demonstrates every
//         major feature, and prints status snapshots.
//      3. Regardless of whether data was loaded or created fresh,
//         launches the interactive console (UserInputHandler).
//      4. Persists the home state to disk before exiting.
//
//  DESIGN NOTES:
//    - The outer try/catch uses a broad SmartHomeException catch
//      as the last resort, so any unhandled SmartHome-level
//      problem surfaces with a clean message rather than a raw
//      stack trace.  A second catch(Exception) below that is the
//      absolute safety net.
//    - The demo section is guarded by home.getUsers().isEmpty()
//      so it only runs on the first launch; subsequent runs skip
//      straight to the interactive console with the saved state.
//    - Each demo block has its own try/catch so a single failure
//      (e.g. a device already in the desired state) does not abort
//      the entire demo — the program continues to the next section.
// ============================================================

import exceptions.*;

public class Main {

    public static void main(String[] args) {

        try {
            // ── 1. Create Smart Home ─────────────────────────
            // SmartHome is the central container for devices, users,
            // automation schedules, notifications, and contextual modes.
            SmartHome home = new SmartHome();
            String fileName = "smarthome.ser";  // serialisation file name
            home.loadData(fileName);            // load previous state if it exists

            // Only run registration and the full demo on the very first launch.
            // If users were loaded from disk, we skip straight to the console.
            if (home.getUsers().isEmpty()) {

                // ── 2. Register devices ──────────────────────────
                // Create one of each supported device type and add them to
                // the home's device list.  Device IDs are unique identifiers
                // used for lookup and persistence.
                Light          livingRoomLight = new Light("Living Room Light", "D001");
                Light          bedroomLight    = new Light("Bedroom Light",     "D002", 80); // starts at 80% brightness
                Fan            ceilingFan      = new Fan("Ceiling Fan",         "D003", 3); // starts at speed 3
                AirConditioner ac              = new AirConditioner("Main AC",  "D004", 24); // 24°C default
                Door           frontDoor       = new Door("Front Door",         "D005");    // locked by default
                SecuritySystem security        = new SecuritySystem(
                        "Security Cam", "D006",
                        home.getNotificationSystem()); // wire up the notification system

                // Register all devices with the SmartHome container
                home.addDevice(livingRoomLight);
                home.addDevice(bedroomLight);
                home.addDevice(ceilingFan);
                home.addDevice(ac);
                home.addDevice(frontDoor);
                home.addDevice(security);

                // ── 3. Register users ────────────────────────────
                // Create one Admin (full access) and one Guest (limited access)
                Admin admin = new Admin("Alice", "admin123");
                Guest guest = new Guest("Bob",   "guest456");

                home.addUser(admin);
                home.addUser(guest);

                // ── 4. Demonstrate login ─────────────────────────
                // Both users log in with correct credentials.
                // An AuthenticationException would fire here for wrong passwords.
                System.out.println();
                try {
                    admin.login("admin123");
                    guest.login("guest456");
                } catch (AuthenticationException e) {
                    System.out.println("  X  Login failed: " + e.getMessage());
                }

                // ── 5. Demonstrate device control ────────────────
                // Turn on devices and adjust their settings.
                // InvalidDeviceStateException fires if a device is already in the
                // requested state or if a value is out of range.
                System.out.println();
                try {
                    livingRoomLight.turnOn();
                    livingRoomLight.setBrightness(75, true); // verbose = print confirmation
                    ceilingFan.turnOn();
                    ceilingFan.setSpeed(4, true);
                    ac.turnOn();
                    ac.setTemperature(22, true);
                    frontDoor.lock();  // Door.lock() never throws — safe to call unconditionally
                } catch (InvalidDeviceStateException e) {
                    System.out.println("  X  Device error: " + e.getMessage());
                }

                // ── 6. Admin views all devices ───────────────────
                // Admin.viewAllDevices() requires the admin to be logged in;
                // it prints every device regardless of type.
                System.out.println();
                try {
                    admin.viewAllDevices(home.getDevices());
                } catch (UnauthorizedAccessException e) {
                    System.out.println("  X  Access denied: " + e.getMessage());
                }

                // ── 7. Guest sees limited devices ─────────────────
                // Guest.viewLimitedDevices() shows only Lights and Fans —
                // all other device types are silently filtered out.
                try {
                    guest.viewLimitedDevices(home.getDevices());
                } catch (UnauthorizedAccessException e) {
                    System.out.println("  X  Access denied: " + e.getMessage());
                }

                // ── 8. Automation schedules & rules ──────────────
                // Register two time-based schedules and two event-based rules.
                // AutomationException fires for duplicates or blank values.
                System.out.println();
                try {
                    home.getAutomation().addSchedule("22:00", "Turn off all lights");
                    home.getAutomation().addSchedule("06:00", "Turn on Living Room Light");
                    home.getAutomation().addRule("motion_detected", "arm security system");
                    home.getAutomation().addRule("high_energy", "enable power saving");
                } catch (AutomationException e) {
                    System.out.println("  X  Automation error: " + e.getMessage());
                }

                // ── 9. Apply a contextual mode ───────────────────
                // Cinema mode: lights off, AC to 22°C, fan to speed 2.
                // Per-device exceptions are handled inside applyMode() and
                // only truly unknown modes / empty input propagate here.
                try {
                    home.getContextualController().applyMode("Cinema", home.getDevices());
                } catch (AutomationException | InvalidDeviceStateException e) {
                    System.out.println("  X  Mode error: " + e.getMessage());
                }

                // ── 10. Power saving demonstration ───────────────
                // enablePowerSavingMode() turns off all non-essential devices
                // and logs an energy-usage alert via the NotificationSystem.
                try {
                    home.enablePowerSavingMode();
                } catch (InvalidDeviceStateException e) {
                    System.out.println("  X  Power saving error: " + e.getMessage());
                }

                // ── 11. Security alert demonstration ─────────────
                // Arm the security system and immediately trigger an alert
                // to demonstrate the arm → triggerAlert → notification flow.
                System.out.println();
                try {
                    security.arm();           // must arm before triggerAlert() works
                    security.triggerAlert();  // fires motionDetected() on NotificationSystem
                } catch (InvalidDeviceStateException e) {
                    System.out.println("  X  Security error: " + e.getMessage());
                }

                // ── 12. Show notifications ────────────────────────
                // Print the full notification log accumulated during the demo.
                // SmartHomeException is thrown if the log is empty.
                try {
                    home.getNotificationSystem().showNotifications();
                } catch (SmartHomeException e) {
                    System.out.println("  X  Notification error: " + e.getMessage());
                }

                // ── 13. Final device snapshot ─────────────────────
                // Print the current state of all devices after the full demo.
                try {
                    home.showAllDevices();
                } catch (SmartHomeException e) {
                    System.out.println("  X  " + e.getMessage());
                }
            }

            // ── 14. Launch interactive console ────────────────
            // Present a login prompt and command loop so the user can
            // interact with the home in real time.  This runs regardless
            // of whether the demo above was skipped (data loaded) or not.
            System.out.println("\n==========================================");
            System.out.println("  System ready. Please log in.");
            System.out.println("  [Hint] Admin -> Alice / admin123");
            System.out.println("         Guest -> Bob   / guest456");
            System.out.println("==========================================");
            UserInputHandler handler = new UserInputHandler(home);
            handler.runLoop(); // blocks until the user types "exit"

            // ── Save before exiting ─────────────────────────
            // Serialise the full SmartHome state (devices, users, schedules,
            // notifications) so the next launch can resume from where we left off.
            home.saveData(fileName);

        } catch (SmartHomeException e) {
            // Catches any unhandled SmartHome-level exception that escaped the demo blocks
            System.out.println("\n  X  System error: " + e.getMessage());
        } catch (Exception e) {
            // Absolute safety net — catches any remaining unexpected runtime exception
            System.out.println("\n  X  Unexpected error: " + e.getMessage());
            e.printStackTrace(); // print the full stack trace for debugging
        }
    }
}
