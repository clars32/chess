package server.websocket;

import io.javalin.websocket.WsContext;

public class Connection {
    
    private final String username;
    private final WsContext wsContext;

    public Connection(String username, WsContext wsContext) {
        this.username = username;
        this.wsContext = wsContext;
    }

    public String username() {
        return username;
    }

    public void send(String message) {
        if (wsContext.session.isOpen()) {
            wsContext.send(message);
        }
    }

}
