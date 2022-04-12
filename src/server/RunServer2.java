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
import java.util.Random;

import shared.MapServer;

public class RunServer2 {

    private static Registry registry;

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException, InterruptedException {
        if (args.length != 1) {
            System.out.println("Please pass PORT_NUMBER as arguments through args");
        } else {
            int PORT_NUMBER = Integer.valueOf(args[0]);

            registry = LocateRegistry.getRegistry("localhost", PORT_NUMBER);
            MapServer server2 = new ServerImpl();
            registry.bind("Server2", server2);

            long start;
            long current;
            Random rand = new Random();
            while (true) {

                System.out.println("Server2 is up!");
                int upTime = rand.nextInt(4) * 1000 + 7000; // 7000-10000 sec upTime
                start = System.currentTimeMillis();
                Thread.sleep(upTime);
                current = System.currentTimeMillis();
                System.out.println("Server2 was up for " + (current - start) + " milliseconds");

                server2.resetAcceptor(); // this might not be good
                registry.unbind("Server2");
                System.out.println("Server2 is down!");
                int downTime = rand.nextInt(3) * 1000 + 3000; // 3000-5000 sec downTime
                start = System.currentTimeMillis();
                Thread.sleep(downTime);
                current = System.currentTimeMillis();
                System.out.println("Server2 was down for " + (current - start) + " milliseconds");

                registry.bind("Server2", server2);
            }
        }
    }
}

