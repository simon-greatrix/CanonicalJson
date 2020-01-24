package io.setl.json.parser;

import io.setl.json.primitive.PNull;
import io.setl.json.primitive.PString;
import java.util.NoSuchElementException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser.Event;

/**
 * @author Simon Greatrix on 15/01/2020.
 */
class ObjectWalker extends WalkingParser {

  final JsonObject object;

  private final String[] keys;

  private boolean isKey = false;


  ObjectWalker(WalkingParser delegate, JsonObject object) {
    super(delegate, object.size(), Event.END_OBJECT);
    this.object = object;
    keys = object.keySet().toArray(new String[0]);
  }


  @Override
  protected boolean checkNextImpl() {
    if (isKey) {
      isKey = false;
      return true;
    }

    index++;
    isKey = true;
    return index < size;
  }


  @Override
  protected Event fetchNextImpl() {
    if (isKey) {
      return Event.KEY_NAME;
    }
    return eventForType(object.get(keys[index]));
  }


  @Override
  public JsonValue getValue() {
    if (index < 0) {
      throw new IllegalStateException("Next has not been called");
    }
    if (index >= size) {
      throw new NoSuchElementException();
    }
    if (isKey) {
      return new PString(keys[index]);
    }
    JsonValue jv = object.get(keys[index]);
    return (jv != null) ? jv : PNull.NULL;
  }


  @Override
  JsonValue primaryObject() {
    return object;
  }
}
