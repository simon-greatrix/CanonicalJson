package com.pippsford.json.exception;

import jakarta.json.JsonException;

/**
 * Thrown when attempting to write a JSON structure that is outside the IJson subset.
 */
public class ForbiddenIJsonException extends JsonException {

  /**
   * New instance with the detail message.
   *
   * @param message the message
   */
  public ForbiddenIJsonException(String message) {
    super(message);
  }

}
