package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import static java.lang.Thread.sleep;

/**
 * RunClient spins up an instance of RMIClient. RunClient takes user input, and communicates
 * with the server using it's instance of RMIClient.
 */
public class RunClient {

    /**
     * Per project requirements, we are to pre-populate our map with data. This method calls 7 PUTS,
     * 5 GETS, and 5 DELETES.
     * @param IP_ADDRESS IP_Address of server
     * @param PORT_NUMBER Port number of server
     * @param client which will call methods on the server which populate the server
     * @return void
     */
    private static void fillUpServer(String IP_ADDRESS, String PORT_NUMBER, RMIClient client) throws InterruptedException, RemoteException, NotBoundException {
        System.out.println("Executing 7 PUTS, 5 GETS, 5 DELETES via fillUpServer()");
        System.out.println("Due to paxos algorithm's contentions + the random server downtimes, running multiple commands in quick session is somewhat unreliable; thus there are no guarantees that all of these commands will execute (unless we want to wait 20 seconds per run)");
        System.out.println("To give each PUT, GET, DELETE command a decent shot of executing, there is a 3 second delay between commands");
        sleep(5000);

        // 7 PUTS
        client.prepare("PUT Tom 70000");
        sleep(3000);
        client.prepare("PUT Jerry 80000");
        sleep(3000);
        client.prepare("PUT Billy 150000");
        sleep(3000);
        client.prepare("PUT Kevin 60000");
        sleep(3000);
        client.prepare("PUT Jill 100000");
        sleep(3000);
        client.prepare("PUT Wendy 120000");
        sleep(3000);
        client.prepare("PUT Elon 5000000");
        sleep(3000);

        // 5 GETS
        client.prepare("GET Billy");
        sleep(3000);
        client.prepare("GET Kevin");
        sleep(3000);
        client.prepare("GET Jill");
        sleep(3000);
        client.prepare("GET Wendy");
        sleep(3000);
        client.prepare("GET Elon");
        sleep(3000);

        // 5 DELETES
        client.prepare("DELETE Billy");
        sleep(3000);
        client.prepare("DELETE Kevin");
        sleep(3000);
        client.prepare("DELETE Jill");
        sleep(3000);
        client.prepare("DELETE Wendy");
        sleep(3000);
        client.prepare("DELETE Elon");
        sleep(3000);
    }

    /**
     * Prints some statements to the console, informing the user of how to communicate with the server.
     * @return void
     */
    private static void introduceClient() {
        System.out.println();
        System.out.println();
        System.out.println("Type commands to the server in the following format:");
        System.out.println("PUT (KEY) (VALUE) - puts an employee name (KEY) and an employee salary (VALUE) into the server.");
        System.out.println("GET (KEY) - GET an employee salary (KEY) from the server.");
        System.out.println("DELETE (KEY) - DELETE an employee entry (KEY) from the server");
        System.out.println();
        System.out.println("Here's an example how to use each:");
        System.out.println("'PUT Billy 100000'");
        System.out.println("'GET Billy'");
        System.out.println("'DELETE Billy'");
        System.out.println();
        System.out.println("Starting map is (hopefully) populated with following entries- Tom:70000, Jerry:80000");
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "10000"); // sets timeout for the rmi call. 10 seconds
        if (args.length != 2) {
            System.out.println("Please pass IP_ADDRESS and PORT_NUMBER as arguments through args");
        } else {

            String IP_ADDRESS = args[0];
            String PORT_NUMBER = args[1];

            RMIClient client = new RMIClient();
            client.startClient(IP_ADDRESS, Integer.valueOf(PORT_NUMBER));

            fillUpServer(IP_ADDRESS, PORT_NUMBER, client);
            introduceClient();

            Scanner in = new Scanner(System.in);

            while (true) {
                System.out.print("Enter commands: ");
                String line = in.nextLine();
                if (!validityCheck(line)) { // validityCheck aka sanity check to avoid clogging up paxos run
                    continue;
                }

                try {
                    client.prepare(line);
                } catch (RemoteException e) {
                    System.out.println("RemoteException, couldn't find RMI registry");
                } catch (NotBoundException e) {
                    System.out.println("NotBoundException, server not on RMI registry");
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException, connection timed out (10 seconds)");
                }

            }
        }
    }

    /**
     * Returns a String of the current system time in "yyyy-MM-dd HH:mm:ss.SSS" format
     *
     * This code from:
     * https://stackoverflow.com/questions/1459656/how-to-get-the-current-time-in-yyyy-mm-dd-hhmisec-millisecond-format-in-java
     *
     * @return String of current system time in "yyyy-MM-dd HH:mm:ss.SSS" format
     */
    public static String getFormattedCurrentSystemTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        // Get the current date
        Date currentDate = new Date();
        String currentDateTimeOutput = timeFormat.format(currentDate);
        return currentDateTimeOutput;
    }

    /**
     * Returns a boolean representing whether a given string consists of all letters and digits
     * @param string - a string that we want to verify as alphanumeric
     * @return boolean representing whether a string is alphanumeric
     */
    public static boolean isAlphaNumeric(String string) {
        for (char c : string.toCharArray()) {
            if (!Character.isLetter(c) && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a string can be converted into an integer
     * @param string - string that we want to convert to integer
     * @return boolean representing whether a string is an integer or not
     */
    public static boolean isInteger(String string) {
        long value = 0;
        for (char c : string.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
            if (value > 2147483647) { // integers only go up to this value
                return false;
            }
            value = value*10 + Integer.parseInt(String.valueOf(c));
        }
        if (value > 2147483647) { // integers only go up to this value
            return false;
        }
        return true;
    }

    /**
     * Sanity check before we send off our command to the server. This will save on unnecessary server calls.
     * @param line User input from the command line
     * @return boolean representing whether this is a valid command
     */
    public static boolean validityCheck(String line) {
        System.out.println("Timestamp=" + getFormattedCurrentSystemTime() + ". RunClient.validityCheck()");
        String[] splitClientMessage = line.split(" ");
        if (splitClientMessage[0].equals("PUT")) {
            boolean isValidCommand = checkValidityPUT(splitClientMessage);
            return isValidCommand;
        } else if (splitClientMessage[0].equals("GET")) {
            boolean isValidCommand = checkValidityGET(splitClientMessage);
            return isValidCommand;
        } else if (splitClientMessage[0].equals("DELETE")) {
            boolean isValidCommand = checkValidityDELETE(splitClientMessage);
            return isValidCommand;
        } else {
            System.out.println("Unsuccessful command: Please pick from 'PUT', 'GET', 'DELETE'");
            return false;
        }
    }

    /**
     * Helper method checks validity for PUT commands.
     * @param splitClientMessage User input split into distinct words
     * @return boolean representing whether PUT command is valid
     */
    public static boolean checkValidityPUT(String[] splitClientMessage) {
        if (splitClientMessage.length != 3) {
            System.out.println("PUT request does not have appropriate number of arguments");
            return false;
        } else {
            boolean isAlphaNum = isAlphaNumeric(splitClientMessage[1]);
            boolean isInt = isInteger(splitClientMessage[2]);
            if (isAlphaNum && isInt) { // valid input
                return true;
            } else if (!isAlphaNum && !isInt) {
                System.out.println("Unsuccessful command: PUT's key must contain only alphanumeric characters and PUT's value must be a non-negative integer");
                return false;
            } else if (!isAlphaNum) {
                System.out.println("Unsuccessful command: PUT's key must contain only alphanumeric characters");
                return false;
            } else {
                System.out.println("Unsuccessful command: PUT's value must be a non-negative integer");
                return false;
            }
        }
    }

    /**
     * Helper method checks validity for GET commands.
     * @param splitClientMessage User input split into distinct words
     * @return boolean representing whether GET command is valid
     */
    public static boolean checkValidityGET(String[] splitClientMessage) {
        if (splitClientMessage.length != 2) {
            System.out.println("GET request does not have appropriate number of arguments");
            return false;
        } else {
            boolean isAlphaNum = isAlphaNumeric(splitClientMessage[1]);
            if (!isAlphaNum) {
                System.out.println("Unsuccessful command: GET's key must contain only alphanumeric characters");
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method checks validity for DELETE commands.
     * @param splitClientMessage User input split into distinct words
     * @return boolean representing whether DELETE command is valid
     */
    public static boolean checkValidityDELETE(String[] splitClientMessage) {
        if (splitClientMessage.length != 2) {
            System.out.println("DELETE request does not have appropriate number of arguments");
            return false;
        } else {
            boolean isAlphaNum = isAlphaNumeric(splitClientMessage[1]);
            if (!isAlphaNum) {
                System.out.println("Unsuccessful command: DELETE's key must contain only alphanumeric characters");
                return false;
            }
        }
        return true;
    }

}