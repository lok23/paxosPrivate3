package shared;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * MapServer. ServerImpl implements these methods, and RMIClient accesses them via RMI.
 */
public interface MapServer extends Remote {

    Set<String> prepare(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;

    // PROMISE is an ACCEPTOR method
    String[] promise(long timestamp) throws RemoteException, InterruptedException, NotBoundException;

    // PROPOSE is a PROPOSER method
    boolean propose(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;

    // ACCEPT is an ACCEPTOR method
    boolean accept(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;

    void broadcastToLearners(String message) throws RemoteException, InterruptedException, NotBoundException;

    // ADDTOSET is a LEARNER method
    void addToSet(String message) throws RemoteException, InterruptedException, NotBoundException;

    Set<String> getSet() throws RemoteException, InterruptedException, NotBoundException;

    // clean up methods
    void resetAcceptor() throws RemoteException, InterruptedException, NotBoundException;

    boolean isAcceptorIsActive() throws RemoteException, InterruptedException, NotBoundException;

    boolean isExistingPaxosRun() throws RemoteException, InterruptedException, NotBoundException;
}