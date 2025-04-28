package util;

import java.io.Serializable;

/**
 * Represents an operation in the Key-Value Store for Paxos consensus.
 * <p>
 * Encapsulates PUT, DELETE, and NOOP operations with their associated key and value.
 * </p>
 */
public class Operation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The type of operation.
     */
    public enum Type {
        /** Insert or update a key with a value. */
        PUT,

        /** Remove a key from the store. */
        DELETE,

        /** No-op operation, used for heartbeats or no-op proposals. */
        NOOP
    }

    private final Type type;
    private final String key;
    private final String value;

    /**
     * Constructs a new Operation.
     *
     * @param type  The operation type (PUT, DELETE, or NOOP).
     * @param key   The key to operate on (empty string for NOOP).
     * @param value The value to associate (only for PUT; null or empty for DELETE/NOOP).
     */
    public Operation(Type type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the operation type.
     *
     * @return The type of this operation.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the key for this operation.
     *
     * @return The key (may be empty for NOOP).
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the value for this operation.
     *
     * @return The value (only meaningful for PUT operations).
     */
    public String getValue() {
        return value;
    }
}
