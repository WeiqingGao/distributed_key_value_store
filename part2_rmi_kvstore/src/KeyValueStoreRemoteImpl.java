import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The implementation class of the remote interface <interface> KeyValueStoreRemote </interface>.
 * This class inherited from UnicastRemoteObject class to support the Java RMI server.
 */
public class KeyValueStoreRemoteImpl extends UnicastRemoteObject implements KeyValueStoreRemote {
    // The version number is a 8-byte number, which will be serialized together with class name as
    // the additional information, to support the later deserialization for checking if it has the
    // correct version of the class.
    private static final long serialVersionUID = 1L;

    private final Map<String, String> store = new HashMap<>();
    // The lock used to guarantee the exclusion of updating the store.
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * The constructor of this class.
     */
    public KeyValueStoreRemoteImpl() throws RemoteException {
        super();
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
        lock.lock();
        try {
            if (store.containsKey(key)) {
                throw new MalformedRequestException("The key \"" + key + "\" already exists.");
            }
            store.put(key, value);
            System.out.printf("[Server] PUT: %s => %s%n", key, value);
        } finally {
            lock.unlock();
        }
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
        lock.lock();
        try {
            if (store.containsKey(key)) {
                store.remove(key);
                System.out.printf("[Server] DELETE: %s => OK%n", key);
            } else {
                throw new MalformedRequestException("Key not found: " + key);
            }
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
}
