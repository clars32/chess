package service;

public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String exceptionMessage) {
        super(exceptionMessage);
    }
    
}
