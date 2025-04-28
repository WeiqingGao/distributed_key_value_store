package client;

import java.net.*;
import java.util.Scanner;
import java.util.logging.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import common.Protocol;

/**
 * UDP Client for the Key-Value Store.
 * Sends PUT, GET, and DELETE requests to the UDP server.
 */
public class KeyValueStoreUDPClient {
    private static final Logger logger = Logger.getLogger(KeyValueStoreUDPClient.class.getName());
    private final String host;
    private final int port;
    private static final int TIMEOUT = 5000; // 5 seconds timeout

    /**
     * Constructs a KeyValueStoreUDPClient instance.
     * @param host Server hostname or IP.
     * @param port Server port.
     */
    public KeyValueStoreUDPClient(String host, int port) {
        this.host = host;
        this.port = port;
        setupLogger();
    }

    /**
     * Sets up the logging format including timestamps.
     */
    private void setupLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT.%1$tL] %4$s: %5$s%n");
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
     * Prints a message to the console with a timestamp, without a new line.
     * Used for interactive user input prompts.
     * @param message The message to print.
     */
    private static void printWithoutNewlineWithTimestamp(String message) {
        String timestamp = String.format("[%1$tF %1$tT.%1$tL] ", System.currentTimeMillis());
        System.out.print(timestamp + message);
    }

    /**
     * Sends a request to the UDP server.
     * @param type  The request type (PUT, GET, DELETE).
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

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT);

            byte[] requestData = Protocol.createRequest(type, key, value);
            InetAddress address = InetAddress.getByName(host);
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, address, port);

            long startTime = System.currentTimeMillis();
            socket.send(requestPacket);
            logger.info(String.format("Sent %s request to %s:%d for key: %s", requestType, host, port, key));

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            try {
                socket.receive(responsePacket);
                long endTime = System.currentTimeMillis();
                String response = new String(responsePacket.getData(), 0, responsePacket.getLength(), StandardCharsets.UTF_8);

                logger.info(String.format("Response in %d ms", (endTime - startTime)));

                return response;
            } catch (SocketTimeoutException e) {
                logger.warning(String.format("%s request for key '%s' timed out.", requestType, key));
                return "ERROR: Timeout";
            }


        } catch (UnknownHostException e) {
            logger.severe("Unknown host: " + host);
            return "ERROR: Unknown host";
        } catch (SocketException e) {
            logger.severe("Socket error: " + e.getMessage());
            return "ERROR: Socket issue";
        } catch (IOException e) {
            logger.severe("I/O error: " + e.getMessage());
            return "ERROR: I/O error";
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
     * Main method for running the UDP client interactively.
     * @param args Command line arguments (expects <host> <port>).
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            printWithTimestamp("Usage: java KeyValueStoreUDPClient <host> <port>");
            return;
        }

        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            KeyValueStoreUDPClient client = new KeyValueStoreUDPClient(host, port);

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    printWithoutNewlineWithTimestamp("Enter command (PUT/GET/DELETE) or 'exit': ");
                    String command = scanner.nextLine().trim().toUpperCase();

                    if ("EXIT".equals(command)) {
                        printWithTimestamp("Exiting client...");
                        break;
                    }

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
