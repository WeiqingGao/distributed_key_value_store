package server;

import api.KeyValueStoreRemote;
import util.LoggerUtil;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.*;
import java.util.concurrent.*;

/**
 * RMI server launcher for the Ring‐based Paxos Key‐Value Store.
 * <p>
 * Parses command‐line arguments to build the ring, starts the RMI registry,
 * binds the RingElectionKVStore instance, and manages initial peer connections.
 * </p>
 */
public class KeyValueStoreRMIServer {
    private static final long RETRY_INTERVAL_MS = 5000;
    private static RingElectionKVStore kvStore;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> retryTask;
    private static List<String> ring = new ArrayList<>();
    private static List<String> failedPeers = new ArrayList<>();

    /**
     * Main entry point.
     *
     * @param args args[0]=localPort; args[1..]=peer addresses host:port
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java KeyValueStoreRMIServer <port> [peer1:port ...]");
            return;
        }
        int port = parsePort(args[0], 1099);
        buildRing(port, args);

        retryTask = scheduler.scheduleAtFixedRate(
            KeyValueStoreRMIServer::retryPeers, RETRY_INTERVAL_MS, RETRY_INTERVAL_MS, TimeUnit.MILLISECONDS
        );

        try {
            Registry registry = LocateRegistry.createRegistry(port);
            kvStore = new RingElectionKVStore(ring, ring.indexOf(ring.get(0)));
            registry.rebind("KeyValueRMIStore", kvStore);
            LoggerUtil.log("RMI server bound on port " + port);

            initialConnect();
            if (failedPeers.isEmpty()) {
                retryTask.cancel(false);
                scheduler.shutdown();
                LoggerUtil.log("All peers connected.");
            }
            LoggerUtil.log("Server ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static int parsePort(String s, int def) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) {
            LoggerUtil.logError("Invalid port '" + s + "', using " + def);
            return def;
        }
    }

    private static void buildRing(int port, String[] args) {
        ring.add("localhost:" + port);
        for (int i = 1; i < args.length; i++) {
            ring.add(args[i]);
        }
    }

    private static void initialConnect() {
        for (int i = 1; i < ring.size(); i++) {
            String peer = ring.get(i);
            if (!connectPeer(peer)) failedPeers.add(peer);
        }
    }

    private static boolean connectPeer(String addr) {
        try {
            String[] p = addr.split(":");
            Registry r = LocateRegistry.getRegistry(p[0], Integer.parseInt(p[1]));
            KeyValueStoreRemote stub = (KeyValueStoreRemote) r.lookup("KeyValueRMIStore");
            kvStore.addPeer(stub);
            LoggerUtil.log("Connected to peer " + addr);
            return true;
        } catch (Exception e) {
            LoggerUtil.logError("Failed to connect " + addr + ": " + e.getMessage());
            return false;
        }
    }

    private static void retryPeers() {
        if (failedPeers.isEmpty()) return;
        Iterator<String> it = failedPeers.iterator();
        while (it.hasNext()) {
            if (connectPeer(it.next())) it.remove();
        }
        if (failedPeers.isEmpty()) {
            retryTask.cancel(false);
            scheduler.shutdown();
            LoggerUtil.log("All peers reconnected.");
        }
    }
}
