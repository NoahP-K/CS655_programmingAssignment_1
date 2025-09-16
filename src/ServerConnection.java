import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// This class represents the server side of the network connection between client and server.
public class ServerConnection {
    public ServerSocket serverSocket;
    public Socket clientSocket;
    public BufferedReader in;
    public PrintWriter out;

    // Establishes network connection from the server side
    // Connects the out to the in so that messages can be transferred
    public ServerConnection(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    // Severs the network connection from the server side
    public void terminate(){
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket.");
        }
    }
}