package shared;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface MapServer extends Remote {

    // PREPARE is a PROPOSER method
    Set<String> prepare(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;

    // PROMISE is an ACCEPTOR method
    String[] promise(long timestamp) throws RemoteException, InterruptedException, NotBoundException;

    // PROPOSE is a PROPOSER method
    boolean propose(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;

    // ACCEPT is an ACCEPTOR method
    boolean accept(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;
}