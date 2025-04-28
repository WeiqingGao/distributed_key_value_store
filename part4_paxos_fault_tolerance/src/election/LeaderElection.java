package election;

import api.KeyValueStoreRemote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Static helper implementing the ring‐based leader election logic.
 * <p>
 * Provides methods to send and receive ElectionMessage tokens around
 * a fixed ring of node addresses.
 * </p>
 */
public class LeaderElection {
    /**
     * Send an ElectionMessage to the next node in the ring.
     *
     * @param ring      Array of all node addresses in ring order.
     * @param selfIndex Index of this node in the ring.
     * @param msg       The ElectionMessage token to forward.
     * @throws Exception on RMI or lookup failure.
     */
    public static void forward(String[] ring, int selfIndex, ElectionMessage msg) throws Exception {
        int next = (selfIndex + 1) % ring.length;
        String addr = ring[next];
        String[] parts = addr.split(":");
        Registry reg = LocateRegistry.getRegistry(parts[0], Integer.parseInt(parts[1]));
        KeyValueStoreRemote stub = (KeyValueStoreRemote) reg.lookup("KeyValueRMIStore");
        stub.receiveElection(msg);
    }
}
