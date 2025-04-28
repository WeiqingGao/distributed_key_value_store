package common;

public class MalformedRequestException extends Exception {
    /**
     * default consturctor.
     */
    public MalformedRequestException() {
        super("Malformed request received");
    }

    /**
     * constructor with a customize message.
     * @param message message customized by caller.
     */
    public MalformedRequestException(String message) {
        super(message);
    }

    /**
     * Constructor with a custom message and cause.
     * @param message Detailed error message.
     * @param cause The underlying cause of this exception.
     */
    public MalformedRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}