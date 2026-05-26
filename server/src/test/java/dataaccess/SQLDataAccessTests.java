package dataaccess;

import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLDataAccessTests {
    
    private static SQLDataAccess db;

    @BeforeAll
    static void setup() throws DataAccessException {
        db = new SQLDataAccess();
    }

    @BeforeEach
    void clearDB() throws DataAccessException {
        db.clear();
    }

    // clear()

    @Test
    void clearPositive() throws DataAccessException {
        db.createUser(new UserData("carter", "pass", "test@test.com"));
        db.createGame(new GameData(0, null, null, "testGame", new ChessGame()));
        db.clear();
        assertNull(db.getUser("carter"));
        assertEquals(0, db.listGames().size());
    }

    // createUser()

    @Test
    void createUserPositive() throws DataAccessException {
        db.createUser(new UserData("carter", "pass", "test@test.com"));
        assertNotNull(db.getUser("carter"));
    }

    @Test
    void createUserDuplicate() throws DataAccessException {
        db.createUser(new UserData("carter", "pass", "test@test.com"));
        assertThrows(DataAccessException.class,
            () -> db.createUser(new UserData("carter", "other", "test2@test.com")));
    }

    // getUser()
    
    @Test
    void getUserExists() throws DataAccessException {
        db.createUser(new UserData("carter", "pass", "test@test.com"));
        assertEquals("carter", db.getUser("carter").username());
    }

    @Test
    void getUserNotFound() throws DataAccessException {
        assertNull(db.getUser("nobody"));
    }

    // createAuth()

    @Test
    void createAuthPositive() throws DataAccessException {
        db.createUser(new UserData("carter", "pass", "test@test.com"));
        db.createAuth(new AuthData("token123", "carter"));
        assertNotNull(db.getAuth("token123"));
    }

    @Test
    void createAuthDuplicate() throws DataAccessException {
        db.createUser(new UserData("carter", "pass", "test@test.com"));
        db.createAuth(new AuthData("token123", "carter"));
        assertThrows(DataAccessException.class,
            () -> db.createAuth(new AuthData("token123", "carter")));
    }

    // getAuth()

    @Test
    void getAuthExists() throws DataAccessException {
        db.createUser(new UserData("carter", "pass", "test@test.com"));
        db.createAuth(new AuthData("token123", "carter"));
        assertEquals("carter", db.getAuth("token123").username());
    }

    @Test
    void getAuthMissing() throws DataAccessException {
        assertNull(db.getAuth("ghost"));
    }

    // deleteAuth()

    @Test
    void deleteAuthPositive() throws DataAccessException {
        db.createUser(new UserData("carter", "pass", "test@test.com"));
        db.createAuth(new AuthData("token123", "carter"));
        db.deleteAuth("token123");
        assertNull(db.getAuth("token123"));
    }

    @Test
    void deleteAuthNonexistent() {
        assertDoesNotThrow(() -> db.deleteAuth("ghost"));
    }

    // createGame()

    @Test
    void createGamePositive() throws DataAccessException {
        int id = db.createGame(new GameData(0, null, null, "testGame", new ChessGame()));
        assertTrue(id > 0);
    }

    @Test
    void createGameNullName() {
        assertThrows(DataAccessException.class,
            () -> db.createGame(new GameData(0, null, null, null, new ChessGame())));
    }

    // getGame()

    @Test
    void getGamePositive() throws DataAccessException {
        int id = db.createGame(new GameData(0, null, null, "testGame", new ChessGame()));
        assertEquals("testGame", db.getGame(id).gameName());
    }

    @Test
    void getGameNotFound() throws DataAccessException {
        assertNull(db.getGame(99999));
    }

    // listGames()

    @Test
    void listGamesPositive() throws DataAccessException {
        db.createGame(new GameData(0, null, null, "game1", new ChessGame()));
        db.createGame(new GameData(0, null, null, "game2", new ChessGame()));
        assertEquals(2, db.listGames().size());
    }

    @Test
    void listGamesEmpty() throws DataAccessException {
        assertEquals(0, db.listGames().size());
    }

}
