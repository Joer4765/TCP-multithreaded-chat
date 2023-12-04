package server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ChatServer server;
    private PrintWriter writer;

    public ClientHandler(Socket clientSocket, ChatServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        try {
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message;
            ObjectMapper objectMapper = new ObjectMapper();

            while ((message = reader.readLine()) != null) {
                System.out.println("Received message: " + message);

                JsonNode jsonMessage = objectMapper.readTree(message);
                String messageType = jsonMessage.get("type").asText();

                switch (messageType) {
                    case "chat":
                        String sender = jsonMessage.get("sender").asText();
                        String chatMessage = jsonMessage.get("message").asText();
                        // Логіка обробки повідомлення чату
                        server.broadcastMessage("[" + sender + "]: " + chatMessage, this);
                        break;

                    case "system":
                        String systemMessage = jsonMessage.get("message").asText();
                        // Логіка обробки системного повідомлення
                        System.out.println("System message: " + systemMessage);
                        break;

                    case "user_status":
                        String username = jsonMessage.get("username").asText();
                        String status = jsonMessage.get("status").asText();
                        // Логіка обробки повідомлення про приєднання/вихід користувача
                        System.out.println(username + " has " + status);
                        break;

                    default:
                        System.out.println("Unknown message type: " + messageType);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            server.removeClient(this);
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
}
