import exceptions.*;

public class Main {

    public static void main(String[] args) {

        try {
            // create the central smart home container and load any previously saved state
            SmartHome home = new SmartHome();
            String fileName = "smarthome.ser";
            home.loadData(fileName);

            // first launch only — skip this entire block if data was loaded from disk
            if (home.getUsers().isEmpty()) {

                // ── Register devices ─────────────────────────────

                Light          livingRoomLight = new Light("Living Room Light", "D001");
                Light          bedroomLight    = new Light("Bedroom Light",     "D002", 80); // starts at 80% brightness
                Fan            ceilingFan      = new Fan("Ceiling Fan",         "D003", 3);  // starts at speed 3
                AirConditioner ac              = new AirConditioner("Main AC",  "D004", 24); // 24°C default
                Door           frontDoor       = new Door("Front Door",         "D005");     // locked by default
                SecuritySystem security        = new SecuritySystem(
                        "Security Cam", "D006",
                        home.getNotificationSystem()); // wire up notifications so alerts get logged

                home.addDevice(livingRoomLight);
                home.addDevice(bedroomLight);
                home.addDevice(ceilingFan);
                home.addDevice(ac);
                home.addDevice(frontDoor);
                home.addDevice(security);

                // ── Register users ───────────────────────────────

                Admin admin = new Admin("Alice", "admin123");
                Guest guest = new Guest("Bob",   "guest456");

                home.addUser(admin);
                home.addUser(guest);

                // ── Demo: login ──────────────────────────────────

                System.out.println();
                try {
                    admin.login("admin123");
                    guest.login("guest456");
                } catch (AuthenticationException e) {
                    System.out.println("  X  Login failed: " + e.getMessage());
                }

                // ── Demo: device control ─────────────────────────

                System.out.println();
                try {
                    livingRoomLight.turnOn();
                    livingRoomLight.setBrightness(75, true); // true = print confirmation
                    ceilingFan.turnOn();
                    ceilingFan.setSpeed(4, true);
                    ac.turnOn();
                    ac.setTemperature(22, true);
                    frontDoor.lock(); // lock() never throws — safe to call without checking state
                } catch (InvalidDeviceStateException e) {
                    System.out.println("  X  Device error: " + e.getMessage());
                }

                // ── Demo: admin sees everything ──────────────────

                System.out.println();
                try {
                    admin.viewAllDevices(home.getDevices());
                } catch (UnauthorizedAccessException e) {
                    System.out.println("  X  Access denied: " + e.getMessage());
                }

                // ── Demo: guest sees only lights and fans ────────

                try {
                    guest.viewLimitedDevices(home.getDevices());
                } catch (UnauthorizedAccessException e) {
                    System.out.println("  X  Access denied: " + e.getMessage());
                }

                // ── Demo: automation schedules and rules ─────────

                System.out.println();
                try {
                    home.getAutomation().addSchedule("22:00", "Turn off all lights");
                    home.getAutomation().addSchedule("06:00", "Turn on Living Room Light");
                    home.getAutomation().addRule("motion_detected", "arm security system");
                    home.getAutomation().addRule("high_energy", "enable power saving");
                } catch (AutomationException e) {
                    System.out.println("  X  Automation error: " + e.getMessage());
                }

                // ── Demo: cinema mode — lights off, AC 22°C, fan speed 2 ──

                try {
                    home.getContextualController().applyMode("Cinema", home.getDevices());
                } catch (AutomationException | InvalidDeviceStateException e) {
                    System.out.println("  X  Mode error: " + e.getMessage());
                }

                // ── Demo: power saving ───────────────────────────

                try {
                    home.enablePowerSavingMode();
                } catch (InvalidDeviceStateException e) {
                    System.out.println("  X  Power saving error: " + e.getMessage());
                }

                // ── Demo: arm security and trigger an alert ──────

                System.out.println();
                try {
                    security.arm();          // must be armed before triggerAlert() will work
                    security.triggerAlert(); // fires motionDetected() on the notification system
                } catch (InvalidDeviceStateException e) {
                    System.out.println("  X  Security error: " + e.getMessage());
                }

                // ── Demo: show all notifications collected so far ─

                try {
                    home.getNotificationSystem().showNotifications();
                } catch (SmartHomeException e) {
                    System.out.println("  X  Notification error: " + e.getMessage());
                }

                // ── Demo: final snapshot of all device states ────

                try {
                    home.showAllDevices();
                } catch (SmartHomeException e) {
                    System.out.println("  X  " + e.getMessage());
                }
            }

            // ── Launch interactive console ────────────────────
            // runs whether the demo above was skipped or not
            System.out.println("\n==========================================");
            System.out.println("  System ready. Please log in.");
            System.out.println("  [Hint] Admin -> Alice / admin123");
            System.out.println("         Guest -> Bob   / guest456");
            System.out.println("==========================================");
            UserInputHandler handler = new UserInputHandler(home);
            handler.runLoop(); // blocks here until the user types "exit"

            // ── Save before exiting ───────────────────────────
            home.saveData(fileName); // persists devices, users, and notifications for next launch

        } catch (SmartHomeException e) {
            System.out.println("\n  X  System error: " + e.getMessage());
        } catch (Exception e) {
            // safety net for any unexpected runtime exception
            System.out.println("\n  X  Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}