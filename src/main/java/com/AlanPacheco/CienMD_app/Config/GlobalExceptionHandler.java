package com.AlanPacheco.CienMD_app.Config;

import com.AlanPacheco.CienMD_app.DTO.ErrorResponse;
import com.AlanPacheco.CienMD_app.Exception.GameAlreadyFinishedException;
import com.AlanPacheco.CienMD_app.Exception.GameNotFoundException;
import com.AlanPacheco.CienMD_app.Exception.GameQuestionNotFoundException;
import com.AlanPacheco.CienMD_app.Exception.InsufficientQuestionsException;
import com.AlanPacheco.CienMD_app.Exception.ParticipantNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGameNotFound(GameNotFoundException ex) {
        return new ErrorResponse("GAME_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ParticipantNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleParticipantNotFound(ParticipantNotFoundException ex) {
        return new ErrorResponse("PARTICIPANT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(GameQuestionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGameQuestionNotFound(GameQuestionNotFoundException ex) {
        return new ErrorResponse("GAME_QUESTION_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InsufficientQuestionsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInsufficientQuestions(InsufficientQuestionsException ex) {
        return new ErrorResponse("INSUFFICIENT_QUESTIONS", ex.getMessage());
    }

    @ExceptionHandler(GameAlreadyFinishedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleGameFinished(GameAlreadyFinishedException ex) {
        return new ErrorResponse("GAME_FINISHED", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ErrorResponse("VALIDATION_ERROR", msg);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalState(IllegalStateException ex) {
        return new ErrorResponse("INVALID_STATE", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        return new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
    }
}
