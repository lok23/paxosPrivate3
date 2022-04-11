package acceptor;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import shared.MapServer;

public class Acceptor implements AcceptorInterface {

    // to stop the thread
    private boolean exit;

    public Acceptor() throws RemoteException {
        System.out.println("An acceptor was instantiated!");
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public boolean prepare(long timestamp, String message) throws RemoteException {
        return false;
    }
}