package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Handles encoding and decoding of requests for the key-value store communication protocol.
 */
public class Protocol {
    // using byte instead of int to improve the efficiency.
    public static final byte PUT = 0x01;
    public static final byte GET = 0x02;
    public static final byte DELETE = 0x03;
    public static final int MAX_KEY_LENGTH = 1024; // maximum allowed key length
    public static final int MAX_VALUE_LENGTH = 4096; // maximum allowed value length

    /**
     * Validates the request type.
     * @throws MalformedRequestException if the type is invalid.
     */
    private static void validateRequestType(byte type) throws MalformedRequestException {
        if (type != PUT && type != GET && type != DELETE) {
            throw new MalformedRequestException("Invalid request type: " + type);
        }
    }

    /**
     * Creates a request packet in byte array format.
     * This method will be called by the Client to structure its requested data
     *
     * @param type The request type (PUT, GET, DELETE).
     * @param key  The key.
     * @param value The value (nullable for GET/DELETE).
     * @return Byte array representing the request.
     */
    public static byte[] createRequest(byte type, String key, String value) {
        // transforms the key, value of String datatype to byte array by method
        // `String.getBytes(StandardCharsets.UTF_8)`
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = (value != null) ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];

        // validates the lengths of both the resulting byte arrays of key and value.
        if (keyBytes.length > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("Key length exceeds maximum limit: "
                + MAX_KEY_LENGTH);
        }
        if (valueBytes.length > MAX_VALUE_LENGTH) {
            throw new IllegalArgumentException("Value length exceeds maximum limit: "
                + MAX_VALUE_LENGTH);
        }

        // creates the ByteBuffer which can be parsed directly by the server.
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + 4 + keyBytes.length + valueBytes.length);

        // header
        buffer.put(type);
        buffer.putInt(keyBytes.length);
        buffer.putInt(valueBytes.length);

        // data
        buffer.put(keyBytes);
        buffer.put(valueBytes);

        return buffer.array();
    }

    /**
     * Converts a Request object into a byte array.
     */
    public static byte[] toByteArray(Request request) {
        return createRequest(request.getType(), request.getKey(), request.getValue());
    }

    /**
     * Parses a received request packet and returns a Request object.
     * This method will be called by the Server to parse the requests sent by the Client, and
     * execute the corresponding operations.
     *
     * @param data The raw request data in byte array format.
     * @return The parsed Request object.
     * @throws MalformedRequestException if the request is malformed.
     */
    public static Request parseRequest(byte[] data) throws MalformedRequestException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);

            // Read header
            byte type = buffer.get();
            validateRequestType(type);

            int keyLength = buffer.getInt();
            int valueLength = buffer.getInt();

            // Validate key and value lengths
            if (keyLength <= 0 || keyLength > MAX_KEY_LENGTH) {
                throw new MalformedRequestException("Invalid key length: " + keyLength);
            }
            if (valueLength < 0 || valueLength > MAX_VALUE_LENGTH) {
                throw new MalformedRequestException("Invalid value length: " + valueLength);
            }
            if (buffer.remaining() < keyLength + valueLength) {
                throw new MalformedRequestException("Incomplete request: insufficient data");
            }

            // Read key
            byte[] keyBytes = new byte[keyLength];
            buffer.get(keyBytes);
            String key = new String(keyBytes, StandardCharsets.UTF_8);

            // Read value
            String value = null;
            if (valueLength > 0) {
                byte[] valueBytes = new byte[valueLength];
                buffer.get(valueBytes);
                value = new String(valueBytes, StandardCharsets.UTF_8);
            }

            return new Request(type, key, value);
        } catch (BufferUnderflowException e) {
            throw new MalformedRequestException("Incomplete request data", e);
        }
    }

    /**
     * Writes a request to an OutputStream (Used by TCP).
     */
    public static void writeRequest(OutputStream outputStream, byte type, String key, String value) throws IOException {
        byte[] requestData = createRequest(type, key, value);
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        dataOutputStream.writeInt(requestData.length);
        dataOutputStream.write(requestData);
        dataOutputStream.flush();
    }

    /**
     * Reads a request from an InputStream (Used by TCP).
     */
    public static Request readRequest(InputStream inputStream) throws IOException, MalformedRequestException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        int requestLength = dataInputStream.readInt();
        byte[] requestData = new byte[requestLength];

        dataInputStream.readFully(requestData);

        return parseRequest(requestData);
    }
}
