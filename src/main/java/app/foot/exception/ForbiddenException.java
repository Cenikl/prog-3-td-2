package app.foot.exception;


import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(
                HttpStatus.FORBIDDEN.value(), message);
    }
}
