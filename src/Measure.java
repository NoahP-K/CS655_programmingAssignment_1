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
        int port;
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
            }
        }
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            System.err.println("Argument -p cannot be empty and must be valid port number.");
            return;
        }
        if(type.equals("client")){
            Client.runMeasureProgram(port, hostname); // needs server name/IP address to connect to server
        } else if (type.equals("server")){
            Server.runMeasureProgram(port); // does not need IP address; will generate and print for client to use
        } else {
            System.err.println("Argument -t must be either 'client' or 'server'.");
        }
    }

}
