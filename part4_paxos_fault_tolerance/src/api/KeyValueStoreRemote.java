package api;

import exception.MalformedRequestException;
import exception.PaxosConsensusException;
import util.Operation;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for a replicated Key-Value Store using Paxos consensus.
 * <p>
 * Defines client‐facing operations (PUT, GET, DELETE) as well as
 * internal Paxos RPC calls (prepare, accept) used by proposers and acceptors.
 * </p>
 */
public interface KeyValueStoreRemote extends Remote {

    /**
     * Store or update a key‐value pair.  Must be invoked on the current leader.
     *
     * @param key   Non‐empty key string.
     * @param value Non‐empty value string.
     * @throws RemoteException            on RMI error.
     * @throws MalformedRequestException  if key or value is invalid.
     * @throws PaxosConsensusException    if Paxos consensus fails.
     */
    void put(String key, String value)
        throws RemoteException, MalformedRequestException, PaxosConsensusException;

    /**
     * Retrieve the value associated with a key.
     *
     * @param key Non‐empty key string.
     * @return The value, or null if not present.
     * @throws RemoteException           on RMI error.
     * @throws MalformedRequestException if key is invalid.
     */
    String get(String key)
        throws RemoteException, MalformedRequestException;

    /**
     * Delete a key‐value pair.  Must be invoked on the current leader.
     *
     * @param key Non‐empty key string.
     * @throws RemoteException           on RMI error.
     * @throws MalformedRequestException if key is invalid or not found.
     * @throws PaxosConsensusException   if Paxos consensus fails.
     */
    void delete(String key)
        throws RemoteException, MalformedRequestException, PaxosConsensusException;

    /**
     * Paxos prepare RPC invoked by proposers on acceptors.
     *
     * @param instanceId     Unique Paxos instance identifier.
     * @param proposalNumber Proposal number.
     * @param op             Operation being proposed.
     * @return "PROMISE", "NACK", or "FAILURE" on simulated failure.
     * @throws RemoteException on RMI error.
     */
    String paxosPrepare(String instanceId, int proposalNumber, Operation op)
        throws RemoteException;

    /**
     * Paxos accept RPC invoked by proposers on acceptors.
     *
     * @param instanceId     Unique Paxos instance identifier.
     * @param proposalNumber Proposal number.
     * @param op             Operation being accepted.
     * @return "ACCEPTED", "NACK", or "FAILURE" on simulated failure.
     * @throws RemoteException on RMI error.
     */
    String paxosAccept(String instanceId, int proposalNumber, Operation op)
        throws RemoteException;

    /**
     * Handle an incoming election token for ring-based leader election.
     *
     * @param msg The election message being forwarded.
     * @throws RemoteException on RMI error.
     */
    void receiveElection(election.ElectionMessage msg) throws RemoteException;

}
