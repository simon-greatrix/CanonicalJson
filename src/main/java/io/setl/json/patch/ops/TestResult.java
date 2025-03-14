package io.setl.json.patch.ops;

import java.util.Objects;

import jakarta.annotation.Nonnull;
import jakarta.json.JsonException;
import jakarta.json.JsonStructure;

import io.setl.json.builder.ObjectBuilder;
import io.setl.json.pointer.JsonExtendedPointer;
import io.setl.json.pointer.JsonExtendedPointer.ResultOfAdd;

class TestResult extends Test {

  private final ResultOfAdd resultOfAdd;


  /**
   * New instance for a presence check.
   *
   * @param path        the path to test
   * @param resultOfAdd Desired result of an add operation
   */
  TestResult(@Nonnull String path, @Nonnull ResultOfAdd resultOfAdd) {
    super(path);
    this.resultOfAdd = Objects.requireNonNull(resultOfAdd, "Result-of-add must not be null");
  }


  /**
   * New instance for a presence check.
   *
   * @param pointer     the path to test
   * @param resultOfAdd Desired result of an add operation
   */
  TestResult(@Nonnull JsonExtendedPointer pointer, @Nonnull ResultOfAdd resultOfAdd) {
    super(pointer);
    this.resultOfAdd = Objects.requireNonNull(resultOfAdd, "Result-of-add must not be null");
  }


  @Override
  public <T extends JsonStructure> T apply(T target) {
    ResultOfAdd actual = pointer.testAdd(target);
    if (actual != resultOfAdd) {
      throw new JsonException("Add will " + actual + ", required to " + resultOfAdd + " at " + getPath());
    }
    return target;
  }


  @Override
  protected Object getCriteria() {
    return resultOfAdd;
  }


  /**
   * Get the expected result of an "add" operation on the test path.
   *
   * @return the expected result
   */
  public ResultOfAdd getResultOfAdd() {
    return resultOfAdd;
  }


  @Nonnull
  @Override
  public Type getType() {
    return Type.RESULT;
  }


  @Override
  protected void toJsonObject(ObjectBuilder builder) {
    builder.add("resultOfAdd", resultOfAdd.name());
  }

}
