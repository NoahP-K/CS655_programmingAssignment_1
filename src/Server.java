import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

//class to hold the methods for the server portions of the assignment
public class Server {
    //helper function to retrieve local IP
    private static String getLocalIP() throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        return localHost.getHostAddress();
    }

    //run the server side of the echo program
    public static void runEchoProgram(int port) {
        //Display local IP. Makes it easier for client-side user to find
        // address to connect to.
        try {
            String ipAddress = getLocalIP();
            System.out.println("Client can connect at " + ipAddress);
        } catch (UnknownHostException e) {
            System.err.println("Could not display local host address.");
        }

        ServerConnection connection;
        try {
            System.out.println("Starting server...");
            connection = new ServerConnection(port);
        } catch (IOException e) {
            System.err.println("Failed to create socket. Stopping server.");
            return;
        }

        System.out.println("Client connected.");

        String inputLine;
        try {
            while ((inputLine = connection.in.readLine()) != null) {
                connection.out.println(inputLine);
                if (inputLine.equals("quit")) {
                    System.out.println("Client has quit. Stopping server.");
                    break;
                }
                System.out.println("Echoing message: " + inputLine);
            }
        } catch (IOException e) {
            System.err.println("Failed to read input.");
        }

    }

    // run the server side of the measure program - TODO
    public static void runMeasureProgram(int port) {
        //Display local IP. Makes it easier for client-side user to find address to connect to.
        try {
            String ipAddress = getLocalIP();
            System.out.println("Client can connect at " + ipAddress);
        } catch (UnknownHostException e) {
            System.err.println("Could not display local host address.");
        }

        ServerConnection connection;
        try {
            System.out.println("Starting server...");
            connection = new ServerConnection(port);
        } catch (IOException e) {
            System.err.println("Failed to create socket. Stopping server.");
            return;
        }

        System.out.println("Client connected.");
    }
}
