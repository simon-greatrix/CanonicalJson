package io.setl.json.structure;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.json.JsonValue;

import io.setl.json.Primitive;

/**
 * Set which converts JsonValue to Primitives.
 */
public class JObjectEntries implements Set<Entry<String, JsonValue>> {

  private final Set<Entry<String, JObjectEntry>> mySet;


  public JObjectEntries(Set<Entry<String, JObjectEntry>> mySet) {
    this.mySet = mySet;
  }


  @Override
  public boolean add(Entry<String, JsonValue> entry) {
    throw new UnsupportedOperationException();
  }


  @Override
  public boolean addAll(@Nonnull Collection<? extends Entry<String, JsonValue>> c) {
    throw new UnsupportedOperationException();
  }


  @Override
  public void clear() {
    mySet.clear();
  }


  @Override
  public boolean contains(Object o) {
    if (!(o instanceof Entry<?, ?>)) {
      return false;
    }
    Entry<?, ?> e = (Entry<?, ?>) o;
    if (!(e.getKey() instanceof String)) {
      return false;
    }
    Object v = e.getValue();
    if (v instanceof Primitive) {
      return mySet.contains(e);
    }
    if (v != null && !(v instanceof JsonValue)) {
      return false;
    }
    return mySet.contains(new SimpleEntry<>(e.getKey(), Primitive.cast((JsonValue) e.getValue())));
  }


  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (!contains(o)) {
        return false;
      }
    }
    return true;
  }


  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public boolean equals(Object o) {
    return mySet.equals(o);
  }


  @Override
  public int hashCode() {
    return mySet.hashCode();
  }


  @Override
  public boolean isEmpty() {
    return mySet.isEmpty();
  }


  @Override
  @Nonnull
  public Iterator<Entry<String, JsonValue>> iterator() {
    final Iterator<Entry<String, JObjectEntry>> myIterator = mySet.iterator();
    return new Iterator<>() {

      @Override
      public boolean hasNext() {
        return myIterator.hasNext();
      }


      @Override
      public Entry<String, JsonValue> next() {
        return myIterator.next().getValue();
      }


      @Override
      public void remove() {
        myIterator.remove();
      }
    };
  }


  @Override
  public Stream<Entry<String, JsonValue>> parallelStream() {
    return mySet.parallelStream().map(Entry::getValue);
  }


  @Override
  public boolean remove(Object o) {
    return mySet.remove(o);
  }


  @Override
  public boolean removeAll(@Nonnull Collection<?> c) {
    return mySet.removeAll(c);
  }


  @Override
  public boolean removeIf(Predicate<? super Entry<String, JsonValue>> filter) {
    return mySet.removeIf(e -> filter.test(e.getValue()));
  }


  @Override
  public boolean retainAll(@Nonnull Collection<?> c) {
    return mySet.retainAll(c);
  }


  @Override
  public int size() {
    return mySet.size();
  }


  @Override
  public Spliterator<Entry<String, JsonValue>> spliterator() {
    return new JObjectEntrySpliterator(mySet.spliterator());
  }


  @Override
  public Stream<Entry<String, JsonValue>> stream() {
    return mySet.stream().map(Entry::getValue);
  }


  @Override
  @Nonnull
  public Object[] toArray() {
    return mySet.toArray();
  }


  @SuppressWarnings("SuspiciousToArrayCall")
  @Override
  @Nonnull
  public <T> T[] toArray(@Nonnull T[] a) {
    return mySet.toArray(a);
  }


  @SuppressWarnings("SuspiciousToArrayCall")
  @Override
  @Nonnull
  public <T> T[] toArray(@Nonnull IntFunction<T[]> generator) {
    return mySet.toArray(generator);
  }


  @Override
  public String toString() {
    return mySet.toString();
  }

}
