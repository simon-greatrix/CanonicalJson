package com.pippsford.json.patch.ops;

import jakarta.json.JsonPatch.Operation;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.pippsford.json.CJObject;
import com.pippsford.json.builder.ObjectBuilder;
import com.pippsford.json.patch.PatchOperation;
import com.pippsford.json.pointer.JsonExtendedPointer;
import com.pippsford.json.pointer.PointerFactory;

/**
 * A "copy" operation.
 *
 * @author Simon Greatrix on 06/02/2020.
 */
public class Copy extends PatchOperation {

  private final String from;

  private final JsonExtendedPointer fromPointer;


  /**
   * Create a new copy operation.
   *
   * @param path the path to copy to
   * @param from the path to copy from
   */
  @JsonCreator
  public Copy(
      @JsonProperty("path") String path,
      @JsonProperty("from") String from
  ) {
    super(path);
    this.from = from;
    fromPointer = PointerFactory.create(from);
  }


  /**
   * New instance from the JSON representation.
   *
   * @param object the representation of the copy operation
   */
  public Copy(CJObject object) {
    super(object);
    from = object.getString("from");
    fromPointer = PointerFactory.create(from);
  }


  @Override
  public <T extends JsonStructure> T apply(T target) {
    JsonValue value = fromPointer.getValue(target);
    return pointer.add(target, value);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Copy)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    Copy copy = (Copy) o;
    return from.equals(copy.from);
  }


  /**
   * Get the "from" path.
   *
   * @return the "from" path
   */
  public String getFrom() {
    return from;
  }


  /**
   * Get the "from" pointer.
   *
   * @return the "from" pointer
   */
  @JsonIgnore
  public JsonExtendedPointer getFromPointer() {
    return fromPointer;
  }


  @Override
  public Operation getOperation() {
    return Operation.COPY;
  }


  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + from.hashCode();
    return result;
  }


  @Override
  public CJObject toJsonObject() {
    return new ObjectBuilder()
        .add("op", getOperation().operationName())
        .add("path", getPath())
        .add("from", from)
        .build();
  }

}
