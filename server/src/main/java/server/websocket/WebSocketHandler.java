package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.GameGson;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

public class WebSocketHandler {
    
    private final DataAccess dataAccess;
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = GameGson.create();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void onConnect(WsConnectContext ctx) {
        // To be filled in later
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx, command);
                case MAKE_MOVE -> makeMove(ctx, command);
                case LEAVE -> leave(ctx, command);
                case RESIGN -> resign(ctx, command);
            }
        } catch (Exception ex) {
            sendError(ctx, "Error: " + ex.getMessage());
        }
    }

    public void onClose(WsCloseContext ctx) {
        // To be filled in later
    }

    public void onError(WsErrorContext ctx) {
        // To be filled in later
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) throws Exception {
        // To be filled in later
    }

    private void makeMove(WsMessageContext ctx, UserGameCommand command) throws Exception {
        // To be filled in later
    }

    private void leave(WsMessageContext ctx, UserGameCommand command) throws Exception {
        // To be filled in later
    }

    private void resign(WsMessageContext ctx, UserGameCommand command) throws Exception {
        // To be filled in later
    }

    private void sendError(WsMessageContext ctx, String message) {
        ctx.send(gson.toJson(new ErrorMessage(message)));
    }

}
