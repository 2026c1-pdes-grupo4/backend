package ar.edu.unq.backend.common.exception;

import ar.edu.unq.backend.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    public NotFoundException(ErrorCode code, String message) {
        super(HttpStatus.NOT_FOUND, code, message);
    }
}

