package ar.edu.unq.backend.common.exception;

import ar.edu.unq.backend.common.error.ApiError;
import ar.edu.unq.backend.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApiExceptionBuildsApiError() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("/properties/1");

        ApiException ex = new ValidationException(ErrorCode.INVALID_REQUEST, "Invalid payload");

        ResponseEntity<ApiError> response = handler.handleApiException(ex, request);
        assertNotNull(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().status());
        assertEquals("INVALID_REQUEST", response.getBody().code());
        assertEquals("/properties/1", response.getBody().path());
    }

    @Test
    void handleUnhandledBuildsInternalError() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("/boom");

        ResponseEntity<ApiError> response = handler.handleUnhandled(new RuntimeException("boom"), request);
        assertNotNull(response.getBody());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().status());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("/boom", response.getBody().path());
    }

    @Test
    void handleValidationBuildsFieldDetails() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("/users");

        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new ValidationPayload(), "payload");
        binding.rejectValue("email", "invalid", "must be valid");

        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, binding);

        ResponseEntity<ApiError> response = handler.handleValidation(ex, request);
        assertNotNull(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().status());
        assertEquals("INVALID_REQUEST", response.getBody().code());
        assertEquals(1, response.getBody().details().size());
    }

    @Test
    void handleAccessDeniedBuildsForbidden() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("/users");

        ResponseEntity<ApiError> response = handler.handleAccessDenied(new AccessDeniedException("denied"), request);
        assertNotNull(response.getBody());

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().status());
        assertEquals("FORBIDDEN", response.getBody().code());
    }

    @Test
    void handleAuthenticationBuildsUnauthorized() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("/users");

        ResponseEntity<ApiError> response = handler.handleAuthentication(new BadCredentialsException("bad"), request);
        assertNotNull(response.getBody());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().status());
        assertEquals("UNAUTHORIZED", response.getBody().code());
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
        // helper for MethodParameter construction in tests
    }

    private static class ValidationPayload {
        @SuppressWarnings("unused")
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}





