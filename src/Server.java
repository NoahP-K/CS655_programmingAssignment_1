import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {
    public static void runEchoProgram(int port) {

        System.out.println("Server listening on port " + port + ".");

        //Identify local IP address (helpful for
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ipAddress = localHost.getHostAddress();
            System.out.println("Client can connect at " + ipAddress);
        } catch (UnknownHostException e) {
            System.out.println("Could not get local host address.");
            return;
        }
        try (
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
        ) {
            System.out.println("Client connected.");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                out.println(inputLine);
                if (inputLine.equals("quit")) {
                    System.out.println("Client has quit. Stopping server.");
                    break;
                }
                System.out.println("Echoing message: " + inputLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
