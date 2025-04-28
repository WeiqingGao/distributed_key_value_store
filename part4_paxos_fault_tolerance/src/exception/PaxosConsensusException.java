package exception;

/**
 * Exception thrown when Paxos consensus cannot reach a majority quorum.
 * <p>
 * Indicates that either the prepare or accept phase failed to gather enough
 * responses, or that the proposer was not the current leader.
 * </p>
 */
public class PaxosConsensusException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a PaxosConsensusException with a default message.
     */
    public PaxosConsensusException() {
        super("Paxos consensus failed");
    }

    /**
     * Constructs a PaxosConsensusException with the specified detail message.
     *
     * @param message Detailed error message explaining the failure.
     */
    public PaxosConsensusException(String message) {
        super(message);
    }

    /**
     * Constructs a PaxosConsensusException with the specified detail message and cause.
     *
     * @param message Detailed error message.
     * @param cause   Underlying cause of the exception.
     */
    public PaxosConsensusException(String message, Throwable cause) {
        super(message, cause);
    }
}
