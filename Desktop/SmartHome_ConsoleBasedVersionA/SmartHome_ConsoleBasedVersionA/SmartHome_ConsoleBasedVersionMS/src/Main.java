import exceptions.*;

public class Main {

    public static void main(String[] args) {

        try {
            // ── 1. Create Smart Home ─────────────────────────
            SmartHome home = new SmartHome();
            String fileName = "smarthome.ser";
            home.loadData(fileName);

            // We only run the registration and demo if the file was empty
            if (home.getUsers().isEmpty()) {
                // ── 2. Register devices ──────────────────────────
                Light          livingRoomLight = new Light("Living Room Light", "D001");
                Light          bedroomLight    = new Light("Bedroom Light",     "D002", 80);
                Fan            ceilingFan      = new Fan("Ceiling Fan",         "D003", 3);
                AirConditioner ac              = new AirConditioner("Main AC",  "D004", 24);
                Door           frontDoor       = new Door("Front Door",         "D005");
                SecuritySystem security        = new SecuritySystem(
                        "Security Cam", "D006",
                        home.getNotificationSystem());

                home.addDevice(livingRoomLight);
                home.addDevice(bedroomLight);
                home.addDevice(ceilingFan);
                home.addDevice(ac);
                home.addDevice(frontDoor);
                home.addDevice(security);

                // ── 3. Register users ────────────────────────────
                Admin admin = new Admin("Alice", "admin123");
                Guest guest = new Guest("Bob",   "guest456");

                home.addUser(admin);
                home.addUser(guest);

                // ── 4. Demonstrate login ─────────────────────────
                System.out.println();
                try {
                    admin.login("admin123");
                    guest.login("guest456");
                } catch (AuthenticationException e) {
                    System.out.println("  X  Login failed: " + e.getMessage());
                }

                // ── 5. Demonstrate device control ────────────────
                System.out.println();
                try {
                    livingRoomLight.turnOn();
                    livingRoomLight.setBrightness(75, true);
                    ceilingFan.turnOn();
                    ceilingFan.setSpeed(4, true);
                    ac.turnOn();
                    ac.setTemperature(22, true);
                    frontDoor.lock();
                } catch (InvalidDeviceStateException e) {
                    System.out.println("  X  Device error: " + e.getMessage());
                }

                // ── 6. Admin views all devices ───────────────────
                System.out.println();
                try {
                    admin.viewAllDevices(home.getDevices());
                } catch (UnauthorizedAccessException e) {
                    System.out.println("  X  Access denied: " + e.getMessage());
                }

                // ── 7. Guest sees limited devices ─────────────────
                try {
                    guest.viewLimitedDevices(home.getDevices());
                } catch (UnauthorizedAccessException e) {
                    System.out.println("  X  Access denied: " + e.getMessage());
                }

                // ── 8. Automation schedules & rules ──────────────
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
                try {
                    home.getContextualController().applyMode("Cinema", home.getDevices());
                } catch (AutomationException | InvalidDeviceStateException e) {
                    System.out.println("  X  Mode error: " + e.getMessage());
                }

                // ── 10. Power saving demonstration ───────────────
                try {
                    home.enablePowerSavingMode();
                } catch (InvalidDeviceStateException e) {
                    System.out.println("  X  Power saving error: " + e.getMessage());
                }

                // ── 11. Security alert demonstration ─────────────
                System.out.println();
                try {
                    security.arm();
                    security.triggerAlert();
                } catch (InvalidDeviceStateException e) {
                    System.out.println("  X  Security error: " + e.getMessage());
                }

                // ── 12. Show notifications ────────────────────────
                try {
                    home.getNotificationSystem().showNotifications();
                } catch (SmartHomeException e) {
                    System.out.println("  X  Notification error: " + e.getMessage());
                }

                // ── 13. Final device snapshot ─────────────────────
                try {
                    home.showAllDevices();
                } catch (SmartHomeException e) {
                    System.out.println("  X  " + e.getMessage());
                }
            }

            // ── 14. Launch interactive console ────────────────
            System.out.println("\n==========================================");
            System.out.println("  System ready. Please log in.");
            System.out.println("  [Hint] Admin -> Alice / admin123");
            System.out.println("         Guest -> Bob   / guest456");
            System.out.println("==========================================");
            UserInputHandler handler = new UserInputHandler(home);
            handler.runLoop();

            // ── Save before exiting ─────────────────────────
            home.saveData(fileName);

        } catch (SmartHomeException e) {
            // catches any unexpected SmartHome level error
            System.out.println("\n  X  System error: " + e.getMessage());
        } catch (Exception e) {
            // last safety net — catches anything unexpected
            System.out.println("\n  X  Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}