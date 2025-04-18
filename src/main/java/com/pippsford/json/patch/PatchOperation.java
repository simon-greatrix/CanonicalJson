package com.pippsford.json.patch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import jakarta.annotation.Nonnull;
import jakarta.json.JsonArray;
import jakarta.json.JsonPatch.Operation;
import jakarta.json.JsonStructure;

import com.pippsford.json.CJObject;
import com.pippsford.json.FormattedJson;
import com.pippsford.json.exception.InvalidPatchException;
import com.pippsford.json.patch.ops.Add;
import com.pippsford.json.patch.ops.Copy;
import com.pippsford.json.patch.ops.Move;
import com.pippsford.json.patch.ops.Remove;
import com.pippsford.json.patch.ops.Replace;
import com.pippsford.json.patch.ops.Test;
import com.pippsford.json.pointer.JsonExtendedPointer;
import com.pippsford.json.pointer.PointerFactory;

/**
 * An operation in a patch.
 *
 * @author Simon Greatrix on 06/02/2020.
 */
@JsonTypeInfo(
    use = Id.NAME,
    include = As.EXISTING_PROPERTY,
    property = "op"
)
@JsonSubTypes({
    @Type(name = "add", value = Add.class),
    @Type(name = "copy", value = Copy.class),
    @Type(name = "move", value = Move.class),
    @Type(name = "remove", value = Remove.class),
    @Type(name = "replace", value = Replace.class),
    @Type(name = "test", value = Test.class)
})
public abstract class PatchOperation implements FormattedJson {

  /**
   * Convert a JsonArray specifying patch operations to the actual operations.
   *
   * @param array array
   *
   * @return the operations
   */
  static List<PatchOperation> convert(JsonArray array) {
    if (array == null) {
      return List.of();
    }
    int s = array.size();
    List<PatchOperation> operations = new ArrayList<>(s);
    for (int i = 0; i < s; i++) {
      CJObject jsonObject = CJObject.asJObject(array.getJsonObject(i));
      String op = jsonObject.getString("op");
      switch (op) {
        case "add":
          operations.add(new Add(jsonObject));
          break;
        case "copy":
          operations.add(new Copy(jsonObject));
          break;
        case "move":
          operations.add(new Move(jsonObject));
          break;
        case "remove":
          operations.add(new Remove(jsonObject));
          break;
        case "replace":
          operations.add(new Replace(jsonObject));
          break;
        case "test":
          operations.add(Test.create(jsonObject));
          break;
        default:
          throw new InvalidPatchException("Unknown operation: \"" + op + "\"");
      }
    }
    return operations;
  }


  /** The pointer to the target. */
  protected final JsonExtendedPointer pointer;

  private final String path;


  /**
   * New instance.
   *
   * @param path the path
   */
  protected PatchOperation(@Nonnull String path) {
    this.path = Objects.requireNonNull(path, "path must be specified");
    pointer = PointerFactory.create(path);
  }


  /**
   * New instance.
   *
   * @param pointer the pointer
   */
  protected PatchOperation(@Nonnull JsonExtendedPointer pointer) {
    path = Objects.requireNonNull(pointer, "pointer must be specified").getPath();
    this.pointer = pointer;
  }


  /**
   * New instance.
   *
   * @param object the JSON object that specifies the operation
   */
  protected PatchOperation(@Nonnull CJObject object) {
    this(object.getString("path"));
  }


  /**
   * Apply this operation to the target.
   *
   * @param <T>    the type of the target
   * @param target the target
   *
   * @return the modified target
   */
  public abstract <T extends JsonStructure> T apply(T target);


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PatchOperation)) {
      return false;
    }

    PatchOperation operation = (PatchOperation) o;
    return path.equals(operation.path);
  }


  /**
   * Get the operation name.
   *
   * @return the operation name
   */
  @JsonProperty("op")
  public String getOp() {
    return getOperation().operationName();
  }


  /**
   * Get the operation this patch operation represents.
   *
   * @return the operation.
   */
  @JsonIgnore
  public abstract Operation getOperation();


  /**
   * Get the path of this operation.
   *
   * @return the path
   */
  public String getPath() {
    return path;
  }


  /**
   * Get the pointer used in the operation.
   *
   * @return the pointer
   */
  @JsonIgnore
  public JsonExtendedPointer getPathPointer() {
    return pointer;
  }


  @Override
  public int hashCode() {
    return path.hashCode();
  }


  /**
   * Output this operation as a JSON object in canonical form.
   *
   * @return the JSON object that specifies this operation
   */
  public String toCanonicalString() {
    return toJsonObject().toCanonicalString();
  }


  /**
   * Output this operation as a JSON object.
   *
   * @return the JSON object that specifies this operation
   */
  public abstract CJObject toJsonObject();


  /**
   * Output this operation as a JSON object in pretty form.
   *
   * @return the JSON object that specifies this operation
   */
  public String toPrettyString() {
    return toJsonObject().toPrettyString();
  }


  @Override
  public String toString() {
    return toJsonObject().toString();
  }

}
