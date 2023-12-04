package server2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class ChatMessage {
    private final MessageType type;
    private final String user;
    private final String content;
}

