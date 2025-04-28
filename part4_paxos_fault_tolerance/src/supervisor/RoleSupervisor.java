package supervisor;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Supervises a single worker thread by periodically checking its health
 * and restarting it if it has terminated or failed.
 */
public class RoleSupervisor {
    private final String roleName;
    private final Supplier<Runnable> workerFactory;
    private final ExecutorService workerPool = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
    private Future<?> currentTask;

    /**
     * Constructs a RoleSupervisor.
     *
     * @param roleName      Name of the role (e.g., "Acceptor").
     * @param workerFactory Factory that creates new worker Runnable instances.
     */
    public RoleSupervisor(String roleName, Supplier<Runnable> workerFactory) {
        this.roleName = roleName;
        this.workerFactory = workerFactory;
        start();
    }

    /** Start the initial worker and the periodic monitor. */
    private void start() {
        restartWorker();
        monitor.scheduleAtFixedRate(() -> {
            if (currentTask.isDone() || currentTask.isCancelled()) {
                System.err.println("[" + roleName + "] worker terminated; restarting...");
                restartWorker();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /** Submit a fresh worker task to the worker pool. */
    private void restartWorker() {
        currentTask = workerPool.submit(workerFactory.get());
    }
}
