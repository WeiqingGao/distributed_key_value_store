package election;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Message token used for ring‐based leader election.
 * <p>
 * Carries the originator’s address and the set of candidate addresses
 * seen so far.  When the token returns to its origin, the minimum address
 * (lexicographically) in the candidate set is chosen as leader.
 * </p>
 */
public class ElectionMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String origin;
    private final Set<String> candidates = new HashSet<>();

    /**
     * Constructs a new ElectionMessage.
     *
     * @param origin      The address of the node that started this election (host:port).
     * @param currentLeader The current leader address, or null if none.
     */
    public ElectionMessage(String origin, String currentLeader) {
        this.origin = origin;
        if (currentLeader != null) {
            candidates.add(currentLeader);
        }
    }

    /**
     * Add a candidate address to the set.
     *
     * @param addr The candidate address (host:port).
     */
    public void addCandidate(String addr) {
        candidates.add(addr);
    }

    /**
     * Check whether the message has returned to its originator.
     *
     * @param selfAddr This node’s address.
     * @return true if selfAddr equals the origin.
     */
    public boolean backToOrigin(String selfAddr) {
        return origin.equals(selfAddr);
    }

    /**
     * Select the leader as the lexicographically smallest address
     * among all candidates.
     *
     * @return The elected leader’s address.
     */
    public String selectLeader() {
        return Collections.min(candidates);
    }

    /** @return the originator’s address. */
    public String getOrigin() {
        return origin;
    }

    /** @return unmodifiable view of candidate addresses. */
    public Set<String> getCandidates() {
        return Collections.unmodifiableSet(candidates);
    }
}
