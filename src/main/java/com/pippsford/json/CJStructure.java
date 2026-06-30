package com.pippsford.json;

import jakarta.json.JsonStructure;

/** Mark a Canonical structure as being a JsonStructure. */
public interface CJStructure extends JsonStructure {

  /**
   * Convert the structure to a non-JSON representation. Objects are replaced with maps, Arrays are replaced with lists, and primitives are replaced with
   * their standard Java representations.
   *
   * @return this structure as an external value
   */
  Object getExternalValue();

}
