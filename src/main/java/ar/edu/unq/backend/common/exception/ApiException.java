package ar.edu.unq.backend.common.exception;

import ar.edu.unq.backend.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode code;
    private final List<String> details;

    public ApiException(HttpStatus status, ErrorCode code, String message) {
        this(status, code, message, List.of());
    }

    public ApiException(HttpStatus status, ErrorCode code, String message, List<String> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorCode getCode() {
        return code;
    }

    public List<String> getDetails() {
        return details;
    }
}

