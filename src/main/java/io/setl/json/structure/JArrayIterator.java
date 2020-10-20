package io.setl.json.structure;

import java.util.ListIterator;
import javax.json.JsonValue;

import io.setl.json.Primitive;

/**
 * @author Simon Greatrix on 12/08/2020.
 */
public class JArrayIterator implements ListIterator<JsonValue> {

  private final ListIterator<Primitive> me;


  public JArrayIterator(ListIterator<Primitive> me) {
    this.me = me;
  }


  @Override
  public void add(JsonValue jsonValue) {
    me.add(Primitive.cast(jsonValue));
  }


  @Override
  public boolean hasNext() {
    return me.hasNext();
  }


  @Override
  public boolean hasPrevious() {
    return me.hasPrevious();
  }


  @Override
  public JsonValue next() {
    return me.next();
  }


  @Override
  public int nextIndex() {
    return me.nextIndex();
  }


  @Override
  public JsonValue previous() {
    return me.previous();
  }


  @Override
  public int previousIndex() {
    return me.previousIndex();
  }


  @Override
  public void remove() {
    me.remove();
  }


  @Override
  public void set(JsonValue jsonValue) {
    me.set(Primitive.cast(jsonValue));
  }

}
