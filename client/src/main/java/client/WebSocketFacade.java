package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.net.URI;

/**
 * Client-side WebSocket connection to the server's {@code /ws} endpoint. Sends
 * {@link UserGameCommand}s to the server and forwards every {@link ServerMessage}
 * it receives to a {@link ServerMessageObserver}.
 */
public class WebSocketFacade extends Endpoint {

    private final Gson gson = new Gson();
    private final ServerMessageObserver observer;
    private Session session;

    public WebSocketFacade(String serverUrl, ServerMessageObserver observer) throws ResponseException {
        this.observer = observer;
        try {
            URI uri = new URI(serverUrl.replaceFirst("^http", "ws") + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, ClientEndpointConfig.Builder.create().build(), uri);

            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    handle(message);
                }
            });
        } catch (Exception ex) {
            throw new ResponseException(500, "Unable to connect to game server: " + ex.getMessage());
        }
    }

    private void handle(String message) {
        ServerMessage base = gson.fromJson(message, ServerMessage.class);
        ServerMessage parsed = switch (base.getServerMessageType()) {
            case LOAD_GAME -> gson.fromJson(message, LoadGameMessage.class);
            case NOTIFICATION -> gson.fromJson(message, NotificationMessage.class);
            case ERROR -> gson.fromJson(message, ErrorMessage.class);
        };
        observer.notify(parsed);
    }

    public void send(UserGameCommand command) throws ResponseException {
        try {
            session.getBasicRemote().sendText(gson.toJson(command));
        } catch (Exception ex) {
            throw new ResponseException(500, "Failed to send command: " + ex.getMessage());
        }
    }

    public void close() throws ResponseException {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception ex) {
            throw new ResponseException(500, "Failed to close connection: " + ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        // No action needed on open; messages are handled by the registered MessageHandler.
    }
}
