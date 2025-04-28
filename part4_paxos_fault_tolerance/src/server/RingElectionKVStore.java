package server;

import api.KeyValueStoreRemote;
import election.LeaderElector;
import exception.PaxosConsensusException;
import paxos.*;
import supervisor.*;
import util.LoggerUtil;
import util.Operation;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.*;
import java.util.concurrent.*;

/**
 * Main RMI object implementing KeyValueStoreRemote.
 * <p>
 * Integrates ring‐based leader election, Paxos roles, and supervised threads.
 * </p>
 */
public class RingElectionKVStore extends UnicastRemoteObject implements KeyValueStoreRemote {
    private final List<String> ring;
    private final int selfIndex;
    private final PaxosAcceptor acceptor;
    private final PaxosLearner learner;
    private final PaxosProposer proposer;
    private final LeaderElector elector;
    private final RoleSupervisor acceptorSup, proposerSup, learnerSup;
    private volatile String leaderAddr;

    public RingElectionKVStore(List<String> ring, int selfIndex) throws RemoteException {
        super();
        this.ring = ring;
        this.selfIndex = selfIndex;
        this.leaderAddr = ring.get(selfIndex);

        // Paxos components
        ConcurrentMap<String,PaxosInstance> instances = new ConcurrentHashMap<>();
        acceptor = new PaxosAcceptor(instances);
        learner = new PaxosLearner(instances, new ConcurrentHashMap<>());

        // RPC client
        PaxosRPCClient rpcClient = new PaxosRPCClientImpl();
        proposer = new PaxosProposer(ring, acceptor, rpcClient);

        // Register self and peers
        proposer.addPeer(this);
        for (String addr : ring) {
            if (!addr.equals(ring.get(selfIndex))) {
                try {
                    KeyValueStoreRemote stub = lookup(addr);
                    proposer.addPeer(stub);
                } catch (Exception e) {
                    LoggerUtil.logError("Failed to lookup peer " + addr + ": " + e.getMessage());
                }
            }
        }

        // Leader election
        ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
        elector = new LeaderElector(ring.toArray(new String[0]), selfIndex, sched);
        elector.start(5);

        // Supervisors
        acceptorSup = new RoleSupervisor("Acceptor", () -> new AcceptorWorker(this));
        proposerSup = new RoleSupervisor("Proposer", () -> new ProposerWorker(this));
        learnerSup = new RoleSupervisor("Learner", () -> new LearnerWorker(this));
    }

    /** Lookup a remote stub by host:port. */
    private KeyValueStoreRemote lookup(String addr) throws Exception {
        String[] parts = addr.split(":");
        Registry reg = LocateRegistry.getRegistry(parts[0], Integer.parseInt(parts[1]));
        return (KeyValueStoreRemote) reg.lookup("KeyValueRMIStore");
    }

    /** Add a new peer stub for Paxos RPC. */
    public void addPeer(KeyValueStoreRemote stub) {
        proposer.addPeer(stub);
    }

    @Override
    public void put(String key, String value)
        throws RemoteException, PaxosConsensusException {
        ensureLeader();
        proposer.propose(new Operation(Operation.Type.PUT, key, value));
    }

    @Override
    public String get(String key) {
        return learner.getStore().get(key);
    }

    @Override
    public void delete(String key)
        throws RemoteException, PaxosConsensusException {
        ensureLeader();
        proposer.propose(new Operation(Operation.Type.DELETE, key, null));
    }

    @Override
    public String paxosPrepare(String inst, int pn, Operation op) {
        return acceptor.prepare(inst, pn);
    }

    @Override
    public String paxosAccept(String inst, int pn, Operation op) {
        return acceptor.accept(inst, pn, op);
    }

    private void ensureLeader() throws PaxosConsensusException {
        if (!ring.get(selfIndex).equals(leaderAddr)) {
            throw new PaxosConsensusException("Not leader: " + ring.get(selfIndex));
        }
    }

    public boolean isLeader() {
        return ring.get(selfIndex).equals(leaderAddr);
    }

    public void noOpProposal() {
        try {
            proposer.propose(new Operation(Operation.Type.NOOP, "", ""));
            LoggerUtil.log("[Proposer] No-op proposal succeeded");
        } catch (PaxosConsensusException e) {
            LoggerUtil.logError("[Proposer] No-op failed: " + e.getMessage());
        }
    }

    public void learnCommitted() {
        learner.learn();
    }

    @Override
    public void receiveElection(election.ElectionMessage msg) throws RemoteException {
        elector.receive(msg);
        this.leaderAddr = elector.getLeader();
    }
}
