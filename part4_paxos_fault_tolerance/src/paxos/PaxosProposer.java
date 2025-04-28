package paxos;

import api.KeyValueStoreRemote;
import exception.PaxosConsensusException;
import util.Operation;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Proposer role in Paxos.
 * <p>
 * Only the elected leader should invoke {@code propose}.  It drives
 * the prepare and accept phases, with optional retry logic.
 * </p>
 */
public class PaxosProposer {
    private final List<String> peerAddrs;
    private final PaxosAcceptor localAcceptor;
    private final PaxosRPCClient rpcClient;
    private int proposalCounter = 0;
    private final List<KeyValueStoreRemote> peerStubs = new ArrayList<>();

    /**
     * @param peerAddrs List of all replica addresses ("host:port").
     * @param localAcceptor Local acceptor to handle self RPCs.
     * @param rpcClient Client to invoke remote RPCs on other replicas.
     */
    public PaxosProposer(List<String> peerAddrs,
                         PaxosAcceptor localAcceptor,
                         PaxosRPCClient rpcClient) {
        this.peerAddrs = peerAddrs;
        this.localAcceptor = localAcceptor;
        this.rpcClient = rpcClient;
    }

    /**
     * Drive a Paxos proposal for the given operation.
     *
     * @param op Operation to propose.
     * @throws PaxosConsensusException if prepare or accept quorum cannot be reached.
     */
    public void propose(Operation op) throws PaxosConsensusException {
        String instanceId = "inst-" + System.nanoTime();
        int pn = ++proposalCounter;
        int total = peerAddrs.size(), quorum = total / 2 + 1;

        // Prepare phase
        int promises = 0;
        for (String addr : peerAddrs) {
            String resp = rpcClient.prepare(addr, instanceId, pn, op);
            if ("PROMISE".equals(resp)) promises++;
        }
        if (promises < quorum) {
            throw new PaxosConsensusException("Prepare quorum failed: " + promises + "/" + total);
        }

        // Accept phase
        int accepts = 0;
        for (String addr : peerAddrs) {
            String resp = rpcClient.accept(addr, instanceId, pn, op);
            if ("ACCEPTED".equals(resp)) accepts++;
        }
        if (accepts < quorum) {
            throw new PaxosConsensusException("Accept quorum failed: " + accepts + "/" + total);
        }
    }

    /**
     * Add a new peer stub for RPC calls.
     * @param stub Remote stub implementing KeyValueStoreRemote.
     */
    public void addPeer(KeyValueStoreRemote stub) {
        peerStubs.add(stub);
    }
}
