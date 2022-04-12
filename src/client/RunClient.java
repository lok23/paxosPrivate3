package client;

import java.net.SocketTimeoutException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import client.RMIClient;

public class RunClient {
    public static void main(String[] args) throws Exception {
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "8000"); // sets timeout for the rmi call. 8 seconds
        if (args.length != 2) {
            System.out.println("Please pass IP_ADDRESS and PORT_NUMBER as arguments through args");
        } else {

            String IP_ADDRESS = args[0];
            String PORT_NUMBER = args[1];

            RMIClient client = new RMIClient();
            client.startClient(IP_ADDRESS, Integer.valueOf(PORT_NUMBER));

            Scanner in = new Scanner(System.in);

            while (true) {
                System.out.print("Enter commands: ");
                String line = in.nextLine();
                try {
                    client.prepare(line);
                } catch (RemoteException | NotBoundException | InterruptedException ignored) { // have to use "ignored" for System.setProperty()
                    System.out.println("Command did not work, connection timed out");
                }

            }
        }
    }
}