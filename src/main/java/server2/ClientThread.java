package server2;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private final Socket socket;

    private final MultiThreadedServer server;
    private final PrintWriter writer;
    @Getter
    private String nickname;


    private static final Logger logger = LogManager.getLogger(ClientThread.class);

    public ClientThread(Socket socket , MultiThreadedServer server) {
        this.socket = socket;
        this.server = server;
        try {
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            logger.error("Error setting up client handler: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try  {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println("Enter nickname: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String clientInput = in.readLine();
            this.nickname = clientInput;
            out.println("Connected to the chat\n");
            server.broadcastMessage(new ChatMessage(MessageType.JOIN, nickname, ""), this);
            while ((clientInput = in.readLine()) != null) {

                server.broadcastMessage(new ChatMessage(MessageType.CHAT, nickname, clientInput), this);
            }
            server.broadcastMessage(new ChatMessage(MessageType.LEAVE, nickname, ""), this);
            socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            server.removeClient(this);
        }
    }

    public void sendMessage(ChatMessage message) {
        writer.println(message.getType() + ": " + message.getUser() + ": " + message.getContent());
    }


}
