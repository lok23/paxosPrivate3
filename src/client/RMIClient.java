package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import server.PaxosResults;
import shared.MapServer;

/**
 * RMIClient. The RMIClient connects to 1 of the 3 servers, which the client uses to perform operations.
 */
public class RMIClient {
    private Registry registry;

    // all servers are stored on this RMI registry
    public void startClient(String IP_ADDRESS, int PORT_NUMBER) throws Exception {
        registry = LocateRegistry.getRegistry(IP_ADDRESS, PORT_NUMBER);
    }

    /**
     * Initiates paxos prepare method on a server, thus beginning the paxos run. Tries to initiate a
     * successful paxos run 4 times, before giving up. After a successful paxos run, resets
     * all acceptors so that a new paxos run can be initiated.
     * @param message GET, PUT, DELETE command that user wants to execute on the map
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */

    public void prepare(String message) throws RemoteException, InterruptedException, NotBoundException {
        int maxAttempts = 4;
        while (maxAttempts > 0) {
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
                    Thread.sleep(new Random().nextInt(2) * 500 + 500); // try again in 0.5-1.0 seconds
                    continue;
            }
            if (server.isExistingPaxosRun()) {
                System.out.println("There is possible contention in your paxos run");
            }
            System.out.println("Timestamp=" + getFormattedCurrentSystemTime() + ". Beginning paxos run");
            PaxosResults paxosResults = server.prepare(timestamp, message);
            if (!paxosResults.getReturnedMessage().equals("")) {
                System.out.println(paxosResults.getReturnedMessage());
            }
            if (!paxosResults.isFailedPaxosRun()) {
                System.out.println("(DEBUG MESSAGE FOR TA'S): Paxos run completed! State of map: " + paxosResults.getReturnedMap());
                System.out.println("Attempting to reset acceptors...");
                this.resetAcceptors();
                break;
            }
            System.out.println("Failed to get a consensus. Retrying prepare()");
            maxAttempts--;
            Thread.sleep(new Random().nextInt(3) * 1000);
        }
        if (maxAttempts <= 0) {
            System.out.println("MaxAttempts ran out! Operation cancelled. Reason: Failed paxos runs");
        }
    }

    /**
     * Helper method for RMIClient.prepare(). Resets acceptor state so new paxos run can occur.
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    public void resetAcceptors() throws RemoteException, InterruptedException, NotBoundException {
        try {
            MapServer server1 = (MapServer) registry.lookup("Server1");
            server1.resetAcceptor();
        } catch (NotBoundException ignored) { // may occur if the server isn't bound

        }

        try {
            MapServer server2 = (MapServer) registry.lookup("Server2");
            server2.resetAcceptor();
        } catch (NotBoundException ignored) { // may occur if the server isn't bound

        }

        try {
            MapServer server3 = (MapServer) registry.lookup("Server3");
            server3.resetAcceptor();
        } catch (NotBoundException ignored) { // may occur if the server isn't bound

        }
    }

    /**
     * Returns a String of the current system time in "yyyy-MM-dd HH:mm:ss.SSS" format
     *
     * This code from:
     * https://stackoverflow.com/questions/1459656/how-to-get-the-current-time-in-yyyy-mm-dd-hhmisec-millisecond-format-in-java
     *
     * @return String of current system time in "yyyy-MM-dd HH:mm:ss.SSS" format
     */
    public static String getFormattedCurrentSystemTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        // Get the current date
        Date currentDate = new Date();
        String currentDateTimeOutput = timeFormat.format(currentDate);
        return currentDateTimeOutput;
    }

}