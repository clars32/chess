package server.websocket;

import io.javalin.websocket.WsContext;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    
    // gameID -> (username -> Connection)
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections =
        new ConcurrentHashMap<>();
    
    public void add(int gameID, String username, WsContext wsContext) {
        connections.computeIfAbsent(gameID, key -> new ConcurrentHashMap<>())
            .put(username, new Connection(username, wsContext));
    }

    public void remove(int gameID, String username) {
        ConcurrentHashMap<String, Connection> gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            gameConnections.remove(username);
            if (gameConnections.isEmpty()) {
                connections.remove(gameID);
            }
        }
    }

    public void broadcast(int gameID, String excludeUsername, String message) {
        ConcurrentHashMap<String, Connection> gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }
        for (Connection connection : gameConnections.values()) {
            if (!connection.username().equals(excludeUsername)) {
                connection.send(message);
            }
        }
    }
    
}
