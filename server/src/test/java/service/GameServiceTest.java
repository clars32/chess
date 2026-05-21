package service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import chess.ChessGame;
import dataaccess.*;
import model.GameData;

public class GameServiceTest {
    
    private DataAccess dao;
    private UserService userService;
    private AuthService authService;
    private GameService gameService;
    private String token;

    @BeforeEach
    void setup() throws DataAccessException {

        dao = new MemoryDataAccess();
        userService = new UserService(dao);
        authService = new AuthService(dao);
        gameService = new GameService(dao, authService);
        token = userService.register(
            new RegisterRequest("carter", "pass", "test@test.com")).authToken();
    
    }

    @Test
    void listGamesSuccess() throws DataAccessException {

        dao.createGame(new GameData(0, null, null, "test", new ChessGame()));

        ListGamesResult result = gameService.listGames(token);

        assertEquals(1, result.games().size());

    }

    @Test
    void listGamesUnauthorizedThrows() {
        assertThrows(UnauthorizedException.class, () ->
            gameService.listGames("bad-token"));
    }
    
}
