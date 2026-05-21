package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    
    private DataAccess dao;
    private UserService userService;
    private AuthService authService;

    @BeforeEach
    void setup() {

        dao = new MemoryDataAccess();
        userService = new UserService(dao);
        authService = new AuthService(dao);

    }

    @Test
    void logoutSuccess() throws DataAccessException {

        RegisterResult reg = userService.register(
            new RegisterRequest("carter", "pass", "test@test.com"));
        
        authService.logout(reg.authToken());

        assertNull(dao.getAuth(reg.authToken()));

    }

    @Test
    void logoutInvalidTokenThrows() {

        assertThrows(UnauthorizedException.class, () ->
            authService.logout("fake-token"));

    }

}