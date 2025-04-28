package paxos;

import util.Operation;
import util.LoggerUtil;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements the Learner role in Paxos.
 * <p>
 * Periodically scans all Paxos instances and applies any newly
 * accepted operations to the local state machine.
 * </p>
 */
public class PaxosLearner {
    private final ConcurrentMap<String, PaxosInstance> instances;
    private final Map<String,String> store;
    private final Set<String> applied = ConcurrentHashMap.newKeySet();

    /**
     * @param instances Shared Paxos instance map from acceptor.
     * @param store Local key-value store to apply operations.
     */
    public PaxosLearner(ConcurrentMap<String, PaxosInstance> instances,
                        Map<String,String> store) {
        this.instances = instances;
        this.store = store;
    }

    /**
     * Scan and apply any operations that have been accepted by a majority
     * but not yet applied locally.
     */
    public void learn() {
        for (Map.Entry<String, PaxosInstance> entry : instances.entrySet()) {
            String instId = entry.getKey();
            PaxosInstance pi = entry.getValue();
            if (pi.getAcceptedNumber() > 0 && applied.add(instId)) {
                Operation op = pi.getAcceptedOp();
                if (op != null && op.getType() != Operation.Type.NOOP) {
                    switch (op.getType()) {
                        case PUT:
                            store.put(op.getKey(), op.getValue());
                            LoggerUtil.log("[Learner] Applied PUT "
                                + op.getKey() + "=>" + op.getValue());
                            break;
                        case DELETE:
                            store.remove(op.getKey());
                            LoggerUtil.log("[Learner] Applied DELETE "
                                + op.getKey());
                            break;
                        default:
                    }
                }
            }
        }
    }

    /**
     * Return the local key-value store for read operations.
     */
    public Map<String,String> getStore() {
        return store;
    }
}
