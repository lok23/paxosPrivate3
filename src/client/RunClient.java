package client;

import java.util.Scanner;

import client.RMIClient;

public class RunClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Please pass IP_ADDRESS and PORT_NUMBER as arguments through args");
        } else {

            String IP_ADDRESS = args[0];
            String PORT_NUMBER = args[1];

            RMIClient client = new RMIClient();
            client.startClient();

            Scanner in = new Scanner(System.in);

            while (true) {
                System.out.print("Enter commands: ");
                String line = in.nextLine();
                client.prepare(System.currentTimeMillis(), line);
                System.out.println("PAXOS complete!");
            }
        }
    }
}