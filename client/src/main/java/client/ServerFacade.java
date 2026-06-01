package client;

import com.google.gson.Gson;

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

    // private HTTP plumbing

}
