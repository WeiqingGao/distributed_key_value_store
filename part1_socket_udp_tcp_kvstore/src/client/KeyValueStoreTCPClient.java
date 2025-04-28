package client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.*;
import common.Protocol;

/**
 * TCP Client for the Key-Value Store.
 */
public class KeyValueStoreTCPClient {
    private static final Logger logger = Logger.getLogger(KeyValueStoreTCPClient.class.getName());
    private final String host;
    private final int port;

    public KeyValueStoreTCPClient(String host, int port) {
        this.host = host;
        this.port = port;
        setupLogger();
    }

    /**
     * Configures logging with millisecond precision timestamps.
     */
    private void setupLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT.%1$tL] %4$s: %5$s%n");
    }

    /**
     * Sends a request to the TCP server.
     * @param type  Request type (PUT, GET, DELETE).
     * @param key   The key to operate on.
     * @param value The value for PUT requests (null for GET/DELETE).
     * @return Server response or an error message.
     */
    private String sendRequest(byte type, String key, String value) {
        String requestType = getRequestTypeName(type);

        if (key == null || key.trim().isEmpty()) {
            logger.warning(requestType + " failed: Key cannot be empty.");
            return "ERROR: Key cannot be empty.";
        }

        long startTime = System.currentTimeMillis();

        try (Socket socket = new Socket(host, port);
             OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream()) {

            // Send request
            Protocol.writeRequest(outputStream, type, key, value);
            logger.info(String.format("Sent %s request to %s:%d for key: %s", requestType, host, port, key));

            // Read response
            byte[] responseBuffer = new byte[1024];
            int bytesRead = inputStream.read(responseBuffer);

            if (bytesRead == -1) {
                logger.warning("Server closed the connection unexpectedly.");
                return "ERROR: Server closed the connection.";
            }

            long endTime = System.currentTimeMillis();
            String response = new String(responseBuffer, 0, bytesRead, StandardCharsets.UTF_8);

            logger.info(String.format("Received response in %d ms: %s", (endTime - startTime), response));
            return response;

        } catch (IOException e) {
            logger.severe("Error communicating with server: " + e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Returns a human-readable request type name.
     */
    private String getRequestTypeName(byte type) {
        switch (type) {
            case Protocol.PUT: return "PUT";
            case Protocol.GET: return "GET";
            case Protocol.DELETE: return "DELETE";
            default: return "UNKNOWN";
        }
    }

    public String put(String key, String value) {
        return sendRequest(Protocol.PUT, key, value);
    }

    public String get(String key) {
        return sendRequest(Protocol.GET, key, null);
    }

    public String delete(String key) {
        return sendRequest(Protocol.DELETE, key, null);
    }

    /**
     * Prints a message to the console with a timestamp.
     * @param message The message to print.
     */
    private static void printWithTimestamp(String message) {
        String timestamp = String.format("[%1$tF %1$tT.%1$tL] ", System.currentTimeMillis());
        System.out.println(timestamp + message);
    }

    /**
     * Prints a message to the console with a timestamp, without a newline.
     * Used for interactive user input prompts.
     * @param message The message to print.
     */
    private static void printWithoutNewlineWithTimestamp(String message) {
        String timestamp = String.format("[%1$tF %1$tT.%1$tL] ", System.currentTimeMillis());
        System.out.print(timestamp + message);
    }

    /**
     * Main method for running the TCP client interactively.
     * @param args Command line arguments (expects <host> <port>).
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            printWithTimestamp("Usage: java KeyValueStoreTCPClient <host> <port>");
            return;
        }

        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            KeyValueStoreTCPClient client = new KeyValueStoreTCPClient(host, port);

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    printWithoutNewlineWithTimestamp("Enter command (PUT/GET/DELETE) or 'exit': ");
                    String command = scanner.nextLine().trim().toUpperCase();

                    if ("EXIT".equals(command)) {
                        printWithTimestamp("Exiting client...");
                        break;
                    }

                    // Validate command before asking for a key
                    if (!command.equals("PUT") && !command.equals("GET") && !command.equals("DELETE")) {
                        printWithTimestamp("Invalid command. Use PUT, GET, or DELETE.");
                        continue;
                    }

                    printWithoutNewlineWithTimestamp("Enter key: ");
                    String key = scanner.nextLine().trim();

                    if (key.isEmpty()) {
                        printWithTimestamp("Error: Key cannot be empty.");
                        continue;
                    }

                    String value = null;
                    if ("PUT".equals(command)) {
                        printWithoutNewlineWithTimestamp("Enter value: ");
                        value = scanner.nextLine().trim();

                        if (value.isEmpty()) {
                            printWithTimestamp("Error: Value cannot be empty for PUT operation.");
                            continue;
                        }
                    }

                    String result;
                    switch (command) {
                        case "PUT":
                            result = client.put(key, value);
                            break;
                        case "GET":
                            result = client.get(key);
                            break;
                        case "DELETE":
                            result = client.delete(key);
                            break;
                        default:
                            result = "Unexpected error";
                    }

                    printWithTimestamp("Response: " + result);
                }
            }

        } catch (NumberFormatException e) {
            printWithTimestamp("Error: Port must be a valid number.");
        }
    }
}
