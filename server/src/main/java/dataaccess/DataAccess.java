package dataaccess;

import java.util.Collection;

import model.*;

public interface DataAccess {
    
    void clear() throws DataAccessException;

    // User methods
    UserData getUser(String username) throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;

    // Auth methods
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    // Game methods
    Collection<GameData> listGames() throws DataAccessException;
    int createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;

}