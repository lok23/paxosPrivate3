package acceptor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AcceptorInterface extends Remote {
    boolean prepare(long timestamp, String message) throws RemoteException;
}
