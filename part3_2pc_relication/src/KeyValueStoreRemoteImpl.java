import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * The implementation class of KeyValueStoreRemote with replication (2PC).
 */
public class KeyValueStoreRemoteImpl extends UnicastRemoteObject implements KeyValueStoreRemote {
    // The version number is a 8-byte number, which will be serialized together with class name as
    // the additional information, to support the later deserialization for checking if it has the
    // correct version of the class.
    private static final long serialVersionUID = 1L;
    // local store
    private final Map<String, String> store = new HashMap<>();
    // The lock used to guarantee the exclusion of updating the store.
    private final ReentrantLock lock = new ReentrantLock();
    // the list used to record the references of other replicas.
    private List<KeyValueStoreRemote> replicas = new ArrayList<>();
    // temporary storage used to record the 2PC operations(txId -> Operation)
    private Map<String, Operation> pendingOperations = new HashMap<>();

    /**
     * The constructor of this class.
     */
    public KeyValueStoreRemoteImpl() throws RemoteException {
        super();
    }

    /**
     * Sets the references to other replicas.
     * @param others
     */
    public void setReplicas(List<KeyValueStoreRemote> others) {
        this.replicas = others;
    }

    /**
     * {@inheritDoc}
     * @param key   non-empty string {@inheritDoc}
     * @param value non-empty string {@inheritDoc}
     * @throws RemoteException {@inheritDoc}
     * @throws MalformedRequestException {@inheritDoc}
     */
    @Override
    public void put(String key, String value) throws RemoteException, MalformedRequestException {
        validateKey(key);
        if (value == null || value.trim().isEmpty()) {
            throw new MalformedRequestException("Value cannot be null or empty for PUT.");
        }

        // I set the current replica as the coordinator of 2PC
        String txId = UUID.randomUUID().toString();
        // Boxes the operation as an instance of Operation
        Operation op = new Operation(Operation.Type.PUT, key, value);

        // voting phase
        // in this phase, the coordinator asks all participants if they are ready to commit the transaction
        // and if any participant says "no", in this project "NACK", then abort.
        for (KeyValueStoreRemote replica : replicas) {
            try {
                String ack = replica.prepare(txId, op);
                if (!"ACK".equals(ack)) {
                    throw new MalformedRequestException("Replica returned NACK or invalid ack: " + ack);
                }
            } catch (Exception e) {
                rollback2PC(txId);
                throw new MalformedRequestException("Prepare failed on some replica: " + e.getMessage());
            }
        }

        // commit/abort phase
        // in this project, it can not abort since the assumption that no servers will fail
        for (KeyValueStoreRemote replica : replicas) {
            try {
                replica.commit(txId);
            } catch (Exception e) {
                throw new MalformedRequestException("Commit failed on some replica: " + e.getMessage());
            }
        }
        System.out.println("[Coordinator] 2PC success for PUT " + key + "=" + value);
    }

    /**
     * {@inheritDoc}
     * @param key non-empty string {@inheritDoc}
     * @return {@inheritDoc}
     * @throws RemoteException {@inheritDoc}
     * @throws MalformedRequestException {@inheritDoc}
     */
    @Override
    public String get(String key) throws RemoteException, MalformedRequestException {
        validateKey(key);
        lock.lock();
        try {
            String value = store.get(key);
            System.out.printf("[Server] GET: %s => %s%n", key, (value == null ? "NOT_FOUND" : value));
            return value;
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     * @param key non-empty string {@inheritDoc}
     * @throws RemoteException {@inheritDoc}
     * @throws MalformedRequestException {@inheritDoc}
     */
    @Override
    public void delete(String key) throws RemoteException, MalformedRequestException {
        validateKey(key);
        String txId = UUID.randomUUID().toString();
        Operation op = new Operation(Operation.Type.DELETE, key, null);

        // voting phase
        for (KeyValueStoreRemote replica : replicas) {
            try {
                String ack = replica.prepare(txId, op);
                if (!"ACK".equals(ack)) {
                    rollback2PC(txId);
                    throw new MalformedRequestException("Replica returned NACK or invalid ack: " + ack);
                }
            } catch (Exception e) {
                rollback2PC(txId);
                throw new MalformedRequestException("Prepare failed in delete: " + e.getMessage());
            }
        }

        // commit/abort phase
        for (KeyValueStoreRemote replica : replicas) {
            replica.commit(txId);
        }
        System.out.println("[Coordinator] 2PC success for DELETE " + key);
    }

    /**
     * {@inheritDoc}
     * @param txId transaction ID
     * @param operation the specified operation
     * @return
     * @throws RemoteException
     */
    @Override
    public String prepare(String txId, Operation operation) throws RemoteException {
        lock.lock();
        try {
            switch (operation.getType()) {
                case PUT:
                    // checks if the key already exists in the store
                    if (store.containsKey(operation.getKey())) {
                        return "NACK";
                    }
                    // record it in the map
                    pendingOperations.put(txId, operation);
                    break;
                case DELETE:
                    // checks if the key exists in the store
                    if (!store.containsKey(operation.getKey())) {
                        return "NACK";
                    }
                    pendingOperations.put(txId, operation);
                    break;
            }
            return "ACK";
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     * @param txId transaction ID
     * @return
     * @throws RemoteException
     */
    @Override
    public String commit(String txId) throws RemoteException {
        lock.lock();
        try {
            Operation op = pendingOperations.remove(txId);
            if (op == null) {
                return "NONE IS NOT COMMITTED";
            }
            switch (op.getType()) {
                case PUT:
                    store.put(op.getKey(), op.getValue());
                    System.out.printf("[Replica] COMMIT PUT: %s => %s%n", op.getKey(), op.getValue());
                    break;
                case DELETE:
                    store.remove(op.getKey());
                    System.out.printf("[Replica] COMMIT DELETE: %s%n", op.getKey());
                    break;
            }
            return "COMMITTED";
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     * @param txId transaction ID
     * @return
     * @throws RemoteException
     */
    @Override
    public String abort(String txId) throws RemoteException {
        lock.lock();
        try {
            pendingOperations.remove(txId);
            System.out.println("[Replica] ABORT: " + txId);
            return "ABORTED";
        } finally {
            lock.unlock();
        }
    }

    /**
     * Validates the input of key from clients.
     * Serves as a helper function.
     * @param key the value of which to be validated.
     */
    private void validateKey(String key) throws MalformedRequestException {
        if (key == null || key.trim().isEmpty()) {
            throw new MalformedRequestException("Key cannot be null or empty.");
        }
    }


    /**
     * Helper method used to make all replicas abort when any replica fails at the voting phase.
     * @param txId
     */
    private void rollback2PC(String txId) {
        for (KeyValueStoreRemote r : replicas) {
            try {
                r.abort(txId);
            } catch (Exception e) {
                System.err.println("[Coordinator] rollback2PC failed on " + r + " : " + e.getMessage());
            }
        }
    }
}
