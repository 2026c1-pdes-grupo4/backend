package ar.edu.unq.backend.common.exception;

import ar.edu.unq.backend.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(ErrorCode code, String message) {
        super(HttpStatus.UNAUTHORIZED, code, message);
    }
}

