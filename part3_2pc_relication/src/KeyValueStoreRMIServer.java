import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents the RMI server that hosts a distributed replicated Key-Value Store.
 *
 * <p>
 * This server connects to other replicas (provided via command-line arguments), and maintains
 * a list of active replicas. It continuously retries to connect to any failed replicas until
 * all are reachable.
 * </p>
 *
 * <p>
 * When started, the server will:
 * <ul>
 *     <li>Bind its own KeyValueStoreRemoteImpl object to the local RMI registry.</li>
 *     <li>Attempt to connect to other replica servers.</li>
 *     <li>Periodically retry failed connections.</li>
 * </ul>
 * </p>
 *
 * <p>Usage Example:
 * <code>java KeyValueStoreRMIServer 1099 localhost:1100 localhost:1101</code>
 * </p>
 *
 * <p>
 * The current replica will participate in two-phase commit protocols along with other replicas.
 * </p>
 */
public class KeyValueStoreRMIServer {
    // the local key-value store object implementing RMI interface
    private static KeyValueStoreRemoteImpl localObj;
    // the list of successfully connected replicas (including self)
    private static List<KeyValueStoreRemote> replicaStubs = new ArrayList<>();
    //the list of replicas that failed to connect, to be retried later
    private static List<String> failedReplicas = new ArrayList<>();

    private static final long RETRY_INTERVAL = 5000;
    // the scheduler to periodically retry failed replicas
    private static ScheduledExecutorService scheduler;
    // the reference to the scheduled retry task
    private static ScheduledFuture<?> retryTask;

    /**
     * Main method to start the RMI server.
     * @param args First argument is the local port number. The following arguments are other replicas in "host:port" format.
     */
    public static void main(String[] args) {
        int myPort = 1099;
        List<String> otherReplicas = new ArrayList<>();

        // initializes the scheduler
        scheduler = Executors.newScheduledThreadPool(1);
        // calls retryFailedReplicas regularly，once every RETRY_INTERVAL milliseconds
        retryTask = scheduler.scheduleAtFixedRate(() -> retryFailedReplicas(), 5, 5, TimeUnit.SECONDS);

        if (args.length >= 1) {
            try {
                myPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default 1099");
                myPort = 1099;
            }
        }

        for (int i = 1; i < args.length; i++) {
            otherReplicas.add(args[i]);
        }

        try {
            Registry registry = LocateRegistry.createRegistry(myPort);

            // instantiates the local KeyValueStoreRemoteImpl
            localObj = new KeyValueStoreRemoteImpl();
            // adds the coordinator, i.e. the current replica to the replicas
            // then in the voting phase, the current replica can also be asked if ready
            replicaStubs.add(localObj);

            registry.rebind("KeyValueRMIStore", localObj);
            System.out.printf("[Server] RMI Key-Value Store running on port %d...%n", myPort);

            // first time to try to connect other replicas
            for (String rep : otherReplicas) {
                if (!tryConnectReplica(rep)) {
                    failedReplicas.add(rep);
                }
            }

            if (failedReplicas.isEmpty()) {
                System.out.println("All replicas found!");
                retryTask.cancel(false);
                scheduler.shutdown();
            }

            localObj.setReplicas(replicaStubs);

            System.out.println("[Server] Ready. Press Ctrl+C to stop.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Attempts to connect to a replica and adds its stub to the replica list if successful.
     *
     * @param rep The replica address in the format "host:port".
     * @return true if connection is successful; false otherwise.
     */
    private static boolean tryConnectReplica(String rep) {
        String[] hp = rep.split(":");
        if (hp.length != 2) {
            System.err.println("Replica param invalid: " + rep);
            return false;
        }

        String host = hp[0];
        int port;
        try {
            port = Integer.parseInt(hp[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port for " + rep);
            return false;
        }

        try {
            Registry otherReg = LocateRegistry.getRegistry(host, port);
            KeyValueStoreRemote stub = (KeyValueStoreRemote) otherReg.lookup("KeyValueRMIStore");
            replicaStubs.add(stub);
            System.out.println("[Server] Found replica at " + rep);
            // notifies the local object to update the list
            localObj.setReplicas(replicaStubs);
            return true;
        } catch (Exception e) {
            System.err.println("Failed connecting to replica: " + rep + " -> " + e.getMessage());
            return false;
        }
    }

    /**
     * Retries connecting to failed replicas periodically. Once all replicas are connected, stops
     * the retry task.
     */
    private static void retryFailedReplicas() {
        // if all replicas have been connected successfully, then nothing needs to do
        if (failedReplicas.isEmpty()) {
            System.out.println("All replicas found!");
            retryTask.cancel(false);
            scheduler.shutdown();
            return;
        }

        Iterator<String> it = failedReplicas.iterator();
        while (it.hasNext()) {
            String rep = it.next();
            if (tryConnectReplica(rep)) {
                it.remove();
            }
        }
    }
}
