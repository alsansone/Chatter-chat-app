package server;

public class ServerMain {

    private static final int PORT = 8191;

    public static void main(String[] args) {
        Server server = new Server(PORT);
        server.start();
    }
}
