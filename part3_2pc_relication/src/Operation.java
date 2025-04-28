import java.io.Serializable;

/**
 * Boxes the PUT and DELETE operations. It represents either a PUT operation with its corresponding
 * parameters of key and value, or a DELETE operation with its corresponding parameters of key and
 * value
 */
public class Operation implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Type {
        PUT,
        DELETE
    }
    private Type type;
    private String key;
    private String value;

    /**
     * Constructs an instance of this class.
     * @param type PUT or DELETE
     * @param key the specified key of this operation
     * @param value the specified value of this operation
     */
    public Operation(Type type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    /**
     * Gets the Type of this operation.
     * @return the Type of this operation. PUT or DELETE
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the key of this operation.
     * @return the specified key of this operation
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the value of this operation.
     * @return the specified value of this operaiton
     */
    public String getValue() {
        return value;
    }
}
