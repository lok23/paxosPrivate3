package acceptor;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import acceptor.Acceptor;

public class Acceptor3 {

    // to stop the thread
    private boolean exit;

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, InterruptedException, NotBoundException {
        Acceptor acceptor3 = new Acceptor();
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        registry.bind("Acceptor3", acceptor3);

        Random rand = new Random();
        while (true) {
            int upTime = rand.nextInt(4) * 1000 + 7000; // 7000-10000 sec upTime
            Thread.sleep(upTime);
            registry.unbind("Acceptor3");
            int downTime = rand.nextInt(3) * 1000 + 1000; // 1000-3000 sec downTime
            registry.bind("Acceptor3", acceptor3);
        }
    }
}