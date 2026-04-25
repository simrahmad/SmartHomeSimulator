package exceptions;

public class InvalidDeviceStateException extends DeviceException {
    public InvalidDeviceStateException(String message) {
        super(message);
    }
}