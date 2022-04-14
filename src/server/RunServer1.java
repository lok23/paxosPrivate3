package server;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import shared.MapServer;

// RUN RunServer1 first, as it creates the registry
public class RunServer1 {

    private static Registry registry;

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException, InterruptedException {
        if (args.length != 1) {
            System.out.println("Please pass PORT_NUMBER as arguments through args");
        } else {
            int PORT_NUMBER = Integer.valueOf(args[0]);

            registry = LocateRegistry.createRegistry(PORT_NUMBER);
            MapServer server1 = new ServerImpl();
            registry.bind("Server1", server1);

            long start;
            long current;
            Random rand = new Random();
            while (true) {

                System.out.println("Server1 is up!");
                int upTime = rand.nextInt(2) * 1000 + 5000; // 5000-6000 sec upTime
                start = System.currentTimeMillis();
                Thread.sleep(upTime);
                current = System.currentTimeMillis();
                System.out.println("Server1 was up for " + (current - start) + " milliseconds");

                server1.resetAcceptor();
                registry.unbind("Server1");
                System.out.println("Server1 is down!");
                int downTime = rand.nextInt(2) * 1000 + 1000; // 1000-2000 sec downTime
                start = System.currentTimeMillis();
                Thread.sleep(downTime);
                current = System.currentTimeMillis();
                System.out.println("Server1 was down for " + (current - start) + " milliseconds");

                registry.bind("Server1", server1);
            }
        }
    }
}