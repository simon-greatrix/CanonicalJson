package io.setl.json.primitive.cache;

/**
 * @author Simon Greatrix on 05/02/2020.
 */
public class SimpleLruCacheFactory implements ICacheFactory {

  @Override
  public <K, V> ICache<K, V> create(int maxSize) {
    return new SimpleLruCache<>(maxSize);
  }
}
