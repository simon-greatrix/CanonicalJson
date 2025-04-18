package com.pippsford.json.primitive.cache;

import java.util.Locale;

import com.pippsford.json.primitive.CJString;
import com.pippsford.json.primitive.numbers.CJNumber;

/**
 * The types of cache.
 *
 * @author Simon Greatrix on 27/12/2021.
 */
public enum CacheType {
  /** A cache to promote unique map keys. Maps the proposed key {@link String} to its standard representation {@link String}. */
  KEYS,

  /**
   * A cache to improve the parsing of numbers. Maps the unprocessed value {@link String} to the Canonical Number
   * {@link CJNumber}.
   */
  NUMBERS,

  /** A cache to promote string value re-use. Maps the encapsulated {@link String} to the Canonical wrapper {@link CJString}. */
  STRINGS,

  /** A cache that maps a {@link Number} value to a standard representation {@link CJNumber}. */
  VALUES;


  /**
   * Get the name used as part of a configuration property.
   *
   * @return the configuration property element
   */
  public String getPropertyName() {
    return name().toLowerCase(Locale.ROOT);
  }
}
