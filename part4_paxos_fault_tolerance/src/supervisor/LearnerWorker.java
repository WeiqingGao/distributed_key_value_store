package supervisor;

import server.RingElectionKVStore;

import java.util.Random;

/**
 * Worker thread that simulates the Learner role.
 * <p>
 * Periodically invokes the learner logic to apply committed Paxos operations,
 * then sleeps and throws an exception to simulate failure.
 * </p>
 */
public class LearnerWorker implements Runnable {
    private final RingElectionKVStore store;
    private final Random rand = new Random();

    /**
     * @param store Reference to the main KV store for applying learned operations.
     */
    public LearnerWorker(RingElectionKVStore store) {
        this.store = store;
    }

    @Override
    public void run() {
        try {
            while (true) {
                store.learnCommitted();
                long upTime = 2500 + rand.nextInt(3500);
                Thread.sleep(upTime);
                throw new RuntimeException("Simulated Learner failure after " + upTime + "ms");
            }
        } catch (InterruptedException e) {
        }
    }
}

