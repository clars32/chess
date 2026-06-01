package service;

import dataaccess.*;
import model.UserData;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    
    @Test
    void clearRemovesAllData() throws DataAccessException {

        DataAccess dao = new MemoryDataAccess();
        dao.createUser(new UserData("carter", "pass", "test@test.com"));

        new ClearService(dao).clear();

        assertNull(dao.getUser("carter"));

    }
    
}
