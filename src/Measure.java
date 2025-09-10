public class Measure {

    public static void main(String[] args) {
        startMeasure(args);
    }

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
            Client.runEchoProgram(port, hostname);
        } else if (type.equals("server")){
            Server.runEchoProgram(port);
        } else {
            System.err.println("Argument -t must be either 'client' or 'server'.");
        }
    }

}
