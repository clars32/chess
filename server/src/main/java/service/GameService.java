package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;

public class GameService {
    
    private final DataAccess dataAccess;
    private final AuthService authService;

    public GameService(DataAccess dataAccess, AuthService authService) {

        this.dataAccess = dataAccess;
        this.authService = authService;

    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {

        authService.verify(authToken);
        return new ListGamesResult(dataAccess.listGames());

    }
    
    public CreateGameResult createGame(String authToken, CreateGameRequest req) throws DataAccessException {

        authService.verify(authToken);

        if (req == null || req.gameName() == null) {
            throw new BadRequestException("bad request");
        }

        GameData newGame = new GameData(0, null, null, req.gameName(), new ChessGame());
        int gameID = dataAccess.createGame(newGame);

        return new CreateGameResult(gameID);

    }
}
