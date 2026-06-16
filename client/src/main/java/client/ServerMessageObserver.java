package client;

import websocket.messages.ServerMessage;

/**
 * Receives {@link ServerMessage}s that arrive asynchronously over the WebSocket
 * connection. Implemented by the gameplay UI so it can react to LOAD_GAME,
 * NOTIFICATION, and ERROR messages pushed by the server.
 */
public interface ServerMessageObserver {
    void notify(ServerMessage message);
}
