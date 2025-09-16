/*
This program enables communication between a client and a server.
Messages can be sent from the client. The user types into the client's console.
The text that the user types is sent to the server, displayed on the server's console.
Typing 'quit' terminates the program.
 */
public class Echo {

    public static void main(String[] args) {
        startEcho(args);
    }

    //parse command line args and call the client or server version of echo accordingly
    private static void startEcho(String[] args) {
        String type = "error";
        String portString = "error";
        String hostname = "error";
        int port;
        for(int i = 0; i < args.length; i++) {
            switch (args[i]) {
                //client or server
                case "-t":
                    type = args[++i];
                    break;
                //port number
                case "-p":
                    portString = args[++i];
                    break;
                //hostname
                case "-h":
                    hostname = args[++i];
                    break;
            }
        }
        //if the given port number is not an integer, end program.
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            System.err.println("Argument -p cannot be empty and must be valid port number.");
            return;
        }
        if(type.equals("client")){
            Client.runEchoProgram(port, hostname);
        } else if (type.equals("server")){
            Server.runEchoProgram(port);
        } else {
            //the type must be client or server
            System.err.println("Argument -t must be either 'client' or 'server'.");
        }
    }

}
