package server;

import java.rmi.registry.LocateRegistry;

import shared.MapServer;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Random;

import shared.MapServer;

public class RunServer3 {

    private static Registry registry;

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException, InterruptedException {
        if (args.length != 1) {
            System.out.println("Please pass PORT_NUMBER as arguments through args");
        } else {
            int PORT_NUMBER = Integer.valueOf(args[0]);

            registry = LocateRegistry.getRegistry("localhost", PORT_NUMBER);
            MapServer server3 = new ServerImpl();
            registry.bind("Server3", server3);

            long start;
            long current;
            Random rand = new Random();
            while (true) {

                System.out.println("Server3 is up!");
                int upTime = rand.nextInt(2) * 1000 + 5000; // 5000-6000 sec upTime
                start = System.currentTimeMillis();
                Thread.sleep(upTime);
                current = System.currentTimeMillis();
                System.out.println("Server3 was up for " + (current - start) + " milliseconds");

                server3.resetAcceptor();
                registry.unbind("Server3");
                System.out.println("Server3 is down!");
                int downTime = rand.nextInt(2) * 1000 + 1000; // 1000-2000 sec downTime
                start = System.currentTimeMillis();
                Thread.sleep(downTime);
                current = System.currentTimeMillis();
                System.out.println("Server3 was down for " + (current - start) + " milliseconds");

                registry.bind("Server3", server3);
            }
        }
    }
}

