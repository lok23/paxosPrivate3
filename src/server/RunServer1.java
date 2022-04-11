package server;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;

import acceptor.Acceptor;
import acceptor.AcceptorInterface;
import shared.MapServer;

public class RunServer1 {

    private static Registry registry;

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException, InterruptedException {
        if (args.length != 1) {
            System.out.println("Please pass PORT_NUMBER as arguments through args");
        } else {
            int PORT_NUMBER = Integer.valueOf(args[0]);

            System.out.println("1");
            registry = LocateRegistry.createRegistry(PORT_NUMBER);
            MapServer server1 = new ServerImpl();
            registry.bind("Server1", server1);
            System.out.println("Server1 instantiated!");
        }
    }
}