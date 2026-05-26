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
}
