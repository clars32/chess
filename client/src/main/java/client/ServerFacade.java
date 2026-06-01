package client;

import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;

public class ServerFacade {
    
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public ServerFacade(int port) {
        this("http://localhost:" + port);
    }

    // public API

    /// small local records for request bodies
    private record LoginBody(String username, String password) {}
    private record CreateGameBody(String gameName) {}
    private record JoinGameBody(String playerColor, int gameID) {}
    private record CreateGameResult(int gameID) {}
    private record ListGamesResult(Collection<GameData> games) {}

    public void clear() throws ResponseException {
        makeRequest("DELETE", "/db", null, null, null);
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        var body = new UserData(username, password, email);
        return makeRequest("POST", "/user", body, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws ResponseException {
        var body = new LoginBody(username, password);
        return makeRequest("POST", "/session", body, null, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        makeRequest("DELETE", "/session", null, authToken, null);
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        var body = new CreateGameBody(gameName);
        var result = makeRequest("POST", "/game", body, authToken, CreateGameResult.class);
        return result.gameID();
    }

    public Collection<GameData> listGames(String authToken) throws ResponseException {
        var result = makeRequest("GET", "/game", null, authToken, ListGamesResult.class);
        return result.games();
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        var body = new JoinGameBody(playerColor, gameID);
        makeRequest("PUT", "/game", body, authToken, null);
    }

    // private HTTP plumbing

    private<T> T makeRequest(String method, String path, Object requestBody,
                            String authToken, Class<T> responseClass) throws ResponseException {

        try {

            URL url = new URI(serverUrl + path).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(requestBody != null);

            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }

            if (requestBody != null) {
                http.addRequestProperty("Content-Type", "application/json");
                try (var out = http.getOutputStream()) {
                    out.write(gson.toJson(requestBody).getBytes());
                }
            }

            http.connect();

            int status = http.getResponseCode();
            if (status != 200) {
                String errorMessage = readError(http);
                throw new ResponseException(status, errorMessage);
            }

            if (responseClass == null) {
                return null;
            }

            try (InputStream in = http.getInputStream();
                InputStreamReader reader = new InputStreamReader(in)) {
                    return gson.fromJson(reader, responseClass);
            }

        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private String readError(HttpURLConnection http) {

        try (InputStream err = http.getErrorStream()) {
            if (err == null) return "server returned status " + http.getResponseCode();
            var parsed = gson.fromJson(new InputStreamReader(err), java.util.Map.class);
            Object message = parsed != null ? parsed.get("message") : null;
            return message != null ? message.toString() : "unknown error";
        } catch (IOException e) {
            return "unknown error";
        }

    }

}
