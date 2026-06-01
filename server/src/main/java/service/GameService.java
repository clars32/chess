package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

public class GameService {
    
    private final DataAccess dataAccess;
    private final AuthService authService;

    public GameService(DataAccess dataAccess, AuthService authService) {

        this.dataAccess = dataAccess;
        this.authService = authService;

    }

    // listGames()
    public ListGamesResult listGames(String authToken) throws DataAccessException {

        authService.verify(authToken);
        return new ListGamesResult(dataAccess.listGames());

    }
    
    // createGame()
    public CreateGameResult createGame(String authToken, CreateGameRequest req) throws DataAccessException {

        authService.verify(authToken);

        if (req == null || req.gameName() == null) {
            throw new BadRequestException("bad request");
        }

        GameData newGame = new GameData(0, null, null, req.gameName(), new ChessGame());
        int gameID = dataAccess.createGame(newGame);

        return new CreateGameResult(gameID);

    }

    // joinGame()
    public void joinGame(String authToken, JoinGameRequest req) throws DataAccessException {

        AuthData auth = authService.verify(authToken);

        if (req == null || req.playerColor() == null || (!req.playerColor().equals("WHITE") && !req.playerColor().equals("BLACK"))) {
            throw new BadRequestException("bad request");
        }

        GameData game = dataAccess.getGame(req.gameID());
        if (game == null) {
            throw new BadRequestException("bad request");
        }

        String username = auth.username();
        GameData updated;

        if (req.playerColor().equals("WHITE")) {

            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException("already taken");
            }

            updated = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());

        } else {

            if (game.blackUsername() != null) {
                throw new AlreadyTakenException("already taken");
            }

            updated = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());

        }

        dataAccess.updateGame(updated);

    }

}
