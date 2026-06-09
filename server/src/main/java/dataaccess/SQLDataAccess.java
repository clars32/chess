package dataaccess;

import chess.ChessGame;
import model.*;

import org.mindrot.jbcrypt.BCrypt;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;

public class SQLDataAccess implements DataAccess {
    
    private final Gson gson = GameGson.create();

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

    private GameData readGame(ResultSet rs) throws SQLException {
        return new GameData(
            rs.getInt("gameID"),
            rs.getString("whiteUsername"),
            rs.getString("blackUsername"),
            rs.getString("gameName"),
            gson.fromJson(rs.getString("game"), ChessGame.class)
        );
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
    public void createUser(UserData user) throws DataAccessException {
        var sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.username());
                ps.setString(2, BCrypt.hashpw(user.password(), BCrypt.gensalt()));
                ps.setString(3, user.email());
                ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("createUser failed", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var sql = "DELETE FROM auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql)) {
                ps.setString(1, authToken);
                ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("deleteAuth failed", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var sql = "SELECT authToken, username FROM auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql)) {
                ps.setString(1, authToken);
                var rs = ps.executeQuery();
                if (rs.next()) {
                    return new AuthData(rs.getString("authToken"),
                                        rs.getString("username"));
                }
                return null;
        } catch (SQLException e) {
            throw new DataAccessException("getAuth failed", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql)) {
                ps.setInt(1, gameID);
                var rs = ps.executeQuery();
                if (rs.next()) {
                    return readGame(rs);
                }
                return null;
        } catch (SQLException e) {
            throw new DataAccessException("getGame failed", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var sql = "SELECT username, password, email FROM users WHERE username=?";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                var rs = ps.executeQuery();
                if (rs.next()) {
                    return new UserData(rs.getString("username"),
                                        rs.getString("password"),
                                        rs.getString("email"));
                }
                return null;
        } catch (SQLException e) {
            throw new DataAccessException("getUser failed", e);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        var sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql);
            var rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(readGame(rs));
                }
        } catch (SQLException e) {
            throw new DataAccessException("listGames failed", e);
        }
        return result;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var sql = "UPDATE games SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql)) {
                ps.setString(1, game.whiteUsername());
                ps.setString(2, game.blackUsername());
                ps.setString(3, game.gameName());
                ps.setString(4, gson.toJson(game.game()));
                ps.setInt(5, game.gameID());
                ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("updateGame failed", e);
        }
    }

}
