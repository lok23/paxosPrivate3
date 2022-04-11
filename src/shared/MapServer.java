package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MapServer extends Remote {

    boolean prepare(long timestamp, String message) throws RemoteException;
}