package shared;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MapServer extends Remote {

    // PREPARE is a PROPOSER method
    boolean prepare(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;

    // PROMISE is a ACCEPTOR method
    String[] promise(long timestamp) throws RemoteException, InterruptedException, NotBoundException;

    // PROPOSE is a PROPOSER method
    boolean propose(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;

    // ACCEPT is an ACCEPTOR method
    boolean accept(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;
}