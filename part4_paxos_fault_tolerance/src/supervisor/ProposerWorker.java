package supervisor;

import java.util.Random;
import server.RingElectionKVStore;

/**
 * Worker thread that simulates the Proposer (Leader) role.
 * <p>
 * Periodically, if this node is the current leader, it sends a no-op Paxos proposal.
 * Then it sleeps and eventually throws an exception to simulate failure.
 * </p>
 */
public class ProposerWorker implements Runnable {
    private final RingElectionKVStore store;
    private final Random rand = new Random();

    /**
     * @param store Reference to the main KV store for leader checks and proposals.
     */
    public ProposerWorker(RingElectionKVStore store) {
        this.store = store;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (store.isLeader()) {
                    store.noOpProposal();
                }
                long upTime = 3000 + rand.nextInt(4000);
                Thread.sleep(upTime);
                throw new RuntimeException("Simulated Proposer failure after " + upTime + "ms");
            }
        } catch (InterruptedException e) {
        }
    }
}
