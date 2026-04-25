import exceptions.DeviceNotFoundException;
import exceptions.InvalidDeviceStateException;
import exceptions.SmartHomeException;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class SmartHome {

    private List<Device>         devices;
    private Automation           automation;
    private ContextualController contextualController;
    private List<User>           users;
    private NotificationSystem   notificationSystem;

    // ── Constructor ──────────────────────────────────────────
    public SmartHome() {
        this.devices              = new ArrayList<>();
        this.users                = new ArrayList<>();
        this.notificationSystem   = new NotificationSystem();
        this.automation           = new Automation();
        this.contextualController = new ContextualController();
    }

    // ── Getters ──────────────────────────────────────────────
    public List<Device>         getDevices()              { return devices; }
    public List<User>           getUsers()                { return users; }
    public Automation           getAutomation()           { return automation; }
    public ContextualController getContextualController() { return contextualController; }
    public NotificationSystem   getNotificationSystem()   { return notificationSystem; }

    // ── Device management ────────────────────────────────────

    // throws exception if device with same ID already exists
    public void addDevice(Device d) throws SmartHomeException {
        for (Device existing : devices) {
            if (existing.getDeviceId().equals(d.getDeviceId())) {
                throw new SmartHomeException(
                        "[SmartHome] Device with ID " + d.getDeviceId() + " already exists!"
                );
            }
        }
        devices.add(d);
        System.out.println("[SmartHome] Device added: " + d.getName() + " (ID: " + d.getDeviceId() + ")");
    }

    // throws exception if device not found
    public void removeDevice(Device d) throws DeviceNotFoundException {
        if (!devices.remove(d)) {
            throw new DeviceNotFoundException(
                    "[SmartHome] Device not found: " + d.getName()
            );
        }
        System.out.println("[SmartHome] Device removed: " + d.getName());
    }

    // throws exception if no devices registered
    public void showAllDevices() throws SmartHomeException {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("  SMART HOME – Device Status (" + devices.size() + " devices)");
        System.out.println("══════════════════════════════════════════");
        if (devices.isEmpty()) {
            throw new SmartHomeException(
                    "[SmartHome] No devices registered!"
            );
        }
        for (Device d : devices) d.showStatus();
        System.out.println("══════════════════════════════════════════\n");
    }

    // throws exception if device not found
    public void controlDevice(String name) throws DeviceNotFoundException, InvalidDeviceStateException {
        for (Device d : devices) {
            if (d.getName().equalsIgnoreCase(name)) {
                System.out.println("[SmartHome] Toggling: " + name);
                if (d.getStatus()) d.turnOff(); else d.turnOn();
                return;
            }
        }
        throw new DeviceNotFoundException(
                "[SmartHome] Device not found: " + name
        );
    }

    // ── Power saving ─────────────────────────────────────────
    // FIXED CODE
    public void enablePowerSavingMode() throws InvalidDeviceStateException, SmartHomeException {
        System.out.println("\n[SmartHome] ⚡ Power Saving Mode ENABLED");
        for (Device d : devices) {
            if (d instanceof Light) {
                ((Light) d).setBrightness(20, true);
            }
            if (d instanceof AirConditioner) {
                ((AirConditioner) d).setTemperature(26, true);
            }
            if (d instanceof Fan) {
                ((Fan) d).setSpeed(1, true);
            }
        }
        notificationSystem.energyUsageHigh();  // now properly declared
    }
    // ── User management ──────────────────────────────────────

    // throws exception if user with same name already exists
    public void addUser(User u) throws SmartHomeException {
        for (User existing : users) {
            if (existing.getName().equalsIgnoreCase(u.getName())) {
                throw new SmartHomeException(
                        "[SmartHome] User " + u.getName() + " already exists!"
                );
            }
        }
        users.add(u);
        System.out.println("[SmartHome] User registered: " + u.getName() + " [" + u.getRole() + "]");
    }


    // ── File I/O (Save & Load) ───────────────────────────────

    public void loadData(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("[System] No previous data found. Starting fresh.");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // Loading in the exact same order they are saved preserves shared references
            this.devices = (List<Device>) ois.readObject();
            this.notificationSystem = (NotificationSystem) ois.readObject();
            this.users = (List<User>) ois.readObject();
            System.out.println("[System] Data loaded successfully");
        } catch (Exception e) {
            System.out.println("  X  Error loading data: " + e.getMessage());
        }
    }

    public void saveData(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this.devices);
            oos.writeObject(this.notificationSystem);
            oos.writeObject(this.users);
            System.out.println("\n[System] Data saved successfully");
        } catch (IOException e) {
            System.out.println("\n  X  Error saving data: " + e.getMessage());
        }
    }
}