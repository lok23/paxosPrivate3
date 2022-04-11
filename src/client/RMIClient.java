package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import server.ServerImpl;
import shared.MapServer;

public class RMIClient {
    private Registry registry;

    public void startClient(String IP_ADDRESS, int PORT_NUMBER) throws Exception {
        registry = LocateRegistry.getRegistry(IP_ADDRESS, PORT_NUMBER);
    }

    public void prepare(String message) throws RemoteException, InterruptedException, NotBoundException {
        int maxAttempts = 4;
        boolean completed = false;
        while (maxAttempts > 0 && !completed) {
            maxAttempts--;
            long timestamp = System.currentTimeMillis();
            MapServer server;

            // try to connect with Server1, then Server2, then Server3, then repeat until it works
            int serversTrying = 1;
            switch (serversTrying) {
                case 1:
                    try {
                        server = (MapServer) registry.lookup("Server1");
                        break;
                    } catch (NotBoundException e) {
                        System.out.println("Couldn't connect to Server1, trying Server2");
                    }
                case 2:
                    try {
                        server = (MapServer) registry.lookup("Server2");
                        break;
                    } catch (NotBoundException e) {
                        System.out.println("Couldn't connect to Server2, trying Server3");
                    }
                case 3:
                    try {
                        server = (MapServer) registry.lookup("Server3");
                        break;
                    } catch (NotBoundException e) {
                        System.out.println("Couldn't connect to Server3.");
                    }
                default:
                    System.out.println("Incredibly bad luck! All Servers down. Retrying from Server1");
                    maxAttempts--;
                    if (maxAttempts <= 0) {
                        System.out.println("MaxAttempts ran out! Operation cancelled. Reason: All servers down");
                    }
                    Thread.sleep(new Random().nextInt(3) * 1000);
                    continue;
            }
            completed = server.prepare(timestamp, message);
        }
        if (maxAttempts <= 0) {
            System.out.println("MaxAttempts ran out! Operation cancelled. Reason: Server could not perform operation");
        }
        System.out.println("Client prepare finished!");
    }



}