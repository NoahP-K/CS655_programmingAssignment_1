import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void runEchoProgram(int port, String hostname) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Creating socket...");
        try (
                Socket echoSocket = new Socket(hostname, port);
                PrintWriter out =
                        new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(echoSocket.getInputStream()));
        ){
            String userInput;
            System.out.println(
                    "Connected to server " + hostname + ".\n"
                    + "Enter text to have it echoed by the server."
                    + "(Enter 'quit' to quit.)"
            );
            while(true){
                userInput = scanner.nextLine();
                out.println(userInput);
                if(userInput.equals("quit")){
                    break;
                }
                System.out.println("echo: " + in.readLine());
            }

        } catch (IOException e){
            System.err.println("Failure to create socket/connect to server. Stopping client.");
            e.printStackTrace();
        }
    }
}
