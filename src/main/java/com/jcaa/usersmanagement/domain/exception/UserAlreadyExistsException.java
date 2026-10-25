package com.jcaa.usersmanagement.domain.exception;

public final class UserAlreadyExistsException extends DomainException {

  private static final String MESSAGE_EMAIL_EXISTS = "A user with email '%s' already exists.";
  private static final String MESSAGE_USERNAME_EXISTS =
      "A user with username '%s' already exists.";

  private UserAlreadyExistsException(final String message) {
    super(message);
  }

  public static UserAlreadyExistsException becauseEmailAlreadyExists(final String email) {
    return new UserAlreadyExistsException(String.format(MESSAGE_EMAIL_EXISTS, email));
  }

  public static UserAlreadyExistsException becauseUsernameAlreadyExists(final String username) {
    return new UserAlreadyExistsException(String.format(MESSAGE_USERNAME_EXISTS, username));
  }
}
