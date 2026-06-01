package client;

import model.AuthData;
import model.GameData;

import org.junit.jupiter.api.*;
import server.Server;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws ResponseException {
        facade.clear();
    }

    // register()

    @Test
    void registerSuccess() throws Exception {

        AuthData auth = facade.register("player1", "password", "p1@email.com");

        assertNotNull(auth);
        assertEquals("player1", auth.username());
        assertNotNull(auth.authToken());
        assertTrue(auth.authToken().length() > 10);

    }

    @Test
    void registerDuplicateUsernameFails() throws Exception {

        facade.register("player1", "password", "p1@email.com");

        ResponseException ex = assertThrows(ResponseException.class, () ->
            facade.register("player1", "different", "other@email.com"));
        assertEquals(403, ex.statusCode());

    }

    // login()

    @Test
    void loginSuccess() throws Exception {

        facade.register("player1", "password", "p1@email.com");
        AuthData auth = facade.login("player1", "password");

        assertNotNull(auth.authToken());
        assertEquals("player1", auth.username());

    }

    @Test
    void loginWrongPassword() throws Exception {

        facade.register("player1", "password", "p1@email.com");
        ResponseException ex = assertThrows(ResponseException.class, () ->
            facade.login("player1", "wrongpassword")
        );

        assertEquals(401, ex.statusCode());
        
    }

}