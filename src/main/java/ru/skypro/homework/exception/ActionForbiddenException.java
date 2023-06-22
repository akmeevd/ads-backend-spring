package ru.skypro.homework.exception;

public class ActionForbiddenException extends RuntimeException {
    public ActionForbiddenException(String message) {
        super(message);
    }
}
