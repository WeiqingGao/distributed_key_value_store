import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Represents the RMI clients. It is able to get the remote object reference from RMI Registry.
 */
public class KeyValueStoreRMIClient {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java KeyValueStoreClient <server-host> [<port>]");
            return;
        }

        String host = args[0];
        int port = 1099;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number. Using default 1099.");
        }

        try {
            // Gets the registry from the specified host and port number.
            Registry registry = LocateRegistry.getRegistry(host, port);

            // Looks up the remote object by the name "KeyValueRMIStore".
            KeyValueStoreRemote store = (KeyValueStoreRemote) registry.lookup("KeyValueRMIStore");
            System.out.println("Connected to RMI Key-Value Store on " + host + ":" + port);

            // Pre-populates some key-value pairs
            for (int i = 0; i < 5; i++) {
                String key = "key_" + i;
                String value = "value_" + i;
                try {
                    store.put(key, value);
                    System.out.println("Pre-populated: " + key + ": " + value);
                } catch (MalformedRequestException e) {
                    System.err.println("Failed to put key_" + i + ": " + e.getMessage());
                }
            }

            // Processes the command input by the user.
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter command (PUT/GET/DELETE) or 'exit': ");
                String command = scanner.nextLine().trim().toUpperCase();

                if (command.equals("EXIT")) {
                    System.out.println("Exiting client...");
                    break;
                }

                if (!command.equals("PUT") && !command.equals("GET") && !command.equals("DELETE")) {
                    System.out.println("Invalid command. Use PUT, GET, or DELETE.");
                    continue;
                }

                System.out.print("Enter key: ");
                String key = scanner.nextLine().trim();

                if (key.isEmpty()) {
                    System.out.println("Error: Key cannot be empty.");
                    continue;
                }

                try {
                    switch (command) {
                        case "PUT":
                            System.out.print("Enter value: ");
                            String value = scanner.nextLine().trim();
                            if (value.isEmpty()) {
                                System.out.println("Error: Value cannot be empty for PUT operation.");
                                continue;
                            }
                            store.put(key, value);
                            System.out.println("PUT succeeded.");
                            break;

                        case "GET":
                            String result = store.get(key);
                            if (result == null) {
                                System.out.println("GET result: NOT_FOUND");
                            } else {
                                System.out.println("GET result: " + result);
                            }
                            break;

                        case "DELETE":
                            store.delete(key);
                            System.out.println("DELETE succeeded.");
                            break;
                    }
                } catch (MalformedRequestException e) {
                    System.out.println("Operation failed: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
