package client;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import ui.BoardRenderer;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static ui.EscapeSequences.*;

/**
 * Handles the in-game UI: gameplay commands typed by the user and the
 * asynchronous {@link ServerMessage}s pushed by the server while a game is open.
 */
public class GameplayClient implements ServerMessageObserver {

    private final String serverUrl;
    private final Repl repl;

    private WebSocketFacade ws;
    private String authToken;
    private int gameID;
    private TeamColor color;        // the player's color, or null if observing
    private TeamColor perspective;  // which side is drawn on the bottom
    private GameData gameData;       // most recent game state received from the server

    public GameplayClient(String serverUrl, Repl repl) {
        this.serverUrl = serverUrl;
        this.repl = repl;
    }

    /**
     * Opens a WebSocket connection, switches the REPL into gameplay mode, and sends
     * the CONNECT command. The board is drawn when the server replies with LOAD_GAME.
     *
     * @param color the player's color, or {@code null} to observe
     */
    public void connect(String authToken, int gameID, TeamColor color) throws ResponseException {
        this.authToken = authToken;
        this.gameID = gameID;
        this.color = color;
        this.perspective = (color == null) ? TeamColor.WHITE : color;
        this.gameData = null;

        ws = new WebSocketFacade(serverUrl, this);
        repl.enterGameplay();
        ws.send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public String eval(String input) {
        String[] tokens = input.split("\\s+");
        String command = tokens[0].toLowerCase(Locale.ROOT);

        try {
            return switch (command) {
                case "help" -> help();
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "move" -> move(tokens);
                case "resign" -> resign();
                case "highlight" -> highlight(tokens);
                default -> "Unknown command. Type 'help' for available commands.";
            };
        } catch (ResponseException e) {
            return SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR;
        }
    }

    private String help() {
        return """
                  help - show this message
                  redraw - redraw the chess board
                  move <FROM> <TO> [PROMOTION] - make a move, e.g. 'move e2 e4' or 'move e7 e8 q'
                  highlight <SQUARE> - show legal moves for the piece on a square, e.g. 'highlight e2'
                  resign - forfeit the game (the game ends, but you stay connected)
                  leave - leave the game and return to the main menu""";
    }

    private String redraw() {
        if (gameData == null) {
            return "The board has not loaded yet.";
        }
        BoardRenderer.drawBoard(gameData.game(), perspective);
        return "";
    }

    private String leave() throws ResponseException {
        ws.send(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
        ws.close();
        repl.leaveGameplay();
        return "You left the game.";
    }

    private String move(String[] tokens) throws ResponseException {
        if (color == null) {
            return "Observers cannot make moves.";
        }
        if (tokens.length < 3 || tokens.length > 4) {
            return "Usage: move <FROM> <TO> [PROMOTION]  (e.g. 'move e2 e4' or 'move e7 e8 q')";
        }

        ChessPosition start = parsePosition(tokens[1]);
        ChessPosition end = parsePosition(tokens[2]);
        if (start == null || end == null) {
            return "Squares must look like 'e2' (column a-h, row 1-8).";
        }

        ChessPiece.PieceType promotion = null;
        if (tokens.length == 4) {
            promotion = parsePromotion(tokens[3]);
            if (promotion == null) {
                return "Promotion piece must be one of: q, r, b, n.";
            }
        }

        ChessMove chessMove = new ChessMove(start, end, promotion);
        ws.send(new MakeMoveCommand(authToken, gameID, chessMove));
        return "";
    }

    private String resign() throws ResponseException {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        Scanner scanner = repl.scanner();
        String answer = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        if (!answer.equals("yes") && !answer.equals("y")) {
            return "Resignation cancelled.";
        }
        ws.send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
        return "";
    }

    private String highlight(String[] tokens) {
        if (gameData == null) {
            return "The board has not loaded yet.";
        }
        if (tokens.length != 2) {
            return "Usage: highlight <SQUARE>  (e.g. 'highlight e2')";
        }

        ChessPosition position = parsePosition(tokens[1]);
        if (position == null) {
            return "Square must look like 'e2' (column a-h, row 1-8).";
        }

        ChessGame game = gameData.game();
        if (game.getBoard().getPiece(position) == null) {
            return "There is no piece on " + tokens[1].toLowerCase(Locale.ROOT) + ".";
        }

        Collection<ChessMove> legalMoves = game.validMoves(position);
        List<ChessPosition> destinations = new ArrayList<>();
        for (ChessMove legalMove : legalMoves) {
            destinations.add(legalMove.getEndPosition());
        }

        BoardRenderer.drawBoard(game, perspective, position, destinations);
        return "";
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> loadGame((LoadGameMessage) message);
            case NOTIFICATION -> printAsync(SET_TEXT_COLOR_BLUE, ((NotificationMessage) message).getMessage());
            case ERROR -> printAsync(SET_TEXT_COLOR_RED, ((ErrorMessage) message).getErrorMessage());
        }
    }

    private void loadGame(LoadGameMessage message) {
        gameData = message.getGame();
        System.out.println();
        BoardRenderer.drawBoard(gameData.game(), perspective);
        repl.printPrompt();
    }

    private void printAsync(String textColor, String text) {
        System.out.println();
        System.out.println(textColor + text + RESET_TEXT_COLOR);
        repl.printPrompt();
    }

    private ChessPosition parsePosition(String square) {
        if (square.length() != 2) {
            return null;
        }
        char file = Character.toLowerCase(square.charAt(0));
        char rank = square.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }
        int col = file - 'a' + 1;
        int row = rank - '0';
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String piece) {
        return switch (piece.toLowerCase(Locale.ROOT)) {
            case "q", "queen" -> ChessPiece.PieceType.QUEEN;
            case "r", "rook" -> ChessPiece.PieceType.ROOK;
            case "b", "bishop" -> ChessPiece.PieceType.BISHOP;
            case "n", "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }
}
