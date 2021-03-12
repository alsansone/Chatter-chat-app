package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread {

    private static final Logger auditLogger = Logger.getLogger("requests");
    private static final Logger errorLogger = Logger.getLogger("errors");
    private final int port;
    private final Set<ServerWorker> serverWorkers = Collections.synchronizedSet(new HashSet<>());

    private final Set<String> userNames = Collections.synchronizedSet(new HashSet<>());
    private final JDBC db = new JDBC();

    public Server(int port) {
        this.port = port;
    }

    public void run() {
        ExecutorService pool = Executors.newFixedThreadPool(50);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            auditLogger.info("Accepting connections on port: " + serverSocket.getLocalPort());
            db.runDB();
            auditLogger.info("Connected to database");
            while (true) {
                try {
                    if (serverWorkers.size() < 50) {
                        Socket clientSocket = serverSocket.accept();
                        auditLogger.info("Accepted connection from: " + clientSocket.getRemoteSocketAddress());
                        Runnable r = new ServerWorker(clientSocket, this);
                        pool.submit(r);
                    }
                } catch (IOException ex) {
                    errorLogger.log(Level.WARNING, "Error accepting connection", ex);
                }
            }
        } catch (IOException ex) {
            errorLogger.log(Level.SEVERE, "Server could not start", ex);
        } finally {
            try {
                auditLogger.info("Disconnected from chatter database");
                db.closeDB();
            } catch (SQLException ex) {
                errorLogger.log(Level.SEVERE, "Could not close the database connection", ex);
            }
        }
    }

    Set<ServerWorker> getServerWorkers() {
        return serverWorkers;
    }

    void addServerWorkers(ServerWorker worker) {
        this.serverWorkers.add(worker);
    }

    Set<String> getUserNames() {
        return userNames;
    }

    boolean isValidUser(String userName) {
        return db.isUser(userName) && !isLoggedIn(userName);
    }

    boolean isLoggedIn(String userName) {
        return userNames.contains(userName);
    }

    synchronized void addUserName(String userName) {
        userNames.add(userName);
        auditLogger.info(userName + " has joined the server");
    }

    synchronized void removeUser(ServerWorker serverWorker) {
        String userName = serverWorker.getUserName();
        if (serverWorkers.remove(serverWorker)) {
            if (userName != null) {
                userNames.remove(userName);
                auditLogger.info(userName + " has been removed from the list of online users");
                auditLogger.info(userName + " has left the server");
            }
        }
    }
}
