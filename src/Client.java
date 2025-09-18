import java.io.IOException;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

//Class to hold functions for running the client portions of the assignment
public class Client {
    static int probeNum = 10;   //number of probes sent per measurement+payload size

    //run the client side of the echo program
    public static void runEchoProgram(int port, String hostname) {
        Scanner scanner = new Scanner(System.in);
        ClientConnection connection;
        try {
            System.out.println("Connecting to server " + hostname + "...");
            connection = new ClientConnection(hostname, port);
        } catch (IOException e) {
            System.err.println("Failed to connect to server " + hostname + ". Stopping client.");
            return;
        }

        System.out.println(
                        "Connected to server " + hostname + " on port: " + port + ".\n"
                        + "Enter text to have it echoed by the server.\n"
                        + "(Enter 'quit' to quit.)"
        );
        String userInput;
        while (true) {
            userInput = scanner.nextLine();
            connection.out.println(userInput);
            if (userInput.equals("quit")) {
                break;
            }
            try {
                System.out.println("echo: " + connection.in.readLine());
            } catch (IOException e) {
                System.err.println("Failed to echo.");
            }
        }
        //close the connection
        connection.terminate();
    }

    //inner class to store measurement results during MP
    static class MeasurementResults {
        String type;    //rtt or tput
        int msgSize;    //num. bytes in payload
        int probeNum;
        long[] measurements;     //collection of all measurements for this type+size
        int failedProbes;   //number of probes that do not get responses

        public MeasurementResults(int numMeasurements, String type, int msgSize) {
            measurements = new long[numMeasurements];
            probeNum = numMeasurements;
            this.type = type;
            this.msgSize = msgSize;
            failedProbes = 0;
        }
    }

    //run the client side of the measure program
    public static void runMeasureProgram(int port, String hostname) {
        ClientConnection connection;
        try {
            System.out.println("Connecting to server " + hostname + "...");
            connection = new ClientConnection(hostname, port);
        } catch (IOException e) {
            System.err.println("Failed to connect to server " + hostname + ". Stopping client.");
            return;
        }

        System.out.println(
                "Connected to server " + hostname + " on port: " + port + "."
        );

        //For each measurement+payload size, send a connection setup and then take the measurement
        //I know the hardcoding here isn't ideal, but it works fine for this assignment
        int[] rttMsgSizes = {1, 100, 200, 400, 800, 1000};
        String[] rttFiles = {"1byte", "100byte", "200byte", "400byte", "800byte", "1000byte"};
        int[] tputMsgSizes = {1000, 2000, 4000, 8000, 16000, 32000};
        String[] tputFiles = {"1Kbyte", "2Kbyte", "4Kbyte", "8Kbyte", "16Kbyte", "32Kbyte"};

        //Run RTT measurements
        for(int i=0; i<rttFiles.length; i++) {
            Client.MeasurementResults results = new MeasurementResults(Client.probeNum, "rtt", rttMsgSizes[i]);
            //CSP
            boolean didSetUp = connectionSetupPhase(connection, results, 0);
            if(!didSetUp) {
                System.err.println("Setup failed. Skipping "
                        + results.type + " test on "
                        + results.msgSize + " byte payload.");
                continue;
            }
            //MP
            boolean didMeasure = measurementPhase(connection, 0, rttFiles[i], results);
            if(!didMeasure) {
                System.err.println("Measurement failed. Skipping."
                        + results.type + " test on "
                        + results.probeNum + " byte payload.");
            }
            double avgResult = 0;
            for(long measurement: results.measurements) {avgResult += measurement;}
            avgResult /= (results.probeNum - results.failedProbes);
            System.out.println("Average RTT for " + results.msgSize + " byte payload: " + avgResult);
        }
        //CTP
        connectionTerminationPhase(connection);
    }

    //perform the connection termination phase
    private static void connectionTerminationPhase(ClientConnection connection) {
        //send termination message
        connection.out.println("t\n");
        while(true) {
            try {
                connection.in.readLine();
                break;
            } catch (IOException e) {
                System.err.println("Problem receiving termination response. Stopping client.");
                break;
            }
        }
        //disconnect client
        connection.terminate();
    }

    //perform the connection setup phase
    private static boolean connectionSetupPhase(
            ClientConnection connection,
            Client.MeasurementResults results,
            int serverDelay
    ) {
        //send CSP message to server
        connection.out.println(
               String.format("s %s %d %d %d\n", results.type, results.probeNum, results.msgSize, serverDelay));
        while(true) {
            try {
                if (connection.in.readLine().equals("200 OK: Ready")) {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }
    }

    //perform measurement phase
    private static boolean measurementPhase(
            ClientConnection connection,
            int serverDelay,
            String file,
            Client.MeasurementResults results
    ) {
        //Send a number of messages containing the file contents
        String fileContents;
        try {
            fileContents = new String(Files.readAllBytes(Paths.get(String.format("../data/%s/%s", results.type, file))));
        } catch (IOException e) {
            System.err.println("Failed to read file " + file + ". Skipping this measurement phase.");
            return false;
        }
        for(int i=0; i<results.probeNum; i++) {
            long start = System.nanoTime();
            connection.out.println(String.format("m %d %s\n", i, fileContents));
            try {
                String response = connection.in.readLine();
                long end = System.nanoTime();
                long timeElapsed = end - start;
                System.out.println((response.length()<10)
                                ? "Echo: " + response
                                : "Echo: " + response.substring(0, 10) + "..."
                );
                System.out.println("Time elapsed: " + timeElapsed);
                //Note that timeElapsed is in nanoseconds
                if(results.type == "rtt") {results.measurements[i] = timeElapsed;}
                else if(results.type == "tput") {
                    //time is in nanoseconds, size is in bytes, result is megabytes per second
                    double MBps = results.msgSize/timeElapsed * Math.pow(10, 3);
                    results.measurements[i] = (long) MBps;
                }
            } catch (IOException e) {
                System.err.println("Failed to echo.");
                results.failedProbes++;
            }
        }
        return true;
    }
}
