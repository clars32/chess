package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.GameGson;
import model.AuthData;
import model.GameData;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

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

    private void connect(WsMessageContext ctx, UserGameCommand command) throws DataAccessException {
        
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: invalid auth token");
            return;
        }

        GameData game = dataAccess.getGame(command.getGameID());
        if (game == null) {
            sendError(ctx, "Error: game not found");
            return;
        }

        String username = auth.username();
        connections.add(game.gameID(), username, ctx);

        // LOAD_GAME goes only to the root client
        ctx.send(gson.toJson(new LoadGameMessage(game)));

        // Everyone else already in the game hears that this user joined
        String notification = "%s joined the game as %s.".formatted(username, connectRole(username, game));
        connections.broadcast(game.gameID(), username, gson.toJson(new NotificationMessage(notification)));

    }

    private String connectRole(String username, GameData game) {

        if (username.equals(game.whiteUsername())) {
            return "white";
        }
        if (username.equals(game.blackUsername())) {
            return "black";
        }
        return "an observer";
        
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
