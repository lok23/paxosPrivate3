package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shared.MapServer;


public class ServerImpl implements MapServer {

    private int serverId;

    private long timestamp = 0;
    private String message = "";

    private boolean consensusReached = false;

    public ServerImpl() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public boolean prepare(long timestamp, String message) throws RemoteException {
        return false;
    }
}