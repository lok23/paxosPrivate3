package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import shared.MapServer;

public class RMIClient {
    private MapServer server;

    public void startClient() throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        // pick one of the 2 randomly
        int randomNum = new Random().nextInt(2) + 1;
        System.out.println("randomNum: " + randomNum);
        switch (randomNum) {
            case 1: server = (MapServer) registry.lookup("Server1");
                break;
            case 2: server = (MapServer) registry.lookup("Server2");
                break;
            default: throw new Exception("invalid randomNum");
        }
    }

    public void prepare(long timestamp, String message) throws RemoteException, InterruptedException {
        boolean completed = false;
        while (!completed) {
            completed = server.prepare(timestamp, message);
        }
        System.out.println("Client prepare finished!");
    }



}