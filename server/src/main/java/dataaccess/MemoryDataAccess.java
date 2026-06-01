package dataaccess;

import java.util.*;

import model.*;

public class MemoryDataAccess implements DataAccess {
    

    // Store necessary info

    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameId = 1;

    // clear()

    @Override
    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
        nextGameId = 1;
    }

    // register()

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public void createAuth(AuthData auth) {
        auths.put(auth.authToken(), auth);
    }
    
    // logout()

    @Override
    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }

    // listGames()

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    // createGame()

    @Override
    public int createGame(GameData game) {
        int id = nextGameId++;
        GameData withId = new GameData(id, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(id, withId);
        return id;
    }

    // joinGame()

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

}
