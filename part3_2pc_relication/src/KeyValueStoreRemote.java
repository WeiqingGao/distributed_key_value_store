import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The remote interface in which all available remote methods of the key value store are defined.
 * For this project, the methods can be invoked remotely include PUT, GET, DELETE. Additionally,
 * The 2PL-related methods includes prepare, commit, and abort, which are used for internal calls
 * among replicas.
 * All methods in this interface can throw RemoteException to comply with the Java RMI
 * specification. In addition, PUT, GET, and DELETE can also throw MalformedRequestException, which
 * includes the application-specific exceptions that are used to handle the invalid requests from
 * clients.
 */
public interface KeyValueStoreRemote extends Remote {
    /**
     * Put a new key-value pair into the store.
     * @param key   non-empty string
     * @param value non-empty string
     * @throws RemoteException              RMI exceptions
     * @throws MalformedRequestException    invalid key or value exceptions
     */
    void put(String key, String value) throws RemoteException, MalformedRequestException;

    /**
     * Get the value associated with a key.
     * @param key non-empty string
     * @return the value, or null if not found
     * @throws RemoteException              RMI exceptions
     * @throws MalformedRequestException    invalid key exceptions
     */
    String get(String key) throws RemoteException, MalformedRequestException;

    /**
     * Delete the key-value pair from the store.
     * @param key non-empty string
     * @throws RemoteException              RMI exceptions
     * @throws MalformedRequestException    invalid key exceptions
     */
    void delete(String key) throws RemoteException, MalformedRequestException;

    /**
     * Used for the 1st phase, i.e. voting phase, to check if the specified operation can be
     * executed. Returns 'ACK' to notify the client that the operation can be executed, returns
     * 'NACK' otherwise
     * @param txId transaction ID
     * @param operation the specified operation
     * @return "ACK" represents ok；"NACK" represents exceptions
     */
    String prepare(String txId, Operation operation) throws RemoteException;

    /**
     * Used for the 2nd phase, i.e. commit/abort phase, to write to the key-value store.
     * @param txId transaction ID
     * @return "COMMITTED" or throws an exception
     */
    String commit(String txId) throws RemoteException;

    /**
     * Used for the 2nd phase, i.e. commit/abort phase, to abort related operations.
     * @param txId transaction ID
     * @return "ABORTED" or throws an exception
     */
    String abort(String txId) throws RemoteException;
}
