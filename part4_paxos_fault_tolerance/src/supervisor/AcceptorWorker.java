package supervisor;

import server.RingElectionKVStore;

import java.util.Random;

/**
 * Worker thread that simulates the Acceptor role.
 * <p>
 * It simply sleeps for a random duration, then throws an exception
 * to simulate a failure, causing the supervisor to restart it.
 * </p>
 */
public class AcceptorWorker implements Runnable {
    private final RingElectionKVStore store;
    private final Random rand = new Random();

    /**
     * @param store Reference to the main KV store for potential interactions.
     */
    public AcceptorWorker(RingElectionKVStore store) {
        this.store = store;
    }

    @Override
    public void run() {
        try {
            while (true) {
                long upTime = 2000 + rand.nextInt(3000);
                Thread.sleep(upTime);
                throw new RuntimeException("Simulated Acceptor failure after " + upTime + "ms");
            }
        } catch (InterruptedException e) {
        }
    }
}
