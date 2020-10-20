package io.setl.json.structure;

import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.function.Consumer;
import javax.json.JsonValue;

/**
 * Spliterator over JsonValues instead of Primitives. If someone calls setValue on the output, we have to convert to a Primitive.
 */
public class JObjectEntrySpliterator implements Spliterator<Entry<String, JsonValue>> {


  private final Spliterator<Entry<String, JObjectEntry>> me;


  JObjectEntrySpliterator(Spliterator<Entry<String, JObjectEntry>> me) {
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
  public boolean tryAdvance(Consumer<? super Entry<String, JsonValue>> action) {
    return me.tryAdvance(e -> action.accept(e.getValue()));
  }


  @Override
  public Spliterator<Entry<String, JsonValue>> trySplit() {
    Spliterator<Entry<String, JObjectEntry>> newSplit = me.trySplit();
    if (newSplit != null) {
      return new JObjectEntrySpliterator(newSplit);
    }
    return null;
  }

}
