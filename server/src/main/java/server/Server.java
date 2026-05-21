package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.*;
import io.javalin.*;
import com.google.gson.Gson;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final DataAccess dataAccess = new MemoryDataAccess();

    private final ClearService clearService = new ClearService(dataAccess);
    private final UserService userService = new UserService(dataAccess);
    private final AuthService authService = new AuthService(dataAccess);

    private final Gson gson = new Gson();

    public Server() {

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // clear()
        javalin.delete("/db", ctx -> {
            clearService.clear();
            ctx.status(200).result("{}");
        });

        // register()
        javalin.post("/user", ctx -> {
            RegisterRequest req = gson.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResult result = userService.register(req);
            ctx.status(200).result(gson.toJson(result));
        });

        // login()
        javalin.post("/session", ctx -> {
            LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);
            LoginResult result = userService.login(req);
            ctx.status(200).result(gson.toJson(result));
        });

        // logout()
        javalin.delete("/session", ctx -> {
            String authToken = ctx.header("authorization");
            authService.logout(authToken);
            ctx.status(200).result("{}");
        });

        // Handle any exceptions
        javalin.exception(BadRequestException.class, (e, ctx) -> {
            ctx.status(400).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });
        javalin.exception(UnauthorizedException.class, (e, ctx) -> {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });
        javalin.exception(AlreadyTakenException.class, (e, ctx) -> {
            ctx.status(403).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });
        javalin.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

}
