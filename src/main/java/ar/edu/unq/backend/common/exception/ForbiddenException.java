package ar.edu.unq.backend.common.exception;

import ar.edu.unq.backend.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(ErrorCode code, String message) {
        super(HttpStatus.FORBIDDEN, code, message);
    }
}

