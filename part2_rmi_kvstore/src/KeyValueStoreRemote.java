import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The remote interface in which all available remote methods of the key value store are defined.
 * For this project, the methods can be invoked remotely include PUT, GET, DELETE.
 * All methods in this interface can throw RemoteException to comply with the Java RMI
 * specification. In addition, all methods can also throw MalformedRequestException, which includes
 * the application-specific exceptions that are used to handle the invalid requests from clients.
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
}
