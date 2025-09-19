/*
This program measures the round-trip time (RTT) and throughput
of requests sent from the client to the server.
It starts the connection between the two hosts, and then uses test values to observe the resulting values.
 */
public class Measure {

    public static void main(String[] args) {
        startMeasure(args);
    }

    // Starts up the measurement routine, including connection setup
    // Performs preprocessing of the necessary arguments
    // Determines from these arguments whether the desired program to run is a client or server program
    private static void startMeasure(String[] args) {
        String type = "error";
        String portString = "error";
        String hostname = "error";
        String probeNumString = "";
        String serverDelayString = "";
        int port;
        int probeNum = 10;
        int serverDelay = 0;
        for(int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-t":
                    type = args[++i];
                    break;
                case "-p":
                    portString = args[++i];
                    break;
                case "-h":
                    hostname = args[++i];
                    break;
                case "-pn":
                    probeNumString = args[++i];
                    break;
                case "-sd":
                    serverDelayString = args[++i];
                    break;
                default:
                    System.err.println("Unknown option: " + args[i]);
                    return;
            }
        }
        try {
            port = Integer.parseInt(portString);
            if(!probeNumString.isEmpty()) {
                probeNum = Integer.parseInt(probeNumString);
                if(probeNum <= 0) { throw new NumberFormatException(); }
            }
            if(!serverDelayString.isEmpty()) {
                serverDelay = Integer.parseInt(serverDelayString);
                if(serverDelay < 0) { throw new NumberFormatException(); }
            }
        } catch (NumberFormatException e) {
            System.err.println(
                    "Invalid argument passed. Note:\n"
                    + "- Argument -p cannot be empty and must be valid port number.\n"
                    + "- Argument -pr is optional but must be a positive integer.\n"
                    + "- Argument -sd is optional but must be a nonnegative integer."
            );
            return;
        }
        if(type.equals("client")){
            Client.runMeasureProgram(port, hostname, probeNum, serverDelay); // needs server name/IP address to connect to server
        } else if (type.equals("server")){
            Server.runMeasureProgram(port); // does not need IP address; will generate and print for client to use
        } else {
            System.err.println("Argument -t must be either 'client' or 'server'.");
        }
    }

}
