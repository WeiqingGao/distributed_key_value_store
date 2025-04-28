package client;

import api.KeyValueStoreRemote;
import exception.PaxosConsensusException;
import exception.MalformedRequestException;
import util.LoggerUtil;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * RMI client for the distributed Key-Value Store using Paxos.
 * <p>
 * Connects to a remote KeyValueStoreRemote via RMI and allows the user
 * to perform PUT, GET, and DELETE operations interactively.
 * </p>
 */
public class KeyValueStoreRMIClient {
    /**
     * Main entry point.
     *
     * @param args args[0] = server host, args[1] = server port
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            LoggerUtil.log("Usage: java KeyValueStoreRMIClient <server-host> <port>");
            return;
        }
        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            LoggerUtil.logError("Invalid port '" + args[1] + "', using default 1099");
            port = 1099;
        }

        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            KeyValueStoreRemote store =
                (KeyValueStoreRemote) registry.lookup("KeyValueRMIStore");
            LoggerUtil.log("Connected to Key-Value Store at " + host + ":" + port);

            // Pre-populate some keys
            for (int i = 0; i < 5; i++) {
                String key = "key_" + i;
                String value = "value_" + i;
                try {
                    store.put(key, value);
                    LoggerUtil.log("[Client] Pre-populated: " + key + " => " + value);
                } catch (MalformedRequestException e) {
                    LoggerUtil.logError("Malformed request: " + e.getMessage());
                } catch (PaxosConsensusException e) {
                    LoggerUtil.logError("Consensus failure: " + e.getMessage());
                }
            }

            Scanner scanner = new Scanner(System.in);
            while (true) {
                LoggerUtil.log("Enter command (PUT/GET/DELETE) or EXIT:");
                String cmd = scanner.nextLine().trim().toUpperCase();
                if ("EXIT".equals(cmd)) {
                    LoggerUtil.log("Exiting client.");
                    break;
                }
                if (!cmd.equals("PUT") && !cmd.equals("GET") && !cmd.equals("DELETE")) {
                    LoggerUtil.log("Invalid command. Use PUT, GET, or DELETE.");
                    continue;
                }

                System.out.print("Key: ");
                String key = scanner.nextLine().trim();
                if (key.isEmpty()) {
                    LoggerUtil.log("Key cannot be empty.");
                    continue;
                }

                try {
                    switch (cmd) {
                        case "PUT":
                            System.out.print("Value: ");
                            String value = scanner.nextLine().trim();
                            if (value.isEmpty()) {
                                LoggerUtil.log("Value cannot be empty.");
                                continue;
                            }
                            store.put(key, value);
                            LoggerUtil.log("PUT succeeded.");
                            break;
                        case "GET":
                            String result = store.get(key);
                            LoggerUtil.log("GET result: " +
                                (result == null ? "NOT_FOUND" : result));
                            break;
                        case "DELETE":
                            store.delete(key);
                            LoggerUtil.log("DELETE succeeded.");
                            break;
                    }
                } catch (MalformedRequestException e) {
                    LoggerUtil.logError("Request error: " + e.getMessage());
                } catch (PaxosConsensusException e) {
                    LoggerUtil.logError("Consensus failed: " + e.getMessage());
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
