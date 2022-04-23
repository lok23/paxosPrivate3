package shared;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import server.PaxosResults;

/**
 * MapServer. ServerImpl implements these methods, and RMIClient accesses them via RMI.
 */
public interface MapServer extends Remote {

    /**
     * PREPARE is a PROPOSER method. This method sends a prepare timestamp to all
     * acceptors, and starts the paxos algorithm. At the end, it returns information
     * pertaining to the paxos results to the client.
     *
     * @param timestamp timestamp of the request
     * @param message command we want to run, starting with PUT, GET, or DELETE
     * @return PaxosResults A POJO containing relevant information for the client
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    PaxosResults prepare(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;

    /**
     * PROMISE is an ACCEPTOR method. The acceptor receives a prepare message, and if the
     * prepare's timestamp is less than the acceptor's timestamp, it ignores that prepare message.
     * Otherwise, it sends a reply back.
     *
     * @param timestamp timestamp of the request
     * @return String[] containing timestamp and message if we accepted, or null if we rejected
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    String[] promise(long timestamp) throws RemoteException, InterruptedException, NotBoundException;

    /**
     * PROPOSE is a PROPOSER method. Also known as acceptRequest. Propose occurs if the proposer
     * received a majority of promises. Propose sends timestamp & message to all acceptors. If it
     * receives back a majority of accepts, it broadcasts the message to all other
     * servers, and then returns true.
     *
     * @param timestamp timestamp of the request
     * @param message command we want to run, starting with PUT, GET, or DELETE
     * @return boolean indicating whether propose received a majority of accepts
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    boolean propose(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;

    /**
     * ACCEPT is an ACCEPTOR method. Accept receives a propose message, and if the propose's
     * timestamp is less than the acceptor's timestamp, it ignores that propose message.
     * Otherwise, it sends a reply back.
     * @param timestamp timestamp of the request
     * @param message command we want to run, starting with PUT, GET, or DELETE
     * @return boolean indicating whether we accepted the propose message or not
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    boolean accept(long timestamp, String message) throws RemoteException, InterruptedException, NotBoundException;

    // ADDTOSET is a LEARNER method

    /**
     * Executes the GET/PUT/DELETE message. Returned messages are stored in this.paxosResults.
     * @param message command we want to run, starting with PUT, GET, or DELETE
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    void executeCommand(String message) throws RemoteException, InterruptedException, NotBoundException;

    /**
     * A clean up method. Resets the acceptors, so that we can run more than 1 paxos run.
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    void resetAcceptor() throws RemoteException, InterruptedException, NotBoundException;

    /**
     * Returns whether a paxos run is occurring. A paxos run is occurring if at least one
     * acceptor is active (aka taking part in a paxos run).
     * @return boolean, indicating whether a paxos run is occurring.
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    boolean isExistingPaxosRun() throws RemoteException, InterruptedException, NotBoundException;

    /**
     * Returns the map<k,v> that is stored on this server.
     * @return Map<String, Integer> representing an employee and their salary.
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    Map<String, Integer> getMap() throws RemoteException, InterruptedException, NotBoundException;

    /**
     * Helper method for isExistingPaxosRun(). Returns whether an acceptor is
     * active (aka taking part in a paxos run).
     * @return boolean, indicating whether an acceptor is active
     * @throws RemoteException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    boolean isAcceptorIsActive() throws RemoteException, InterruptedException, NotBoundException;

}