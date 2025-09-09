public class Main {

    public static void main(String[] args) {
        if (args[0].equals("C")) {
            Client.runEchoProgram(args);
        } else if (args[0].equals("S")) {
            Server.runEchoProgram(args);
        }
    }

}
