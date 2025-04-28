package paxos;

import util.Operation;
import util.LoggerUtil;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implements the Acceptor role in Paxos.
 * <p>
 * It responds to {@code prepare} and {@code accept} requests, maintaining per-instance
 * promised and accepted proposal numbers.  A random failure can be simulated on each call.
 * </p>
 */
public class PaxosAcceptor {
    private ConcurrentMap<String, PaxosInstance> instances = new ConcurrentHashMap<>();

    /**
     * Construct an acceptor with a shared instances map.
     */
    public PaxosAcceptor(ConcurrentMap<String, PaxosInstance> instances) {
        this.instances = instances;
    }

    /**
     * Handle a prepare request.
     *
     * @param instanceId Unique identifier for the Paxos instance.
     * @param proposalNumber Proposal number of this prepare.
     * @return "PROMISE", "NACK", or "FAILURE" if a simulated failure occurred.
     */
    public String prepare(String instanceId, int proposalNumber) {
        if (simulateFailure()) {
            LoggerUtil.logError("[PaxosAcceptor] Simulated failure in prepare: inst="
                + instanceId + " pn=" + proposalNumber);
            return "FAILURE";
        }
        PaxosInstance pi = instances.computeIfAbsent(instanceId, id -> new PaxosInstance());
        synchronized (pi) {
            if (proposalNumber > pi.getPromised()) {
                pi.setPromised(proposalNumber);
                return "PROMISE";
            } else {
                return "NACK";
            }
        }
    }

    /**
     * Handle an accept request.
     *
     * @param instanceId Unique identifier for the Paxos instance.
     * @param proposalNumber Proposal number of this accept.
     * @param op Operation to accept if allowed.
     * @return "ACCEPTED", "NACK", or "FAILURE" if a simulated failure occurred.
     */
    public String accept(String instanceId, int proposalNumber, Operation op) {
        if (simulateFailure()) {
            LoggerUtil.logError("[PaxosAcceptor] Simulated failure in accept: inst="
                + instanceId + " pn=" + proposalNumber);
            return "FAILURE";
        }
        PaxosInstance pi = instances.computeIfAbsent(instanceId, id -> new PaxosInstance());
        synchronized (pi) {
            if (proposalNumber >= pi.getPromised()) {
                pi.setAcceptedNumber(proposalNumber);
                pi.setAcceptedOp(op);
                return "ACCEPTED";
            } else {
                return "NACK";
            }
        }
    }

    /**
     * Simulate a random failure with 20% probability.
     *
     * @return true if failure is simulated; false otherwise.
     */
    private boolean simulateFailure() {
        return Math.random() < 0.2;
    }
}
