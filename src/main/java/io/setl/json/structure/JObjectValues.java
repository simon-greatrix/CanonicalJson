package io.setl.json.structure;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.json.JsonValue;

/**
 * @author Simon Greatrix on 12/08/2020.
 */
public class JObjectValues implements Collection<JsonValue> {

  private final Collection<JObjectEntry> me;


  public JObjectValues(Collection<JObjectEntry> me) {
    this.me = me;
  }


  public boolean add(JsonValue primitive) {
    throw new UnsupportedOperationException("Add is not supported on a map's values");
  }


  public boolean addAll(@Nonnull Collection<? extends JsonValue> c) {
    throw new UnsupportedOperationException("Add is not supported on a map's values");
  }


  @Override
  public void clear() {
    me.clear();
  }


  @Override
  public boolean contains(Object o) {
    return me.contains(o);
  }


  @Override
  public boolean containsAll(@Nonnull Collection<?> c) {
    return me.containsAll(c);
  }


  @Override
  public boolean isEmpty() {
    return me.isEmpty();
  }


  @Override
  @Nonnull
  public Iterator<JsonValue> iterator() {
    final Iterator<JObjectEntry> myIterator = me.iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return myIterator.hasNext();
      }


      @Override
      public JsonValue next() {
        return myIterator.next().getValue();
      }


      @Override
      public void remove() {
        myIterator.remove();
      }
    };
  }


  @Override
  public Stream<JsonValue> parallelStream() {
    return me.parallelStream().map(JsonValue.class::cast);
  }


  @Override
  public boolean remove(Object o) {
    return me.remove(o);
  }


  @Override
  public boolean removeAll(@Nonnull Collection<?> c) {
    return me.removeAll(c);
  }


  @Override
  public boolean removeIf(Predicate<? super JsonValue> filter) {
    return me.removeIf(e -> filter.test(e.getValue()));
  }


  @Override
  public boolean retainAll(@Nonnull Collection<?> c) {
    return me.retainAll(c);
  }


  @Override
  public int size() {
    return me.size();
  }


  @Override
  public Spliterator<JsonValue> spliterator() {
    return new JValuesSpliterator(me.spliterator());
  }


  @Override
  public Stream<JsonValue> stream() {
    return me.stream().map(JsonValue.class::cast);
  }


  @Override
  @Nonnull
  public Object[] toArray() {
    return toArray(null, JsonValue[]::new);
  }


  @Override
  @Nonnull
  public <T> T[] toArray(T[] array) {
    Class<?> type = array.getClass().getComponentType();
    IntFunction<T[]> generator = s -> (T[]) Array.newInstance(type, s);
    return toArray(array, generator);
  }


  @SuppressWarnings("SuspiciousToArrayCall")
  @Override
  public <T> T[] toArray(IntFunction<T[]> generator) {
    return toArray(null, generator);
  }


  private <T> T[] toArray(T[] original, IntFunction<T[]> generator) {
    Iterator<JsonValue> iterator = iterator();
    int size = size();
    T[] target;
    if (original == null || original.length < size) {
      target = generator.apply(size);
    } else {
      target = original;
    }
    for (int i = 0; i < size; i++) {
      if (iterator.hasNext()) {
        target[i] = (T) iterator.next();
      } else {
        throw new ConcurrentModificationException("Too few elements in iterator");
      }
    }

    if (iterator.hasNext()) {
      throw new ConcurrentModificationException("Too many elements in iterator");
    }

    if (target.length > size) {
      target[size] = null;
    }

    return target;
  }

}
