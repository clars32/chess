package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(RegisterRequest req) throws DataAccessException {

        if (req.username() == null || req.password() == null || req.email() == null) {
            throw new BadRequestException("bad request");
        }

        if (dataAccess.getUser(req.username()) != null) {
            throw new AlreadyTakenException("already taken");
        }

        dataAccess.createUser(new UserData(req.username(), req.password(), req.email()));

        String token = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(token, req.username()));

        return new RegisterResult(req.username(), token);

    }

    public LoginResult login(LoginRequest req) throws DataAccessException {

        if (req.username() == null || req.password() == null) {
            throw new BadRequestException("bad request");
        }

        UserData user = dataAccess.getUser(req.username());
        if (user == null || !BCrypt.checkpw(req.password(), user.password())) {
            throw new UnauthorizedException("unauthorized");
        }

        String token = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(token, req.username()));

        return new LoginResult(req.username(), token);
        
    }

}