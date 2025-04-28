package server;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.*;
import common.MalformedRequestException;
import common.Protocol;
import common.Request;

/**
 * UDP Key-Value Store Server
 * Listens for incoming UDP requests and processes PUT, GET, and DELETE commands.
 */
public class KeyValueStoreUDPServer {
    private static final Logger logger = Logger.getLogger(KeyValueStoreUDPServer.class.getName());
    private final int port;
    private final Map<String, String> store = new HashMap<>();

    /**
     * Constructs a KeyValueStoreUDPServer instance.
     * Sets up the logging format by calling method <method>setupLogger()</method>
     * @param port The port number the server listens on.
     */
    public KeyValueStoreUDPServer(int port) {
        this.port = port;
        setupLogger();
    }

    /**
     * Sets up logging format with timestamps to make it more readable.
     * The format is [YYYY-MM-DD HH:MM:SS.SSS] INFO/WARNING/SEVERE: logging information
     */
    private void setupLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT.%1$tL] %4$s: %5$s%n");
    }


    /**
     * Starts the UDP server, continuously listening for requests.
     */
    public void start() {
        // trying to create a UDP server, and bind the listening port
        try (DatagramSocket socket = new DatagramSocket(port)) {
            logger.info("UDP Key-Value Store Server started on port " + port);

            // since UDP is connectionless, not reliable, a relatively smaller size 1024 is set as
            // the maximum data size the receiveBuffer can receive
            byte[] receiveBuffer = new byte[1024];

            // `while true` prevents the termination of this server until being force killed
            while (true) {
                // create a DatagramPacket instance to hold the received data
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    // wait for a packet
                    // the server will be always waiting until a request is received
                    // blocking receive
                    // once a request is received, the data(byte[]), length(int),
                    // address(InetAddress), and port(int) will be stored in the `receivePacket`
                    socket.receive(receivePacket);

                    // extract client details
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    int receivedLength = receivePacket.getLength();

                    logger.info(String.format(
                        "Received request of length %d from %s:%d",
                        receivedLength, clientAddress.getHostAddress(), clientPort
                    ));

                    // process the request by calling the method `handleRequest`
                    byte[] responseData = handleRequest(
                        Arrays.copyOf(receivePacket.getData(), receivedLength)
                    );

                    // send response
                    DatagramPacket responsePacket = new DatagramPacket(
                        responseData,
                        responseData.length,
                        clientAddress,
                        clientPort
                    );
                    socket.send(responsePacket);
                    logger.info(String.format("Response sent to %s:%d", clientAddress.getHostAddress(), clientPort));

                } catch (IOException e) {
                    logger.warning("Error processing request: " + e.getMessage());
                }
            }
        } catch (SocketException e) {
            logger.severe("Failed to start server: Could not bind to port " + port + ". " + e.getMessage());
        }
    }

    /**
     * Processes an incoming request and returns a response.
     * @param data The raw request data.
     * @return The response bytes.
     */
    private byte[] handleRequest(byte[] data) {
        try {
            Request request = Protocol.parseRequest(data);

            switch (request.getType()) {
                case Protocol.PUT:
                    if (store.containsKey(request.getKey())) {
                        logger.info(String.format("PUT failed: The key %s has been already existed", request.getKey()));
                        return String.format("The key %s has been already existed", request.getKey()).getBytes(StandardCharsets.UTF_8);
                    } else {
                        store.put(request.getKey(), request.getValue());
                        logger.info(String.format("PUT: %s = %s", request.getKey(), request.getValue()));
                        return "OK".getBytes(StandardCharsets.UTF_8);
                    }

                case Protocol.GET:
                    String value = store.get(request.getKey());
                    logger.info(String.format("GET: %s -> %s", request.getKey(), (value != null) ? value : "NOT_FOUND"));
                    return (value != null ? value : "NOT_FOUND").getBytes(StandardCharsets.UTF_8);

                case Protocol.DELETE:
                    if (store.containsKey(request.getKey())) {
                        store.remove(request.getKey());
                        logger.info(String.format("DELETE: %s", request.getKey()));
                        return "OK".getBytes(StandardCharsets.UTF_8);
                    } else {
                        logger.info(String.format("DELETE failed: The key %s is not found", request.getKey()));
                        return String.format("The key %s is not found", request.getKey()).getBytes(StandardCharsets.UTF_8);
                    }

                default:
                    throw new MalformedRequestException("Unknown operation type: " + request.getType());
            }
        } catch (MalformedRequestException e) {
            logger.warning("Received malformed request: " + e.getMessage());
            return ("ERROR: Malformed request - " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }


    /**
     * Main method to start the server.
     * @param args Command line arguments (expects port number).
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java KeyValueStoreUDPServer <port>");
            return;
        }

        try {
            int port = Integer.parseInt(args[0]);
            KeyValueStoreUDPServer server = new KeyValueStoreUDPServer(port);
            server.start();
        } catch (NumberFormatException e) {
            System.out.println("Error: Port must be a valid number.");
        }
    }
}
