package ru.practicum.ewm.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.interaction.api.exception.ConflictDataException;
import ru.practicum.interaction.api.exception.DuplicateException;
import ru.practicum.interaction.api.exception.InvalidDateTimeException;
import ru.practicum.interaction.api.exception.InvalidSortException;
import ru.practicum.interaction.api.exception.NotFoundException;
import ru.practicum.interaction.api.exception.NotFoundRecordInBDException;
import ru.practicum.interaction.api.exception.OperationFailedException;
import ru.practicum.interaction.api.exception.ServerErrorException;
import ru.practicum.interaction.api.exception.ValidationException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidExceptions(MethodArgumentNotValidException e) {
        String message = e.getMessage();
        log.debug("400 BAD_REQUEST {}", message, e);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = e.getMessage();
        log.debug("400 BAD_REQUEST {}", message, e);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidDateTimeException.class)
    public ErrorResponse handleConflictDataException(InvalidDateTimeException e) {
        String message = e.getMessage();
        log.debug("400 BAD_REQUEST {}", message, e);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NotFoundRecordInBDException.class)
    public ErrorResponse handleConflictDataException(NotFoundRecordInBDException e) {
        String message = e.getMessage();
        log.debug("400 BAD_REQUEST {}", message, e);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidSortException.class)
    public ErrorResponse handleConflictDataException(InvalidSortException e) {
        String message = e.getMessage();
        log.debug("400 BAD_REQUEST {}", message, e);
        return new ErrorResponse(message);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final ValidationException e) {
        String message = e.getMessage();
        log.debug("400 BAD_REQUEST {}", message, e);
        return new ErrorResponse(message);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException e) {
        String message = e.getMessage();
        log.debug("404 NOT_FOUND {}", message, e);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(OperationFailedException.class)
    public ErrorResponse handleConflictDataException(OperationFailedException e) {
        String message = e.getMessage();
        log.debug("409 CONFLICT {}", message, e);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictDataException.class)
    public ErrorResponse handleConflictDataException(ConflictDataException e) {
        String message = e.getMessage();
        log.debug("409 CONFLICT {}", message, e);
        return new ErrorResponse(message);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicate(final DuplicateException e) {
        String message = e.getMessage();
        log.debug("409 CONFLICT {}", message, e);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServerErrorException.class)
    public ErrorResponse handleAllExceptions(ServerErrorException e) {
        String message = e.getMessage();
        log.debug("500 INTERNAL_SERVER_ERROR {}", message, e);
        return new ErrorResponse(message);
    }
}
