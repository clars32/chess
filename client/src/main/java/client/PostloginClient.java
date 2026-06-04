package client;

import chess.ChessGame;
import model.GameData;
import ui.BoardRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PostloginClient {
    
    private final ServerFacade facade;
    private final Repl repl;

    // Shared with PreloginClient - set on login/register
    static String authToken;

    // Cached game list so user can reference games by number
    private List<GameData> lastGameList = new ArrayList<>();

    public PostloginClient(ServerFacade facade, Repl repl) {
        this.facade = facade;
        this.repl = repl;
    }

    public String eval(String input) throws ResponseException {
        
        String[] tokens = input.split("\\s+");
        String command = tokens[0].toLowerCase();

        return switch (command) {
            case "help" -> help();
            case "logout" -> logout();
            case "create" -> createGame(tokens);
            case "list" -> listGames();
            case "play" -> playGame(tokens);
            case "observe" -> observeGame(tokens);
            default -> "Unknown command. Type 'help' for available commands.";
        };
    }

    private String help() {
        return """
                  create <NAME> - create a new game
                  list - list all games
                  play <GAME_NUMBER> <WHITE|BLACK> - join a game as a player
                  observe <GAME_NUMBER> - observe a game
                  logout - log out
                  help - show this message""";
    }

    private String logout() throws ResponseException {
        facade.logout(authToken);
        authToken = null;
        repl.setLoggedIn(false);
        return "Logged out.";
    }

    private String createGame(String[] tokens) throws ResponseException {
        if (tokens.length != 2) {
            return "Usage: create <NAME>";
        }

        facade.createGame(authToken, tokens[1]);
        return "Created game '" + tokens[1] + "'.";
    }

    private String listGames() throws ResponseException {
        Collection<GameData> games = facade.listGames(authToken);
        lastGameList = new ArrayList<>(games);

        if (lastGameList.isEmpty()) {
            return "No games found.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lastGameList.size(); i++) {
            GameData game = lastGameList.get(i);
            String white = game.whiteUsername() != null ? game.whiteUsername() : "(open)";
            String black = game.blackUsername() != null ? game.blackUsername() : "(open)";
            sb.append(String.format("  %d. %s - White: %s, Black: %s%n",
                    i + 1, game.gameName(), white, black));
        }
        return sb.toString().stripTrailing();
    }

    private String playGame(String[] tokens) throws ResponseException {
        if (tokens.length != 3) {
            return "Usage: play <GAME_NUMBER> <WHITE|BLACK>";
        }

        int gameNumber;
        try {
            gameNumber = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            return "Game number must be a number.";
        }

        if (gameNumber < 1 || gameNumber > lastGameList.size()) {
            return "Invalid game number. Use 'list' to see available games.";
        }

        String color = tokens[2].toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Color must be WHITE or BLACK.";
        }

        GameData gameData = lastGameList.get(gameNumber - 1);
        facade.joinGame(authToken, color, gameData.gameID());

        ChessGame.TeamColor perspective = color.equals("WHITE")
                ? ChessGame.TeamColor.WHITE
                : ChessGame.TeamColor.BLACK;
        BoardRenderer.drawBoard(gameData.game(), perspective);
        return "Joined game '" + gameData.gameName() + "'as " + color + ".";
    }

    private String observeGame(String[] tokens) {
        if (tokens.length != 2) {
            return "Usage: observe <GAME_NUMBER>";
        }

        int gameNumber;
        try {
            gameNumber = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            return "Game number must be a number.";
        }

        if (gameNumber < 1 || gameNumber > lastGameList.size()) {
            return "Invalid game number. Use 'list' to see available games.";
        }

        GameData gameData = lastGameList.get(gameNumber - 1);
        BoardRenderer.drawBoard(gameData.game(), ChessGame.TeamColor.WHITE);
        return "Observing game '" + gameData.gameName() + "''.";
    }
    
}
