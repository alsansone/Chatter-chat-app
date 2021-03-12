package server;

import java.io.*;
import java.net.Socket;
import java.util.Set;

public class ServerWorker implements Runnable {

    private final Socket userSocket;
    private final Server server;
    private String userName;
    private OutputStream out;
    private BufferedReader reader;

    public ServerWorker(Socket socket, Server server) {
        this.userSocket = socket;
        this.server = server;
        server.addServerWorkers(this);
    }

    @Override
    public void run() {
        try {
            this.reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            this.out = userSocket.getOutputStream();
            this.out.write("*** Welcome to Chatter ***\n".getBytes());
            this.out.write("Type CMD for a list of commands\n".getBytes());

            boolean isValidUser = handleLogin();

            if (isValidUser) {
                broadcast("Chatter: " + getUserName() + " has joined the chat\n");
                handleInput();
            }

        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                this.userSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private boolean handleLogin() {
        boolean isValidName = false;
        int count = 2;
        String name = null;
        do {
            try {
                this.out.write("Enter your name: ".getBytes());
                name = reader.readLine();
                isValidName = server.isValidUser(name);
                if (!isValidName) {
                    if (count == 0) {
                        this.out.write("Chatter: No more attempts left. Goodbye\n".getBytes());
                        removeUser();
                        break;
                    }
                    this.out.write("Chatter: Invalid Name\n".getBytes());
                    this.out.write(("Chatter: " + count + " attempts left\n").getBytes());
                    count--;
                }
            } catch (IOException ex) {
                removeUser();
            }
        } while (!isValidName && count >= 0);

        if (isValidName) {
            setUserName(name);
            return true;
        }
        return false;
    }

    private void handleInput() throws IOException {
        String line;
        String[] tokens;
        while ((line = reader.readLine()) != null) {
            tokens = line.split(" ");
            String cmd = tokens[0];
            if ("logout".equalsIgnoreCase(cmd)) {
                removeUser();
                broadcast("Chatter: " + getUserName() + " has left the chat\n");
                break;
            } else if ("private".equalsIgnoreCase(cmd)) {
                String[] message = line.split(" ", 3);
                if (message.length > 1) {
                    sendPrivate(message);
                } else {
                    this.out.write("Usage: private <user> <message>\n".getBytes());
                }
            } else if ("users".equalsIgnoreCase(cmd)) {
                send("Online Users\n" + getOnlineUsers());
            } else if ("cmd".equalsIgnoreCase(cmd)) {
                this.out.write("logout: leave the chat\nusers: get a list of online users\nprivate: send a private message\n".getBytes());
            } else if (!line.isBlank()) {
                broadcast(getUserName() + ": " + line + "\n");
            }
        }
    }

    private String getOnlineUsers() {
        StringBuilder users = new StringBuilder();
        for (String user: server.getUserNames()) {
            users.append(user).append("\n");
        }
        return users.toString();
    }

    private void sendPrivate(String[] message) {
        String recipient = message[1];
        String body = message[2];
        ServerWorker worker = isUser(recipient);
        if (worker != null) {
            worker.send("<Private> " + getUserName() + ": " + body + "\n");
        } else {
            send("Chatter: " + recipient + " is not in chat\n");
        }
    }

    private void broadcast(String message) {
        Set<ServerWorker> serverWorkers = server.getServerWorkers();
        for (ServerWorker worker: serverWorkers) {
            if (worker.getUserName() != null) {
                worker.send(message);
            }
        }
    }

    private void send(String message) {
        try {
            this.out.write(message.getBytes());
        } catch (IOException e) {
            System.out.println("Error writing to stream " + e.getMessage());
        }
    }

    public String getUserName() {
        return this.userName;
    }

    private void setUserName(String userName) {
        this.userName = userName;
        server.addUserName(userName);
    }

    private void removeUser() {
        server.removeUser(this);
    }

    private ServerWorker isUser(String name) {
        for (ServerWorker worker: server.getServerWorkers()) {
            if (worker.getUserName().equalsIgnoreCase(name))
                return worker;
        }
        return null;
    }
}
