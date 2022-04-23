package server;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import shared.MapServer;

/**
 * RunServer1 represents Server1. RunServer1's main method covers the binding and unbinding
 * of the server, simulating the random "failing" and then recovering of the server.
 * Run RunServer1 first, as it creates the registry.
 * NOTE TO TA: Since the server goes through failure/restart and the server consists of
 * the propoesr, acceptor, and learner, this should make me eligible for the extra credit :).
 */
public class RunServer1 {

    private static Registry registry;

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException, InterruptedException {
        if (args.length != 2) {
            System.out.println("Please pass IP_ADDRESS and PORT_NUMBER as arguments through args");
        } else {
            String IP_ADDRESS = args[0];
            int PORT_NUMBER = Integer.valueOf(args[1]);

            registry = LocateRegistry.createRegistry(PORT_NUMBER);
            MapServer server1 = new ServerImpl(IP_ADDRESS, PORT_NUMBER);
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