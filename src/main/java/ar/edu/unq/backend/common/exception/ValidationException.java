package ar.edu.unq.backend.common.exception;

import ar.edu.unq.backend.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ValidationException extends ApiException {
    public ValidationException(ErrorCode code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }

    public ValidationException(ErrorCode code, String message, List<String> details) {
        super(HttpStatus.BAD_REQUEST, code, message, details);
    }
}

