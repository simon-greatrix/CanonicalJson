package com.pippsford.json.patch.ops;

import java.util.Objects;

import jakarta.annotation.Nonnull;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import com.pippsford.json.builder.ObjectBuilder;
import com.pippsford.json.exception.IncorrectValueException;
import com.pippsford.json.pointer.JsonExtendedPointer;

class TestValue extends Test {

  private final JsonValue value;


  /**
   * New instance for a value comparison.
   *
   * @param pointer the path to test
   * @param value   the value it must equal
   */
  TestValue(@Nonnull JsonExtendedPointer pointer, @Nonnull JsonValue value) {
    super(pointer);
    this.value = Objects.requireNonNull(value, "Test value must not be null");
  }


  /**
   * New instance for a value comparison.
   *
   * @param path  the path to test
   * @param value the value it must equal
   */
  TestValue(@Nonnull String path, @Nonnull JsonValue value) {
    super(path);
    this.value = Objects.requireNonNull(value, "Test value must not be null");
  }


  @Override
  public <T extends JsonStructure> T apply(T target) {
    JsonValue jsonValue = pointer.getValue(target);
    if (value != null && !value.equals(jsonValue)) {
      throw new IncorrectValueException("Test failed. Value at \"" + getPath() + "\" is not " + value);
    }
    return target;
  }


  @Override
  protected Object getCriteria() {
    return value;
  }


  @Nonnull
  @Override
  public Type getType() {
    return Type.VALUE;
  }


  /**
   * Get the value to test against.
   *
   * @return the value
   */
  public JsonValue getValue() {
    return value;
  }


  @Override
  protected void toJsonObject(ObjectBuilder builder) {
    builder.add("value", getValue());
  }

}
