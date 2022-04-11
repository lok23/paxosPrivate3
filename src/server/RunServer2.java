package server;

import java.rmi.registry.LocateRegistry;

import shared.MapServer;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;

import acceptor.Acceptor;
import shared.MapServer;

public class RunServer2 {

    private static Registry registry;

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException {
        if (args.length != 1) {
            System.out.println("Please pass PORT_NUMBER as arguments through args");
        } else {
            int PORT_NUMBER = Integer.valueOf(args[0]);

            System.out.println("2");
            registry = LocateRegistry.getRegistry("localhost", PORT_NUMBER);
            MapServer server2 = new ServerImpl();
            registry.bind("Server2", server2);
            System.out.println("Server2 instantiated!");
        }
    }
}

