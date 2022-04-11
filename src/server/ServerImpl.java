package server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shared.MapServer;


public class ServerImpl implements MapServer {

    private Set<String> mySet = new HashSet<>(); // "mySet" is just for testing. Remove once done
    private Map<String, Integer> myMap = new HashMap<>();
    private int serverId;

    private long proposerTimestamp = 0;
    private String proposerMessage = null;

    private long acceptorTimestamp = 0;
    private String acceptorMessage = null;

    private boolean consensusReached = false;

    public ServerImpl() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }

    // PREPARE is a PROPOSER method
    @Override
    public synchronized Set<String> prepare(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException {
        System.out.println("LOG MESSAGE: ServerImpl.prepare() entered. Server" + this.serverId);

//        this.proposerTimestamp = timestamp;
//        this.proposerMessage = message;
        int promiseCount = 0;

        // the bigger the timestamp, the "newer" it is
        // we want to propagate the message associated with the largest timestamp
        // if other messages are accepted, we want to keep the message associated with the largest timestamp
        // if no messages are accepted, we will propose (acceptRequest) our own
        long biggestTimeStamp = 0;
        String biggestMessage = null;

        // TODO: make not hardcoded
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        MapServer server1;
        try {
            server1 = (MapServer) registry.lookup("Server1");
            String[] response = server1.promise(timestamp); // response = [String acceptorTimestamp, String? acceptorMessage]
            if (response != null) {
                promiseCount++;
                long acceptorTimestamp = Long.valueOf(response[0]);
                String acceptorMessage = response[1];

                if (acceptorMessage != null) { // this acceptor has already accepted a value, consider it
                    if (acceptorTimestamp > biggestTimeStamp) { // if the acceptor's timestamp is larger than the biggestTimeStamp so far, we want to propagate the message belonging to that timestamp
                        biggestTimeStamp = acceptorTimestamp;
                        biggestMessage = acceptorMessage;
                    }
                }
            }
        } catch (NotBoundException e) {
            System.out.println("Server1 not found");
        }

        MapServer server2;
        try {
            server2 = (MapServer) registry.lookup("Server2");
            promiseCount++;
            String[] response = server2.promise(timestamp); // response = [String acceptorTimestamp, String? acceptorMessage]
            if (response != null) {
                promiseCount++;
                long acceptorTimestamp = Long.valueOf(response[0]);
                String acceptorMessage = response[1];

                if (acceptorMessage != null) { // this acceptor has already accepted a value, consider it
                    if (acceptorTimestamp > biggestTimeStamp) { // if the acceptor's timestamp is larger than the biggestTimeStamp so far, we want to propagate the message belonging to that timestamp
                        biggestTimeStamp = acceptorTimestamp;
                        biggestMessage = acceptorMessage;
                    }
                }
            }
        } catch (NotBoundException e) {
            System.out.println("Server2 not found");
        }

        MapServer server3;
        try {
            server3 = (MapServer) registry.lookup("Server3");
            promiseCount++;
            String[] response = server3.promise(timestamp); // response = [String acceptorTimestamp, String? acceptorMessage]
            if (response != null) {
                promiseCount++;
                long acceptorTimestamp = Long.valueOf(response[0]);
                String acceptorMessage = response[1];

                if (acceptorMessage != null) { // this acceptor has already accepted a value, consider it
                    if (acceptorTimestamp > biggestTimeStamp) { // if the acceptor's timestamp is larger than the biggestTimeStamp so far, we want to propagate the message belonging to that timestamp
                        biggestTimeStamp = acceptorTimestamp;
                        biggestMessage = acceptorMessage;
                    }
                }
            }
        } catch (NotBoundException e) {
            System.out.println("Server3 not found");
        }

        System.out.println("promiseCount: " + promiseCount);

        if (promiseCount >= 2) { // we can go ahead, we know that we have a quorum
            if (biggestTimeStamp == 0 && biggestMessage == null) { // nothing has been accepted yet, propose our own message
                boolean successfulPaxosRun = this.propose(timestamp, message);
                if (successfulPaxosRun) {
                    return mySet;
                } else {
                    return null;
                }
            } else { // propagate the message associated with the biggestTimeStamp
                boolean successfulPaxosRun = this.propose(biggestTimeStamp, biggestMessage);
                if (successfulPaxosRun) {
                    return mySet;
                } else {
                    return null;
                }
            }
        } else {
            System.out.println("ServerImpl.prepare() failed to receive 2 promises. Timestamp: " + timestamp);
            return null;
        }

    }

    // PROMISE is an ACCEPTOR method
    // promise returns the timestamp and message
    @Override
    public String[] promise(long timestamp) throws RemoteException, InterruptedException, NotBoundException {
        System.out.println("LOG MESSAGE: ServerImpl.promise() entered. Server" + this.serverId);
        // this code could be simpler but it's easier for me to understand it this way

        if (timestamp < this.acceptorTimestamp) { // we will ignore an out-of-date request
            return null;
        }

        // if a message is accepted (aka this.acceptorMessage != null), we change the timestamp to
        // the new (aka larger timestamp) and then return the accepted message with a newer timestamp
        if (this.acceptorMessage != null) { // we have accepted a message, let the proposer know about it
            this.acceptorTimestamp = timestamp; // update timestamp, and return acceptedMessage
            String[] answer = new String[2];
            answer[0] = String.valueOf(this.acceptorTimestamp);
            answer[1] = this.acceptorMessage;
            return answer;
        } else { // otherwise announce that you have not accepted any message
            // update the timestamp, let the proposer know we haven't accepted any message
            this.acceptorTimestamp = timestamp;
            String[] answer = new String[2];
            answer[0] = String.valueOf(this.acceptorTimestamp);
            answer[1] = this.acceptorMessage; // null
            return answer;
        }
    }

    // PROPOSE is a PROPOSER method
    @Override
    public boolean propose(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException {
        System.out.println("LOG MESSAGE: ServerImpl.propose() entered. Server" + this.serverId);

        int acceptedCount = 0;
        // TODO: make not hardcoded
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        MapServer server1;
        try {
            server1 = (MapServer) registry.lookup("Server1");
            if (server1.accept(timestamp, message)) {
                acceptedCount++;
            }
        } catch (NotBoundException e) {
            System.out.println("Server1 not found!");
        }

        MapServer server2;
        try {
            server2 = (MapServer) registry.lookup("Server2");
            if (server2.accept(timestamp, message)) {
                acceptedCount++;
            }
        } catch (NotBoundException e) {
            System.out.println("Server2 not found!");
        }

        MapServer server3;
        try {
            server3 = (MapServer) registry.lookup("Server3");
            if (server3.accept(timestamp, message)) {
                acceptedCount++;
            }
        } catch (NotBoundException e) {
            System.out.println("Server3 not found!");
        }

        System.out.println("acceptedCount: " + acceptedCount);

        if (acceptedCount >= 2) { // we can go ahead, we know that we have a quorum
            mySet.add(message);
            System.out.println("Server successfully added: " + message);
            System.out.println("mySet: " + mySet);
//            myMap.put()
            return true;
        } else {
            System.out.println("ServerImpl.propose() failed to receive 2 accepts. Timestamp: " + timestamp);
            return false;
        }
    }

    // ACCEPT is an ACCEPTOR method
    @Override
    public boolean accept(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException {
        System.out.println("LOG MESSAGE: ServerImpl.accept() entered. Server" + this.serverId);
        if (timestamp < this.acceptorTimestamp) {
            return false;
        } else { // our proposal has the biggest timestamp this acceptor has seen, we can finally accept this
            this.acceptorTimestamp = timestamp;
            this.acceptorMessage = message;
            return true;
        }
    }


}