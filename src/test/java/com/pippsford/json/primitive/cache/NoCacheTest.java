package com.pippsford.json.primitive.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

/**
 * @author Simon Greatrix on 05/02/2020.
 */
public class NoCacheTest {

  @Test
  public void test() {
    ICache<String, String> cache = new NoCacheFactory().create(CacheType.KEYS, 3);
    assertEquals("a", cache.get("a", Function.identity()));
    // "a" was not cached
    assertEquals("", cache.get("a", a -> ""));
  }

}
