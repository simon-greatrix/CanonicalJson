package io.setl.json.structure;

import java.util.Spliterator;
import java.util.function.Consumer;
import javax.json.JsonValue;

import io.setl.json.Primitive;

/**
 * Spliterator over JsonValues instead of Primitives. I'm not sure why Java requires a wrapper as every Primitive is a JsonValue, but it is happier with one.
 */
public class JArraySpliterator implements Spliterator<JsonValue> {

  private final Spliterator<Primitive> me;


  public JArraySpliterator(Spliterator<Primitive> me) {
    this.me = me;
  }


  @Override
  public int characteristics() {
    return me.characteristics();
  }


  @Override
  public long estimateSize() {
    return me.estimateSize();
  }


  @Override
  public long getExactSizeIfKnown() {
    return me.getExactSizeIfKnown();
  }


  @Override
  public boolean tryAdvance(Consumer<? super JsonValue> action) {
    return me.tryAdvance(action);
  }


  @Override
  public Spliterator<JsonValue> trySplit() {
    Spliterator<Primitive> newSplit = me.trySplit();
    if (newSplit != null) {
      return new JArraySpliterator(newSplit);
    }
    return null;
  }

}
