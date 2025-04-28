package server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.*;
import common.MalformedRequestException;
import common.Protocol;
import common.Request;

/**
 * TCP Key-Value Store Server
 * Handles PUT, GET, and DELETE operations over TCP.
 */
public class KeyValueStoreTCPServer {
    private static final Logger logger = Logger.getLogger(KeyValueStoreTCPServer.class.getName());
    private final int port;
    private final Map<String, String> store = new HashMap<>();

    public KeyValueStoreTCPServer(int port) {
        this.port = port;
        setupLogger();
    }

    /**
     * Sets up logging format with millisecond precision.
     */
    private void setupLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT.%1$tL] %4$s: %5$s%n");
    }

    /**
     * Starts the TCP server and listens for connections.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("TCP Server started on port " + port);

            while (true) {
                try {
                    // Wait for client connection
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                    // Handle client request in a separate method
                    handleClient(clientSocket);

                } catch (IOException e) {
                    logger.warning("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.severe("Could not start server: " + e.getMessage());
        }
    }

    /**
     * Handles a single client request.
     */
    private void handleClient(Socket clientSocket) {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {

            // Read and parse request
            Request request = Protocol.readRequest(inputStream);
            long startTime = System.currentTimeMillis();
            logger.info(String.format("Received %s request from %s:%d for key: %s",
                getRequestTypeName(request.getType()),
                clientSocket.getInetAddress(), clientSocket.getPort(),
                request.getKey()));

            // Process request
            byte[] responseData = handleRequest(request);

            // Send response
            outputStream.write(responseData);
            outputStream.flush();
            long endTime = System.currentTimeMillis();
            logger.info(String.format("Response sent in %d ms to %s:%d",
                (endTime - startTime),
                clientSocket.getInetAddress(), clientSocket.getPort()));

        } catch (IOException | MalformedRequestException e) {
            logger.warning("Error processing client request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.warning("Error closing client socket: " + e.getMessage());
            }
        }
    }

    /**
     * Processes the request and generates a response.
     */
    private byte[] handleRequest(Request request) {
        String response;

        switch (request.getType()) {
            case Protocol.PUT:
                if (store.containsKey(request.getKey())) {
                    response = String.format("The key %s has been already existed", request.getKey());
                    logger.info(String.format("PUT failed: The key %s has been already existed", request.getKey()));
                } else {
                    store.put(request.getKey(), request.getValue());
                    response = "OK";
                    logger.info(String.format("PUT: %s = %s", request.getKey(), request.getValue()));
                }
                break;

            case Protocol.GET:
                response = store.getOrDefault(request.getKey(), "NOT_FOUND");
                logger.info(String.format("GET: %s -> %s", request.getKey(), response));
                break;

            case Protocol.DELETE:
                if (store.containsKey(request.getKey())) {
                    store.remove(request.getKey());
                    response = "OK";
                    logger.info(String.format("DELETE: %s", request.getKey()));
                } else {
                    response = String.format("The key %s is not found", request.getKey());
                    logger.info(String.format("DELETE failed: The key %s is not found", request.getKey()));
                }
                break;

            default:
                response = "ERROR: Unknown operation";
                logger.warning("Unknown operation type: " + request.getType());
        }
        return response.getBytes(StandardCharsets.UTF_8);
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

    /**
     * Main method to start the server.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java KeyValueStoreTCPServer <port>");
            return;
        }

        try {
            int port = Integer.parseInt(args[0]);
            KeyValueStoreTCPServer server = new KeyValueStoreTCPServer(port);
            server.start();
        } catch (NumberFormatException e) {
            System.out.println("Error: Port must be a number.");
        }
    }
}
