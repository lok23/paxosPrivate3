package server;

import java.io.Serializable;
import java.util.Map;

/**
 * PaxosResults is returned to the client after the paxos run is complete. This is necessary
 * because we need to pass a lot of information back to the client, and a simple primitive can't
 * cover all the information we need to transfer.
 * PaxosResults is a POJO. As such, it is pretty self-explanatory what each method does.
 * NOTE TO TA: returnedMap is only for TA's to help TA's grade. In actuality, the
 * client probably wouldn't have the ability to see everything in a map.
 */
public class PaxosResults implements Serializable {

    private boolean isFailedPaxosRun; // isFailedPaxosRun if we failed promises
    private String returnedMessage; // message to be printed to client
    private Map<String, Integer> returnedMap; // success = returnedMap

    public PaxosResults() {
        this.isFailedPaxosRun = false;
        this.returnedMessage = "";
        this.returnedMap = null;
    }

    public boolean isFailedPaxosRun() {
        return this.isFailedPaxosRun;
    }

    public void setFailedPaxosRun(boolean failedPaxosRun) {
        this.isFailedPaxosRun = failedPaxosRun;
    }

    public String getReturnedMessage() {
        return this.returnedMessage;
    }

    public void setReturnedMessage(String returnedMessage) {
        this.returnedMessage = returnedMessage;
    }

    public Map<String, Integer> getReturnedMap() {
        return this.returnedMap;
    }

    public void setReturnedMap(Map<String, Integer> returnedMap) {
        this.returnedMap = returnedMap;
    }
}