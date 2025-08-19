package com.pippsford.json.exception;

import jakarta.json.JsonException;

/**
 * Thrown when attempting to write a JSON value which is not supported by the output format.
 *
 * <p>For example, the I-JSON format does not allow non-characters nor lone-surrogates in strings and object keys.</p>
 */
public class ForbiddenJsonException extends JsonException {

  /**
   * New instance with the detail message.
   *
   * @param message the message
   */
  public ForbiddenJsonException(String message) {
    super(message);
  }

}
