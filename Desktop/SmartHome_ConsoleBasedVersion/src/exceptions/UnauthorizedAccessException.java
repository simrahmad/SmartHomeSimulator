package exceptions;

public class UnauthorizedAccessException extends SmartHomeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}