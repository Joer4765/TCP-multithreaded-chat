package client;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.Properties;

@AllArgsConstructor
public class ChatClient {

    private InetAddress serverHost;
    private int serverPort;
    private static final Logger logger = LogManager.getLogger(ChatClient.class);
//    private String username;

    public static Properties readConfiguration(String filename) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filename));
        } catch (IOException e) {
            System.out.println("Error! " + e.getMessage());
        }
        return properties;
    }

    public static void main(String[] args) {
        String configFilename = "src/main/java/server2/config.properties";
        Properties properties = readConfiguration(configFilename);
        try {
            InetAddress serverAddress = InetAddress.getByName(properties.getProperty("host"));
            ChatClient client = new ChatClient(serverAddress, Integer.parseInt(properties.getProperty("port")));
            client.start();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            Socket socket = new Socket(serverHost, serverPort);

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            logger.info("Connected to the server");

            System.out.println("Connected to the server. Type 'exit' to quit.");

            // потік для читання повідомлень з сервера
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = reader.readLine()) != null) {
                        System.out.println(serverMessage);
                        if (serverMessage.startsWith("CHAT")) {
                            System.out.println(serverMessage);
                        } else if (serverMessage.startsWith("SYSTEM")) {
                            System.out.println(serverMessage);
                        } else if (serverMessage.startsWith("JOIN")) {
                            String joiningUser = serverMessage.split(": ")[1];
                            System.out.println(joiningUser + " joined the chat.");
                        } else if (serverMessage.startsWith("LEAVE")) {
                            String leavingUser = serverMessage.split(": ")[1];
                            System.out.println(leavingUser + " left the chat.");
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error reading messages from the server: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }).start();

            // Введення та надсилання повідомлень на сервер
            String clientMessage;
            while (!(clientMessage = consoleReader.readLine()).equalsIgnoreCase("exit")) {
                writer.println(clientMessage);
            }

            socket.close();
        } catch (IOException e) {
            logger.error("Error in the client: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}