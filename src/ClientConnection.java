import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// This class represents the client side of the network connection between client and server.
public class ClientConnection {
    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;

    // Establishes network connection from the client side
    // Creates a new socket to send to the server
    // Connects the out to the in so messages can be transferred
    public ClientConnection(String hostname, int port)  throws IOException {
        socket = new Socket(hostname, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Severs the network connection from the client side
    public void terminate(){
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket.");
        }
    }
}