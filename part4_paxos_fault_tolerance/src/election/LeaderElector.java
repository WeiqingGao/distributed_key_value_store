package election;

import util.LoggerUtil;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrates periodic ring‐based leader election.
 * <p>
 * Uses a ScheduledExecutorService to send ElectionMessage tokens
 * at fixed intervals.  Maintains the current leader address.
 * </p>
 */
public class LeaderElector {
    private final String[] ring;
    private final int selfIndex;
    private final ScheduledExecutorService scheduler;
    private volatile String leaderAddr;

    /**
     * @param ring            Array of node addresses ("host:port") in ring order.
     * @param selfIndex       Index of this node in the ring array.
     * @param scheduler       ScheduledExecutorService for periodic tasks.
     */
    public LeaderElector(String[] ring, int selfIndex, ScheduledExecutorService scheduler) {
        this.ring = ring;
        this.selfIndex = selfIndex;
        this.scheduler = scheduler;
    }

    /**
     * Start the periodic election task.
     * <p>
     * Every interval seconds, sends an ElectionMessage around the ring.
     * </p>
     *
     * @param intervalSec Period between election rounds, in seconds.
     */
    public void start(int intervalSec) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                ElectionMessage msg = new ElectionMessage(ring[selfIndex], leaderAddr);
                msg.addCandidate(ring[selfIndex]);
                LeaderElection.forward(ring, selfIndex, msg);
            } catch (Exception e) {
                LoggerUtil.logError("Election forward failed: " + e.getMessage());
            }
        }, 0, intervalSec, TimeUnit.SECONDS);
    }

    /**
     * Handle an incoming ElectionMessage.
     * <p>
     * Should be called from the RMI stub implementation of receiveElection().
     * </p>
     *
     * @param msg The received ElectionMessage.
     */
    public synchronized void receive(ElectionMessage msg) {
        msg.addCandidate(ring[selfIndex]);
        if (msg.backToOrigin(ring[selfIndex])) {
            leaderAddr = msg.selectLeader();
            LoggerUtil.log("New leader elected: " + leaderAddr);
        } else {
            try {
                LeaderElection.forward(ring, selfIndex, msg);
            } catch (Exception e) {
                LoggerUtil.logError("Election forward failed: " + e.getMessage());
            }
        }
    }

    /**
     * @return the current leader’s address, or null if not yet elected.
     */
    public String getLeader() {
        return leaderAddr;
    }

    /**
     * @return true if this node is the current leader.
     */
    public boolean isLeader() {
        return ring[selfIndex].equals(leaderAddr);
    }
}
