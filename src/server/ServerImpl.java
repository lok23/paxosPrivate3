package server;

import java.io.Serializable;
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
import java.util.concurrent.ConcurrentHashMap;

import shared.MapServer;

public class ServerImpl implements MapServer {

    private int serverId;

    // private Set<String> mySet = new HashSet<>(); // "mySet" is just for testing. Remove once done
    private Map<String, Integer> myMap = new ConcurrentHashMap<>();

    private PaxosResults paxosResults = new PaxosResults();
    private long acceptorTimestamp = 0;
    private String acceptorMessage = null;
    private boolean acceptorIsActive = false;

    public ServerImpl() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }

    // PREPARE is a PROPOSER method
    @Override
    public PaxosResults prepare(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException {
        System.out.println("LOG MESSAGE: ServerImpl.prepare() entered. Server" + this.serverId);

        System.out.println("prepare message: " + message);

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
                System.out.println("Server1 promiseCount++");
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
            String[] response = server2.promise(timestamp); // response = [String acceptorTimestamp, String? acceptorMessage]
            if (response != null) {
                promiseCount++;
                System.out.println("Server2 promiseCount++");
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
            String[] response = server3.promise(timestamp); // response = [String acceptorTimestamp, String? acceptorMessage]
            if (response != null) {
                promiseCount++;
                System.out.println("Server3 promiseCount++");
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
                    this.paxosResults.setReturnedMap(this.myMap);
                } else {
                    this.paxosResults.setFailedPaxosRun(true);
                }
                return this.paxosResults;
            } else { // propagate the message associated with the biggestTimeStamp
                boolean successfulPaxosRun = this.propose(biggestTimeStamp, biggestMessage);
                if (successfulPaxosRun) {
                    this.paxosResults.setReturnedMap(this.myMap);
                } else {
                    this.paxosResults.setFailedPaxosRun(true);
                }
                return this.paxosResults;
            }
        } else {
            System.out.println("ServerImpl.prepare() failed to receive 2 promises. Timestamp: " + timestamp);
            this.paxosResults.setFailedPaxosRun(true);
            return this.paxosResults;
        }

    }

    // PROMISE is an ACCEPTOR method
    // promise returns the timestamp and message
    @Override
    public synchronized String[] promise(long timestamp) throws RemoteException, InterruptedException, NotBoundException {
        System.out.println("LOG MESSAGE: ServerImpl.promise() entered. Server" + this.serverId);
        // this code could be written simpler & cleaner but imo to understand it this way with all the code comments

        if (timestamp < this.acceptorTimestamp) { // we will ignore an out-of-date request
            return null;
        }

        // if a message is accepted (aka this.acceptorMessage != null), we change the timestamp to
        // the new (aka larger timestamp) and then return the existing accepted message with a newer timestamp
        if (this.acceptorMessage != null) { // we have accepted a message, let the proposer know about it
            this.acceptorIsActive = true;
            this.acceptorTimestamp = timestamp; // update timestamp, and return acceptedMessage
            String[] answer = new String[2];
            answer[0] = String.valueOf(this.acceptorTimestamp);
            answer[1] = this.acceptorMessage; // will not be null
            return answer;
        } else { // otherwise announce that you have not accepted any message
            this.acceptorIsActive = true;
            this.acceptorTimestamp = timestamp; // update the timestamp, let the proposer know we haven't accepted any message
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
                System.out.println("Server1 acceptCount++");
            }
        } catch (NotBoundException e) {
            System.out.println("Server1 not found!");
        }

        MapServer server2;
        try {
            server2 = (MapServer) registry.lookup("Server2");
            if (server2.accept(timestamp, message)) {
                acceptedCount++;
                System.out.println("Server2 acceptCount++");
            }
        } catch (NotBoundException e) {
            System.out.println("Server2 not found!");
        }

        MapServer server3;
        try {
            server3 = (MapServer) registry.lookup("Server3");
            if (server3.accept(timestamp, message)) {
                acceptedCount++;
                System.out.println("Server3 acceptCount++");
            }
        } catch (NotBoundException e) {
            System.out.println("Server3 not found!");
        }

        System.out.println("acceptedCount: " + acceptedCount);

        if (acceptedCount >= 2) { // we can go ahead, we know that we have a quorum
            this.broadcastToLearners(message);
            return true;
        } else {
            System.out.println("ServerImpl.propose() failed to receive 2 accepts. Timestamp: " + timestamp);
            return false;
        }
    }

    // ACCEPT is an ACCEPTOR method
    @Override
    public synchronized boolean accept(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException {
        System.out.println("LOG MESSAGE: ServerImpl.accept() entered. Server" + this.serverId);
        if (timestamp < this.acceptorTimestamp) { // we will ignore an out-of-date request
            return false;
        } else { // our proposal has the biggest timestamp this acceptor has seen, we can finally accept this
            this.acceptorTimestamp = timestamp;
            this.acceptorMessage = message;
            System.out.println("ACCEPTED TIMESTAMP: " + acceptorTimestamp);
            System.out.println("ACCEPTED MESSAGE: " + acceptorMessage);
            return true;
        }
    }

    // learners are responsible for adding to the set
    // v1
    @Override
    public void broadcastToLearners(String message) throws RemoteException, InterruptedException {
        System.out.println("LOG MESSAGE: ServerImpl.broadcastToLearners() entered. Server" + this.serverId);

        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        MapServer server1;
        while (true) {
            try {
                server1 = (MapServer) registry.lookup("Server1");
                server1.executeCommand(message);
                System.out.println("Server1 successfully added: " + message);
                System.out.println("Server1 myMap: " + server1.getMap());
                break;
            } catch (NotBoundException | InterruptedException e) {
                System.out.println("Server1 broadcast to learner failed! Sleeping 200 milliseconds...");
                Thread.sleep(200);
            }
        }

        MapServer server2;
        while (true) {
            try {
                server2 = (MapServer) registry.lookup("Server2");
                server2.executeCommand(message);
                System.out.println("Server2 successfully added: " + message);
                System.out.println("Server2 myMap: " + server2.getMap());
                break;
            } catch (NotBoundException | InterruptedException e) {
                System.out.println("Server2 broadcast to learner failed! Sleeping 200 milliseconds...");
                Thread.sleep(200);
            }
        }

        MapServer server3;
        while (true) {
            try {
                server3 = (MapServer) registry.lookup("Server3");
                server3.executeCommand(message);
                System.out.println("Server3 successfully added: " + message);
                System.out.println("Server3 myMap: " + server3.getMap());
                break;
            } catch (NotBoundException | InterruptedException e) {
                System.out.println("Server3 broadcast to learner failed! Sleeping 200 milliseconds...");
                Thread.sleep(200);
            }
        }
    }

    // v2 // USE THE other one?
//    @Override
//    public void broadcastToLearners(String message) throws RemoteException, InterruptedException {
//        System.out.println("LOG MESSAGE: ServerImpl.broadcastToLearners() entered. Server" + this.serverId);
//
//        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
//
//        MapServer server1;
//        MapServer server2;
//        MapServer server3;
//        while (true) {
//            try {
//                server1 = (MapServer) registry.lookup("Server1");
//                server2 = (MapServer) registry.lookup("Server2");
//                server3 = (MapServer) registry.lookup("Server3");
//                server1.executeCommand(message);
//                server2.executeCommand(message);
//                server3.executeCommand(message);
//                System.out.println("Server1 successfully added: " + message);
//                System.out.println("Server1 myMap: " + server1.getMap());
//                System.out.println("Server2 successfully added: " + message);
//                System.out.println("Server2 myMap: " + server2.getMap());
//                System.out.println("Server3 successfully added: " + message);
//                System.out.println("Server3 myMap: " + server3.getMap());
//                break;
//            } catch (NotBoundException | InterruptedException e) {
//                System.out.println("Server broadcast to learner failed! Sleeping 200 milliseconds...");
//                Thread.sleep(200);
//            }
//        }
//    }

    @Override
    public void executeCommand(String message) throws RemoteException, InterruptedException, NotBoundException {
        String[] splitClientMessage = message.split(" ");
        if (splitClientMessage[0].equals("PUT")) {
            String name = splitClientMessage[1];
            Integer salary = Integer.valueOf(splitClientMessage[2]);
            this.myMap.put(name, salary);
            // responseToClient = "Successful PUT operation: " + key + " " + stringValue; // no need to mention coordinator because client doesn't need to know about coordinator
        } else if (splitClientMessage[0].equals("GET")) {
            String name = splitClientMessage[1];
            Integer value = this.myMap.get(name);
            if (value != null) { // successful GET operation
                // responseToClient = "Successful GET operation. key=" + key + " value=" + String.valueOf(value);
            } else {
                // responseToClient = "Unsuccessful operation: GET's key does not exist";
            }
        } else if (splitClientMessage[0].equals("DELETE")) {
            String name = splitClientMessage[1];
            Integer value = this.myMap.remove(name);
            if (value != null) { // successful remove
                // responseToClient = "Successful DELETE operation: " + key; // no need to mention coordinator because client doesn't need to know about coordinator
            }  else {
                // responseToClient = "Unsuccessful operation: DELETE's key does not exist";
            }
        } else {
            System.out.println("shouldn't have gotten here");
        }

    }

    @Override
    public Map<String, Integer> getMap() throws RemoteException, InterruptedException, NotBoundException {
        return this.myMap;
    }

    @Override
    public void resetAcceptor() throws RemoteException {
        this.paxosResults = new PaxosResults();
        this.acceptorTimestamp = 0;
        this.acceptorMessage = null;
        this.acceptorIsActive = false;
    }

    @Override
    public boolean isAcceptorIsActive() throws RemoteException, InterruptedException, NotBoundException {
        return this.acceptorIsActive;
    }

    @Override
    public boolean isExistingPaxosRun() throws RemoteException {
        System.out.println("LOG MESSAGE: ServerImpl.isExistingPaxosRun() entered. Server" + this.serverId);

        boolean isContention = false;
        // TODO: make not hardcoded
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        MapServer server1;
        try {
            server1 = (MapServer) registry.lookup("Server1");
            if (server1.isAcceptorIsActive()) {
                isContention = true;
            }
        } catch (NotBoundException | InterruptedException e) {
            System.out.println("Server1 likely has contention");
        }

        MapServer server2;
        try {
            server2 = (MapServer) registry.lookup("Server2");
            if (server2.isAcceptorIsActive()) {
                isContention = true;
            }
        } catch (NotBoundException | InterruptedException e) {
            System.out.println("Server2 likely has contention");
        }

        MapServer server3;
        try {
            server3 = (MapServer) registry.lookup("Server3");
            if (server3.isAcceptorIsActive()) {
                isContention = true;
            }
        } catch (NotBoundException | InterruptedException e) {
            System.out.println("Server3 likely has contention");
        }

        return isContention;
    }

}