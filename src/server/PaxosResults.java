package server;

import java.io.Serializable;
import java.util.Map;

public class PaxosResults implements Serializable {

    boolean isFailedPaxosRun; // null = isFailedPaxosRun
    boolean isBadInput; //  bad input = isBadInput
    String returnedMessage;
    Map<String, Integer> returnedMap; // success = returnedMap

    public PaxosResults() {
        this.isFailedPaxosRun = false;
        this.isBadInput = false;
        this.returnedMessage = "";
        this.returnedMap = null;
    }

    public boolean isFailedPaxosRun() {
        return this.isFailedPaxosRun;
    }

    public void setFailedPaxosRun(boolean failedPaxosRun) {
        this.isFailedPaxosRun = failedPaxosRun;
    }

    public boolean isBadInput() {
        return this.isBadInput;
    }

    public void setBadInput(boolean badInput) {
        this.isBadInput = badInput;
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