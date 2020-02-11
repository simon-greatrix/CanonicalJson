package io.setl.json.pointer;

import io.setl.json.exception.PointerMismatchException;
import javax.json.JsonException;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;

/**
 * @author Simon Greatrix on 27/01/2020.
 */
public class EmptyPointer implements JsonPointer {

  public static final EmptyPointer INSTANCE = new EmptyPointer();


  private EmptyPointer() {
    // do nothing
  }


  @Override
  public <T extends JsonStructure> T add(T target, JsonValue value) {
    if (value.getValueType() != target.getValueType()) {
      throw new PointerMismatchException("Root structure type mismatch", "", target.getValueType(), value.getValueType());
    }
    // This could fail if two javax.json implementations were in use in the same JVM.
    @SuppressWarnings("unchecked")
    T output = (T) value;
    return output;
  }


  @Override
  public boolean containsValue(JsonStructure target) {
    return true;
  }


  @Override
  public JsonValue getValue(JsonStructure target) {
    return target;
  }


  @Override
  public <T extends JsonStructure> T remove(T target) {
    throw new JsonException("Cannot remove root structure");
  }


  @Override
  public <T extends JsonStructure> T replace(T target, JsonValue value) {
    throw new JsonException("Cannot replace root structure");
  }
}
