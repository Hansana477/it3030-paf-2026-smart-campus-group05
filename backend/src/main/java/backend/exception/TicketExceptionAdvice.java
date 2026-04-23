package backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class TicketExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleTicketNotFound(TicketNotFoundException exception) {
        Map<String, String> errorMap  = new HashMap<>();
        errorMap .put("errorMessage", exception.getMessage());
        return errorMap;
    }

    @ResponseBody
    @ExceptionHandler(TicketBadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleTicketBadRequest(TicketBadRequestException exception) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error Message", exception.getMessage());
        return errorMap;
    }

    @ResponseBody
    @ExceptionHandler(TicketForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleTicketForbidden(TicketForbiddenException exception) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error Message", exception.getMessage());
        return errorMap;
    }
}