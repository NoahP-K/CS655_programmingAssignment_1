import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

//class to hold the methods for the server portions of the assignment
public class Server {
    private static final String msg404 = "404 ERROR: ";
    private static final String msg200 = "200 OK: ";

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
    static class MeasurementInfo {
        String type;    //rtt or tput
        int msgSize;    //num. bytes in payload
        int probeNum;
        int nextProbeNum;
        int serverDelay;

        public MeasurementInfo() {
            this.type = "unknown";
            this.msgSize = 0;
            this.probeNum = 0;
            this.serverDelay = 0;
            nextProbeNum = 0;
        }
    }

    private static boolean connectionSetupPhase(
            MeasurementInfo info,
            String inputLine
    ) {
        //String inputLine = "";
        try {
            //inputLine = connection.in.readLine();
            String[] inputLineParsed = inputLine.split(" ");
            //verify that the setup message can be parsed into five sections
            if(inputLineParsed.length != 5) {
                throw new Exception("Setup message is in the wrong format.");
            }
            //verify that this is a setup message
            if(!inputLineParsed[0].equals("s")) {
                throw new Exception("Expected setup (s). Message was labeled as " + inputLineParsed[0]);
            }
            //verify the type of measurement
            if (!inputLineParsed[1].equals("rtt") && !inputLineParsed[1].equals("tput")) {
                throw (new Exception("Invalid measurement type " + inputLineParsed[1]));
            } else {info.type = inputLineParsed[1];}
            //verify the number of probes
            int probeNum = Integer.parseInt(inputLineParsed[2]);
            if (probeNum <= 0) {
                throw new Exception("Invalid number of probes: " + inputLineParsed[2]);
            } else {info.probeNum = probeNum;}
            //verify the size of the message
            int msgSize = Integer.parseInt(inputLineParsed[3]);
            if (msgSize <= 0) {
                throw new Exception("Invalid message size: " + inputLineParsed[3]);
            } else {info.msgSize = msgSize;}
            //verify server delay
            int serverDelay = Integer.parseInt(inputLineParsed[4]);
            if (serverDelay < 0) {
                throw new Exception("Invalid server delay: " + inputLineParsed[4]);
            } else {info.serverDelay = serverDelay;}
        } catch (IOException e) {
            System.err.println("Issue reading message.");
            return false;
        } catch (Exception e) {
            System.err.println("Invalid setup message: " + inputLine);
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    private static boolean measurementPhase(
            MeasurementInfo info,
            ServerConnection connection
    ) {
        String inputLine = "";
        try {
            while(true){
                inputLine = connection.in.readLine();
                String[] inputLineParsed = inputLine.split(" ");

                //Error checking:
                //verify that this is a probe message
                if(!inputLineParsed[0].equals("m")) {
                    throw new Exception(
                            "Expected measurement probe (m). Message was labeled as " + inputLineParsed[0]
                    );
                }
                //verify that the test message can be parsed into three sections
                if(inputLineParsed.length != 3) {
                    throw new Exception("Setup message is in the wrong format.");
                }
                //verify that the sequence number received is correct.
                int probeNum = Integer.parseInt(inputLineParsed[1]);
                if(probeNum >= info.probeNum || probeNum < 0) {
                    throw new Exception(
                            "Only expected probe numbers up to "
                            + (info.probeNum-1) + ", got " + probeNum
                    );
                }
                if(probeNum != info.nextProbeNum) {
                    throw new Exception(
                            "Probes out of order. "
                            + "Expected probe number " + info.nextProbeNum + ", got " + probeNum
                    );
                } else {info.nextProbeNum++;}
                // verify that probe contents are of the correct size
                if(inputLineParsed[2].length() != info.msgSize / Character.BYTES) {
                    throw new Exception(
                            "Wrong size probe. "
                            + "Expected size " + info.msgSize + " bytes, got "
                                    + inputLineParsed[2].length() + " characters."
                    );
                }

                //Echo the message back after server delay time
                Thread.sleep(info.serverDelay);
                System.out.println("Probe number " + probeNum + " received. Echoing message.");
                connection.out.println(inputLine);

                //if this was the last probe message, stop this measurement phase
                if(probeNum == info.probeNum-1) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Issue sending/reading message.");
            return false;
        } catch (Exception e) {
            System.err.println("Invalid probe message: " + inputLine);
            System.err.println(e.getMessage());
            return false;
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

        //Indefinitely wait for client messages.
        //Start by checking if a termination message has been sent.
        //Otherwise, alternate between waiting on connection messages and groups of test messages.
        while (true) {
            //Check for termination message
            //This would have been its own method, but there was too much complexity that came
            // from this method potentially reading a setup message and needing to forward it.
            String inputLine = "";
            try {
                inputLine = connection.in.readLine();
                String[] inputLineParsed = inputLine.split(" ");
                //check that, if this is a termination message, its format is correct
                if(inputLineParsed[0].equals("t") && inputLineParsed.length != 1) {
                    throw new Exception("Termination message is in the wrong format.");
                } else if(inputLineParsed[0].equals("t")) {
                    //if the termination message was sent, acknowledge and terminate
                    System.out.println("Termination message received. Terminating connection.");
                    connection.out.println(msg200 + "Closing Connection");
                    connection.terminate();
                    return;
                }
            } catch (IOException e) {
                System.err.println("Issue reading message.");
                connection.out.println(msg404 + "Invalid Connection Termination Message");
                System.err.println("Encountered error 404. Setup failed. Terminating connection.");
                connection.terminate();
                return;
            } catch (Exception e) {
                System.err.println("Invalid termination message: " + inputLine);
                System.err.println(e.getMessage());
                connection.out.println(msg404 + "Invalid Connection Termination Message");
                System.err.println("Encountered error 404. Setup failed. Terminating connection.");
                connection.terminate();
                return;
            }

            //CSP
            System.out.println("Waiting on setup message...");
            MeasurementInfo info = new MeasurementInfo();
            boolean didSetUp = connectionSetupPhase(info, inputLine);
            if(!didSetUp) {
                connection.out.println(msg404 + "Invalid Connection Setup Message");
                System.err.println("Encountered error 404. Setup failed. Terminating connection.");
                connection.terminate();
                return;
            }
            connection.out.println(msg200 + "Ready");

            //MP
            System.out.println("Setup message received. Waiting on probes...");
            boolean didMeasure = measurementPhase(info, connection);
            if(!didMeasure) {
                connection.out.println(msg404 + "Invalid Measurement Message");
                System.err.println("Encountered error 404. Measurement failed. Terminating connection.");
                connection.terminate();
                return;
            }
            System.out.println("All expected probes received.");
        }
    }
}
