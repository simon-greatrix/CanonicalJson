package com.pippsford.json.patch.ops;

import jakarta.json.JsonPatch.Operation;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.pippsford.json.CJObject;
import com.pippsford.json.builder.ObjectBuilder;
import com.pippsford.json.patch.PatchOperation;

/**
 * An "add" operation.
 *
 * @author Simon Greatrix on 06/02/2020.
 */
public class Add extends PatchOperation {

  private final JsonValue value;


  /**
   * New instance.
   *
   * @param path  the path to add the value to
   * @param value the value to add
   */
  @JsonCreator
  public Add(
      @JsonProperty("path") String path,
      @JsonProperty("value") JsonValue value
  ) {
    super(path);
    this.value = value;
  }


  /**
   * New instance.
   *
   * @param object the definition
   */
  public Add(CJObject object) {
    super(object);
    value = object.getJsonValue("value");
  }


  @Override
  public <T extends JsonStructure> T apply(T target) {
    return pointer.add(target, value);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Add)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    Add add = (Add) o;

    return value.equals(add.value);
  }


  @Override
  public Operation getOperation() {
    return Operation.ADD;
  }


  /**
   * Get the value which is added.
   *
   * @return the value
   */
  public JsonValue getValue() {
    return value;
  }


  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }


  @Override
  public CJObject toJsonObject() {
    return new ObjectBuilder()
        .add("op", getOperation().operationName())
        .add("path", getPath())
        .add("value", getValue())
        .build();
  }

}
