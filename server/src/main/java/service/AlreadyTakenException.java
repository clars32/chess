package service;

public class AlreadyTakenException extends RuntimeException {
    
    public AlreadyTakenException(String exceptionMessage) {
        super(exceptionMessage);
    }
    
}
