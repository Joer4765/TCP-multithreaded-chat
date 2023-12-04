package server2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MultiThreadedServer {
    static boolean stop = false;
    private static final List<ClientThread> clients = new ArrayList<>();

    private static final Logger logger = LogManager.getLogger(MultiThreadedServer.class);

    public static void main(String[] args) {
        MultiThreadedServer server = new MultiThreadedServer();
        String configFilename = "src/main/java/server2/config.properties";
        Properties properties = readConfiguration(configFilename);
        server.start(Integer.parseInt(properties.getProperty("port")));
    }

    public static Properties readConfiguration(String filename) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filename));
        } catch (IOException e) {
            System.out.println("Error! " + e.getMessage());
        }
        return properties;
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port " + port);
//            System.out.println("Server started on port " + port);
            while (!stop) {
//                System.out.println("Waiting for clients...");
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected: " + clientSocket.getInetAddress().getHostAddress());
//                System.out.println("Client connected!");

                ClientThread clientThread = new ClientThread(clientSocket, this);
                clients.add(clientThread);
                clientThread.start();
            }
        } catch (IOException e) {
            logger.error("Error in the server: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void broadcastMessage(ChatMessage message, ClientThread sender) {
        for (ClientThread client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientThread client) {
        clients.remove(client);
    }
}
