package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class RunClient {

    public static void main(String[] args) throws Exception {
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "10000"); // sets timeout for the rmi call. 10 seconds
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
                if (!validityCheck(line)) {
                    continue;
                }

                try {
                    client.prepare(line);
                } catch (RemoteException | NotBoundException | InterruptedException e) { // have to use "ignored" for System.setProperty()
                    System.out.println(e);
                    System.out.println("Connection timed out (10 seconds)");
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
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // sanity check before we send off our command to the server. This will save on unnecessary server calls
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

    public static boolean checkValidityDELETE(String[] splitClientMessage) {
        if (splitClientMessage.length != 2) {
            System.out.println("DELETE request does not have appropriate number of arguments");
            return false;
        } else {
            String key = splitClientMessage[1];
            boolean isAlphaNum = isAlphaNumeric(splitClientMessage[1]);
            if (!isAlphaNum) {
                System.out.println("Unsuccessful command: DELETE's key must contain only alphanumeric characters");
                return false;
            }
        }
        return true;
    }

}