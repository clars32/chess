package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

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
    
}
