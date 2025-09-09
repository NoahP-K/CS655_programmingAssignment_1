import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void runEchoProgram(String[] args) {
        String hostname = args[2];
        int portNumber;
        Scanner scanner = new Scanner(System.in);

        try {
            portNumber  = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println(
                    "You seem to have entered an invalid port number."
                            + "Please re-run this with a different port number."
            );
            return;
        }

        try (
                Socket echoSocket = new Socket(hostname, portNumber);
                PrintWriter out =
                        new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(echoSocket.getInputStream()));
        ){
            String userInput;
            System.out.println(
                    "Enter text to have it echoed by the server."
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
            e.printStackTrace();
        }
    }
}
