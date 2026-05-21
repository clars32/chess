package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class RegisterServiceTest {
    
    private DataAccess dao;
    private UserService service;

    @BeforeEach
    void setup() {

        dao = new MemoryDataAccess();
        service = new UserService(dao);
        
    }

    @Test
    void registerSuccess() throws DataAccessException {

        RegisterResult result = service.register(
            new RegisterRequest("carter", "pass", "test@test.com"));

        assertEquals("carter", result.username());
        assertNotNull(result.authToken());
        assertNotNull(dao.getUser("carter"));

    }

    @Test
    void registerDuplicateUsernameThrows() throws DataAccessException {

        service.register(new RegisterRequest("carter", "pass", "test@test.com"));

        assertThrows(AlreadyTakenException.class, () ->
            service.register(new RegisterRequest("carter", "otherpass", "othertest@test.com")));

    }

}