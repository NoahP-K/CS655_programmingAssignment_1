import java.io.IOException;
import java.util.Scanner;

public class Client {
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

    }

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


    }

    private static boolean connectionSetupPhase(
            ClientConnection connection,
            String measurement,
            int probeNum,
            int msgSizeBytes,
            int serverDelay
    ) {
        connection.out.println(
                "s %s %d %d %d\n".formatted(measurement, probeNum, msgSizeBytes, serverDelay)
        );
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
    private static boolean measurementPhase(
            ClientConnection connection,
            String measurement,
            int probeNum,
            int msgSizeBytes,
            int serverDelay
    ) {
        for(int i=0; i<probeNum; i++) {
            connection.out.println(
                    "w %d %s\n".format(i, )
            );
        }
    }
}
