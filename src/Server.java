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

        connection.terminate();
    }

    // inner class to store request information
    class MeasurementInfo {
        String type;    //rtt or tput
        int msgSize;    //num. bytes in payload
        int probeNum;
        int lastProbeNum;
        int serverDelay;

        public MeasurementInfo(String type, int msgSize, int probeNum, int serverDelay) {
            this.type = type;
            this.msgSize = msgSize;
            this.probeNum = probeNum;
            this.serverDelay = serverDelay;
            lastProbeNum = 0;
        }
    }
    // run the server side of the measure program
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

        String inputLine;
        try {
            while ((inputLine = connection.in.readLine()) != null) {
                String[] inputLineParsed =  inputLine.split(" ");
                Server.MeasurementInfo measurementInfo;
                if (inputLineParsed[0].equals("s")) { // Connection setup phase
                    if (inputLineParsed.length != 5 ||
                            !(inputLineParsed[1] == "rtt" || inputLineParsed[1] == "tput") ||
                            Integer.parseInt(inputLineParsed[2]) <= 0 || Integer.parseInt(inputLineParsed[3]) <= 0) {
                        // input string is invalid
                        System.err.println("404 ERROR: Invalid Connection Setup Message");
                        break;
                    }

                    // log input values for later usage in error checking
                    measurementInfo = new MeasurementInfo(inputLineParsed[1],
                            Integer.parseInt(inputLineParsed[2]),
                            Integer.parseInt(inputLineParsed[3]),
                            Integer.parseInt(inputLineParsed[4]));
                    connection.out.println("200 OK: Ready");

                } else if (inputLineParsed[0].equals("m")) { // Measurement phase
                    if (inputLineParsed.length != 3) {
                        System.err.println("404 ERROR: Invalid Measurement Message");
                        break;
                    }
                } else if (inputLineParsed[0].equals("t")) { // Connection termination phase
                    if (!inputLine.equals("t\n")) { // if input string is not just t
                        System.err.println("404 ERROR: Invalid Connection Termination Message");
                    } else {
                        connection.out.println("200 OK: Closing Connection");
                    }
                    break; // system terminates either way
                } else {
                    System.err.println("404 ERROR: Invalid Input Message");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read input.");
        }

        connection.terminate(); // all roads lead to this termination
    }
}
