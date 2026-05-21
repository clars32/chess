package dataaccess;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import com.google.gson.Gson;

import model.AuthData;
import model.GameData;
import model.UserData;

public class SQLDataAccess implements DataAccess {
    
    private final Gson gson = new Gson();

    public SQLDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        createTables();
    }

    private void createTables() throws DataAccessException {
        String[] statements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                username    VARCHAR(256) NOT NULL PRIMARY KEY,
                password    VARCHAR(256) NOT NULL,
                email       VARCHAR(256) NOT NULL
            )     
            """,
            """
            CREATE TABLE IF NOT EXISTS auth (
                authToken   VARCHAR(256) NOT NULL PRIMARY KEY,
                username    VARCHAR(256) NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
            gameID          INT             NOT NULL    AUTO_INCREMENT  PRIMARY KEY,
            whiteUsername   VARCHAR(256),
            blackUsername   VARCHAR(256),
            gameName        VARCHAR(256)    NOT NULL,
            game            TEXT            NOT NULL
            )     
            """
        };
        try (var conn = DatabaseManager.getConnection()) {
            for (var stmt : statements) {
                try (var ps = conn.prepareStatement(stmt)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to create tables", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            for (var table: new String[]{"auth", "games", "users"}) {
                try (var ps = conn.prepareStatement("DELETE FROM " + table)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("clear failed", e);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        var sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql)) {
                ps.setString(1, auth.authToken());
                ps.setString(2, auth.username());
                ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("createAuth failed", e);
        }
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        var sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, game.whiteUsername());
                ps.setString(2, game.blackUsername());
                ps.setString(3, game.gameName());
                ps.setString(4, gson.toJson(game.game()));
                ps.executeUpdate();
                var keys = ps.getGeneratedKeys();
                keys.next();
                return keys.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("createGame failed", e);
        }
    }


    @Override
    public UserData getUser(String username) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAuth'");
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAuth'");
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listGames'");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGame'");
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateGame'");
    }

}
