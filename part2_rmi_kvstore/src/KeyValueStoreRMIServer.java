import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Represents the RMI server. It is able to create and get the RMI Registry at the local, and bind
 * it with the object of KeyValueStoreRemote.
 */
public class KeyValueStoreRMIServer {
    public static void main(String[] args) {
        int port = 1099;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default 1099");
            }
        }

        try {
            Registry registry = LocateRegistry.createRegistry(port);

            // instantiates the remote object
            KeyValueStoreRemoteImpl remoteObj = new KeyValueStoreRemoteImpl();

            // registers the identifier of the remote object by its name
            registry.rebind("KeyValueRMIStore", remoteObj);

            System.out.printf("RMI Key-Value Store Server is running on port %d...%n", port);
            System.out.println("Press Ctrl+C to stop.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
