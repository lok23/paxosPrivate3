package client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Set;

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
            long timestamp = System.currentTimeMillis();
            MapServer server;

            // try to connect with Server1, then Server2, then Server3, then repeat until it works
            int serversTrying = 1;
            switch (serversTrying) {
                case 1:
                    try {
                        server = (MapServer) registry.lookup("Server1");
                        System.out.println("CLIENT TRYING SERVER1");
                        break;
                    } catch (NotBoundException e) {
                        System.out.println("Couldn't connect to Server1, trying Server2");
                    }
                case 2:
                    try {
                        server = (MapServer) registry.lookup("Server2");
                        System.out.println("CLIENT TRYING SERVER2");
                        break;
                    } catch (NotBoundException e) {
                        System.out.println("Couldn't connect to Server2, trying Server3");
                    }
                case 3:
                    try {
                        server = (MapServer) registry.lookup("Server3");
                        System.out.println("CLIENT TRYING SERVER3");
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
                    Thread.sleep(new Random().nextInt(2) * 1000 + 1000);
                    continue;
            }
            if (server.isExistingPaxosRun()) {
                System.out.println("There is possible contention in your paxos run");
            }
            Set<String> stateOfSet = server.prepare(timestamp, message);
            if (stateOfSet != null) {
                completed = true;
                System.out.println("PAXOS run completed! State of mySet: " + stateOfSet);
                System.out.println("Attempting to reset acceptors...");
                Thread.sleep(new Random().nextInt(11) * 100); // 100-1000 milliseconds
                this.resetAcceptors();
                break;
            }
            System.out.println("Failed to get a consensus. Retrying prepare()");
            maxAttempts--;
            Thread.sleep(new Random().nextInt(3) * 1000);
        }
        if (maxAttempts <= 0) {
            System.out.println("MaxAttempts ran out! Operation cancelled. Reason: Server could not add to set");
        }
    }

    // helper method for RMIClient.prepare()
    public void resetAcceptors() throws RemoteException, InterruptedException, NotBoundException {
        try {
            MapServer server1 = (MapServer) registry.lookup("Server1");
            server1.resetAcceptor();
        } catch (NotBoundException ignored) {
            // System.out.println("(NOT TRUE) failed to reset Server1");
        }

        try {
            MapServer server2 = (MapServer) registry.lookup("Server2");
            server2.resetAcceptor();
        } catch (NotBoundException ignored) {
            // System.out.println("(NOT TRUE) failed to reset Server2");
        }

        try {
            MapServer server3 = (MapServer) registry.lookup("Server3");
            server3.resetAcceptor();
        } catch (NotBoundException ignored) {
            // System.out.println("(NOT TRUE) failed to reset Server3");
        }
    }

}