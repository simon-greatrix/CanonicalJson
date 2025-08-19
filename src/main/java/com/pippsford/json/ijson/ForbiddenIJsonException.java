package com.pippsford.json.ijson;

import com.pippsford.json.exception.ForbiddenJsonException;

/**
 * Thrown when attempting to write a JSON value which is outside the I-JSON subset.
 */
public class ForbiddenIJsonException extends ForbiddenJsonException {

  /**
   * New instance with the detail message.
   *
   * @param message the message
   */
  public ForbiddenIJsonException(String message) {
    super(message);
  }

}
