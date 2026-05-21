package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;

public class AuthService {
    
    private final DataAccess dataAccess;

    public AuthService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // Verifies the authToken and either returns the corresponding AuthData or throws an exception
    // Written this way so that other endpoints can just call this instead of having duplicated auth verification
    public AuthData verify(String authToken) throws DataAccessException {

        if (authToken == null) {
            throw new UnauthorizedException("unauthorized");
        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }
        return auth;

    }

    public void logout(String authToken) throws DataAccessException {

        verify(authToken);
        dataAccess.deleteAuth(authToken);

    }
    
}
