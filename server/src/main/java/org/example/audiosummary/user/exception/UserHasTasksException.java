package org.example.audiosummary.user.exception;

public class UserHasTasksException extends RuntimeException {
    public UserHasTasksException(Long id) {
        super("User with id=" + id + " cannot be deleted because he has processing tasks");
    }
}