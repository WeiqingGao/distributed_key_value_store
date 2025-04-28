package paxos;

import util.Operation;

/**
 * Encapsulates the state of a single Paxos consensus instance.
 * <p>
 * Tracks the highest promised proposal number and the accepted proposal.
 * </p>
 */
public class PaxosInstance {
    private int promised = 0;
    private int acceptedNumber = 0;
    private Operation acceptedOp = null;

    /** @return highest promised proposal number. */
    public int getPromised() { return promised; }
    /** @param p new promised number. */
    public void setPromised(int p) { this.promised = p; }

    /** @return accepted proposal number. */
    public int getAcceptedNumber() { return acceptedNumber; }
    /** @param n new accepted number. */
    public void setAcceptedNumber(int n) { this.acceptedNumber = n; }

    /** @return accepted operation, or null if none. */
    public Operation getAcceptedOp() { return acceptedOp; }
    /** @param op operation to accept. */
    public void setAcceptedOp(Operation op) { this.acceptedOp = op; }
}
