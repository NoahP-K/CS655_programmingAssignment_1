import java.io.IOException;
import java.util.Scanner;
import java.nio.file.Paths;
import java.nio.file.Files;

//Class to hold functions for running the client portions of the assignment
public class Client {

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
    public static void runMeasureProgram(int port, String hostname, int probeNum, int serverDelay) {
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
            Client.MeasurementResults results = new MeasurementResults(probeNum, "rtt", rttMsgSizes[i]);
            //CSP
            boolean didSetUp = connectionSetupPhase(connection, results, serverDelay);
            if(!didSetUp) {
                System.err.println("Setup failed for "
                        + results.type + " test on "
                        + results.msgSize + " byte payload. "
                        + "Terminating."
                );
                return;
            }
            //MP
            //some printing for easy reading
            System.out.println(
                    "STARTING " + results.type.toUpperCase() + " TESTS\n"
            );
            boolean didMeasure = measurementPhase(connection, rttFiles[i], results);
            if(!didMeasure) {
                System.err.println("Measurement failed for "
                        + results.type + " test on "
                        + results.msgSize + " byte payload. "
                        + "Terminating."
                );
                return;
            }
            double avgResult = 0;
            for(long measurement: results.measurements) {avgResult += measurement;}
            avgResult /= (results.probeNum - results.failedProbes);
            System.out.println("Average RTT for " + results.msgSize + " byte payload: " + avgResult);
            //some printing for easy reading
            System.out.println(
                    "END OF " + results.type.toUpperCase() + " TESTS"
                            + "\n================================================="
            );
        }

        //Run TPUT measurements
        for(int i=0; i<tputFiles.length; i++) {
            Client.MeasurementResults results = new MeasurementResults(probeNum, "tput", tputMsgSizes[i]);
            //CSP
            boolean didSetUp = connectionSetupPhase(connection, results, serverDelay);
            if(!didSetUp) {
                System.err.println("Setup failed for "
                        + results.type + " test on "
                        + results.msgSize + " byte payload. "
                        + "Terminating."
                );
                return;
            }
            //MP
            //some printing for easy reading
            System.out.println(
                    "STARTING " + results.type.toUpperCase() + " TESTS\n"
            );
            boolean didMeasure = measurementPhase(connection, tputFiles[i], results);
            if(!didMeasure) {
                System.err.println("Measurement failed for "
                        + results.type + " test on "
                        + results.msgSize + " byte payload. "
                        + "Terminating."
                );
                return;
            }
            double avgResult = 0;
            for(long measurement: results.measurements) {avgResult += measurement;}
            avgResult /= (results.probeNum - results.failedProbes);
            System.out.println("Average TPUT for " + results.msgSize + " byte payload: " + avgResult);
            //some printing for easy reading
            System.out.println(
                    "END OF " + results.type.toUpperCase() + " TESTS"
                            + "\n================================================="
            );
        }

        //CTP
        connectionTerminationPhase(connection);
    }

    //perform the connection termination phase
    private static void connectionTerminationPhase(ClientConnection connection) {
        //send termination message
        System.out.println("Sending server termination message.");
        connection.out.println("t");
        try {
            String response = connection.in.readLine();
            if(!response.contains("200")) {
                throw new Exception("Did not receive 200 OK response.");
            } else {
                System.out.println("Received OK. Terminating without error.");
            }
        } catch (IOException e) {
            System.err.println("Problem receiving termination response. Stopping client.");
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
                "s " + results.type + " " + results.probeNum + " "
                + results.msgSize + " " + serverDelay
        );
        try {
            String response = connection.in.readLine();
            if (response.equals("200 OK: Ready")) {
                return true;
            } else {
                throw new Exception("Invalid response received: " + response);
            }
        } catch (IOException e) {
            System.err.println("Error reading message.");
            return false;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }

    }

    //perform measurement phase
    private static boolean measurementPhase(
            ClientConnection connection,
            String file,
            Client.MeasurementResults results
    ) {
        //Send a number of messages containing the file contents
        String fileContents;
        try {
            fileContents = new String(Files.readAllBytes(Paths.get(String.format("../data/%s/%s", results.type, file))));
        } catch (IOException e) {
            System.err.println("Failed to read file " + file);
            return false;
        }
        for(int i=0; i<results.probeNum; i++) {
            System.out.println((fileContents.length()<15)
                    ? "Sending probe: m " + i + " " + fileContents
                    : "Sending probe: m " + i + " " + fileContents.substring(0, 15) + "..."
            );
            long start = System.nanoTime();
            connection.out.println("m " + i + " " + fileContents);
            try {
                String response = connection.in.readLine();
                long end = System.nanoTime();
                long timeElapsed = end - start;
                //if an error is echoed instead of the probe measurement, stop measuring
                if(response.contains("404")){
                    System.err.println("Received 404 error in response.");
                    return false;
                }
                //for the sake of not clogging the terminal, cut off the echo print after 20 characters
                System.out.println((response.length()<20)
                                ? "Response: " + response
                                : "Response: " + response.substring(0, 20) + "..."
                );
                System.out.println("Time elapsed: " + timeElapsed);
                //Note that timeElapsed is in nanoseconds
                if(results.type.equals("rtt")) {results.measurements[i] = timeElapsed;}
                else if(results.type.equals("tput")) {
                    //time is in nanoseconds, size is in bytes, result is megabytes per second
                    double MBps = (double) (results.msgSize * 1000) / timeElapsed;
                    System.out.println("Throughput: " + MBps);
                    results.measurements[i] = (long) MBps;
                }
                //some printing for easy reading
                System.out.println(
                        "--------------------------------------------------"
                );
            } catch (IOException e) {
                System.err.println("Error receiving message.");
                results.failedProbes++;
            }
        }
        return true;
    }
}
