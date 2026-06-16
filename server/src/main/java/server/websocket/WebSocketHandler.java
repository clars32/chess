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
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;

public class WebSocketHandler {
    
    private final DataAccess dataAccess;
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = GameGson.create();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void onConnect(WsConnectContext ctx) {
        // A client is registered with the game once it sends its CONNECT command
        // (handled in onMessage), since that is when its username and gameID are known.
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx, command);
                case MAKE_MOVE -> makeMove(ctx, gson.fromJson(ctx.message(), MakeMoveCommand.class));
                case LEAVE -> leave(ctx, command);
                case RESIGN -> resign(ctx, command);
            }
        } catch (Exception ex) {
            sendError(ctx, "Error: " + ex.getMessage());
        }
    }

    public void onClose(WsCloseContext ctx) {
        // Closed sessions are skipped by Connection.send (it checks isOpen), and a
        // game's connection is explicitly removed when the client sends LEAVE, so no
        // additional cleanup is required here.
    }

    public void onError(WsErrorContext ctx) {
        // Nothing actionable to do for a transport-level error.
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

    private void makeMove(WsMessageContext ctx, MakeMoveCommand command) throws DataAccessException {

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: invalid auth token");
            return;
        }

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            sendError(ctx, "Error: game not found");
            return;
        }

        String username = auth.username();
        ChessGame game = gameData.game();

        if (game.isGameOver()) {
            sendError(ctx, "Error: the game is already over");
            return;
        }

        ChessGame.TeamColor playerColor = colorOf(username, gameData);
        if (playerColor == null) {
            sendError(ctx, "Error: observers cannot make moves");
            return;
        }
        if (game.getTeamTurn() != playerColor) {
            sendError(ctx, "Error: it is not your turn");
            return;
        }

        try {
            game.makeMove(command.getMove());
        } catch (InvalidMoveException ex) {
            sendError(ctx, "Error: invalid move");
            return;
        }

        // Figure out end-of-game state before persisting, so gameOver gets saved
        ChessGame.TeamColor opponent = game.getTeamTurn();
        String opponentName = usernameForColor(gameData, opponent);
        String stateMessage = null;
        if (game.isInCheckmate(opponent)) {
            stateMessage = "%s is in checkmate.".formatted(opponentName);
            game.setGameOver(true);
        } else if (game.isInStalemate(opponent)) {
            stateMessage = "%s is in stalemate.".formatted(opponentName);
            game.setGameOver(true);
        } else if (game.isInCheck(opponent)) {
            stateMessage = "%s is in check.".formatted(opponentName);
        }

        dataAccess.updateGame(gameData);

        int gameID = gameData.gameID();

        // 1. Updated board to everyone, including the mover
        connections.broadcast(gameID, null, gson.toJson(new LoadGameMessage(gameData)));

        // 2. Move description to everyone except the mover
        String moveText = "%s moved %s".formatted(username, describeMove(command.getMove()));
        connections.broadcast(gameID, username, gson.toJson(new NotificationMessage(moveText)));

        // 3. Check / checkmate / stalemate to everyone
        if (stateMessage != null) {
            connections.broadcast(gameID, null, gson.toJson(new NotificationMessage(stateMessage)));
        }

    }

    private ChessGame.TeamColor colorOf(String username, GameData gameData) {
        if (username.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }
        if (username.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    private String usernameForColor(GameData gameData, ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.WHITE ? gameData.whiteUsername() : gameData.blackUsername();
    }

    private String describeMove(ChessMove move) {
        return positionName(move.getStartPosition()) + " to " + positionName(move.getEndPosition());
    }

    private String positionName(ChessPosition position) {
        char file = (char) ('a' + position.getColumn() - 1);
        return "%c%d".formatted(file, position.getRow());
    }

    private void leave(WsMessageContext ctx, UserGameCommand command) throws DataAccessException {

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: invalid auth token");
            return;
        }

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            sendError(ctx, "Error: game not found");
            return;
        }

        String username = auth.username();
        int gameID = gameData.gameID();

        // If a player is leaving, remove them from the game and persist the change
        ChessGame.TeamColor color = colorOf(username, gameData);
        if (color != null) {
            dataAccess.updateGame(removePlayer(gameData, color));
        }

        // Tell everyone else the root client left, then stop tracking this connection
        String notification = "%s left the game.".formatted(username);
        connections.broadcast(gameID, username, gson.toJson(new NotificationMessage(notification)));
        connections.remove(gameID, username);

    }

    private GameData removePlayer(GameData gameData, ChessGame.TeamColor color) {
        String white = color == ChessGame.TeamColor.WHITE ? null : gameData.whiteUsername();
        String black = color == ChessGame.TeamColor.BLACK ? null : gameData.blackUsername();
        return new GameData(gameData.gameID(), white, black, gameData.gameName(), gameData.game());
    }

    private void resign(WsMessageContext ctx, UserGameCommand command) throws DataAccessException {

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: invalid auth token");
            return;
        }

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            sendError(ctx, "Error: game not found");
            return;
        }

        String username = auth.username();
        ChessGame game = gameData.game();

        if (game.isGameOver()) {
            sendError(ctx, "Error: the game is already over");
            return;
        }

        if (colorOf(username, gameData) == null) {
            sendError(ctx, "Error: observers cannot resign");
            return;
        }

        game.setGameOver(true);
        dataAccess.updateGame(gameData);

        String notification = "%s resigned the game.".formatted(username);
        connections.broadcast(gameData.gameID(), null, gson.toJson(new NotificationMessage(notification)));

    }

    private static class MakeMoveCommand extends UserGameCommand {
        private ChessMove move;

        public ChessMove getMove() {
            return move;
        }
    }

    private void sendError(WsMessageContext ctx, String message) {
        ctx.send(gson.toJson(new ErrorMessage(message)));
    }

}
