package paxos;

import util.Operation;

/**
 * Facade for remote Paxos RPC calls.
 * <p>
 * Encapsulates the networking or RMI logic to invoke prepare/accept
 * on remote acceptors (including local).
 * </p>
 */
public interface PaxosRPCClient {
    /**
     * Invoke prepare RPC on the given replica.
     *
     * @param addr           "host:port" of replica.
     * @param instanceId     Paxos instance ID.
     * @param proposalNumber Proposal number.
     * @param op             Operation being proposed (may be null or NOOP).
     * @return response string ("PROMISE","NACK","FAILURE").
     */
    String prepare(String addr, String instanceId, int proposalNumber, Operation op);

    /**
     * Invoke the accept RPC on a remote acceptor.
     *
     * @param addr           Address of the acceptor in "host:port" form.
     * @param instanceId     Unique Paxos instance identifier.
     * @param proposalNumber Proposal number to accept.
     * @param op             Operation to accept.
     * @return The acceptor's response: "ACCEPTED", "NACK", or "FAILURE".
     */
    String accept(String addr, String instanceId, int proposalNumber, Operation op);
}
