package common;

import java.util.Objects;

/**
 * Represents a request in the key-value store system.
 */
public class Request {
    private final byte type;
    private final String key;
    private final String value;

    /**
     * Constructs a new Request.
     *
     * @param type  The request type (PUT, GET, DELETE).
     * @param key   The key associated with the request (cannot be null or empty).
     * @param value The value associated with the request (can be null for GET/DELETE).
     * @throws IllegalArgumentException if key is null or empty.
     */
    public Request(byte type, String key, String value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public byte getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    /**
     * Returns a string representation of the request.
     */
    @Override
    public String toString() {
        return String.format("Request{type=%d, key='%s', value='%s'}", type, key, value);
    }

    /**
     * Checks if two Request objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Request request = (Request) obj;
        return type == request.type &&
            key.equals(request.key) &&
            Objects.equals(value, request.value);
    }

    /**
     * Generates a hash code for the Request object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, key, value);
    }
}
