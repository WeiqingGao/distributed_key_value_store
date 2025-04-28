import java.io.Serializable;

/**
 * An exception indicating that the request (or the parameters of the request) is malformed.
 */
public class MalformedRequestException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public MalformedRequestException() {
        super("Malformed request received");
    }

    /**
     * Constructor with a custom message.
     * @param message Message customized by caller.
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
