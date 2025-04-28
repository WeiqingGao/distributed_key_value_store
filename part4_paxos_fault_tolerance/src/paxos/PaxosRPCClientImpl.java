package paxos;

import api.KeyValueStoreRemote;
import util.Operation;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI‐based implementation of PaxosRPCClient.
 * <p>
 * Performs RMI lookups to invoke paxosPrepare and paxosAccept on remote replicas.
 * </p>
 */
public class PaxosRPCClientImpl implements PaxosRPCClient {
    @Override
    public String prepare(String addr, String instanceId, int proposalNumber, Operation op) {
        try {
            String[] parts = addr.split(":");
            Registry reg = LocateRegistry.getRegistry(parts[0], Integer.parseInt(parts[1]));
            KeyValueStoreRemote stub = (KeyValueStoreRemote) reg.lookup("KeyValueRMIStore");
            return stub.paxosPrepare(instanceId, proposalNumber, op);
        } catch (Exception e) {
            return "FAILURE";
        }
    }

    @Override
    public String accept(String addr, String instanceId, int proposalNumber, Operation op) {
        try {
            String[] parts = addr.split(":");
            Registry reg = LocateRegistry.getRegistry(parts[0], Integer.parseInt(parts[1]));
            KeyValueStoreRemote stub = (KeyValueStoreRemote) reg.lookup("KeyValueRMIStore");
            return stub.paxosAccept(instanceId, proposalNumber, op);
        } catch (Exception e) {
            return "FAILURE";
        }
    }
}