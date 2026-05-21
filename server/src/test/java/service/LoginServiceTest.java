package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class LoginServiceTest {
    
    private DataAccess dao;
    private UserService service;

    @BeforeEach
    void setup() {

        dao = new MemoryDataAccess();
        service = new UserService(dao);

    }

    @Test
    void loginSuccess() throws DataAccessException {

        service.register(new RegisterRequest("carter", "pass", "test@test.com"));

        LoginResult result = service.login(new LoginRequest("carter", "pass"));

        assertEquals("carter", result.username());
        assertNotNull(result.authToken());

    }

    @Test
    void loginWrongPasswordThrows() throws DataAccessException {

        service.register(new RegisterRequest("carter", "pass", "test@test.com"));

        assertThrows(UnauthorizedException.class, () ->
            service.login(new LoginRequest("carter", "wrongpass")));
    }
    
}
