package com.pippsford.json.merge;

import java.util.Map.Entry;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

import com.pippsford.json.CJObject;
import com.pippsford.json.Canonical;

/**
 * Implementation of a JSON merge patch. Merge patches only operate on JSON objects.
 *
 * @author Simon Greatrix on 28/01/2020.
 */
public class Merge implements JsonMergePatch {

  static JsonValue mergePatch(JsonValue target, JsonValue patch) {
    if (patch.getValueType() != ValueType.OBJECT) {
      return Canonical.create(patch);
    }
    JsonObject patchObject = (JsonObject) patch;

    JsonObject output;
    if (target == null || target.getValueType() != ValueType.OBJECT) {
      output = new CJObject();
    } else {
      output = (JsonObject) target;
    }

    for (Entry<String, JsonValue> entry : patchObject.entrySet()) {
      if (entry.getValue().getValueType() == ValueType.NULL) {
        output.remove(entry.getKey());
      } else {
        String key = entry.getKey();
        output.compute(key, (k, current) -> mergePatch(current, entry.getValue()));
      }
    }
    return output;
  }


  private final Canonical patch;


  /**
   * New instance.
   *
   * @param patch the specification of the patch
   */
  public Merge(JsonValue patch) {
    this.patch = Canonical.cast(patch).copy();
  }


  /**
   * Implements the MergePatch function from RFC-7396.
   *
   * @param target the target to apply this patch to.
   *
   * @return the new value
   */
  @Override
  public JsonValue apply(JsonValue target) {
    // Create a copy
    JsonValue copy = Canonical.create(target);

    // Apply the patch
    return mergePatch(copy, patch);
  }


  @Override
  public JsonValue toJsonValue() {
    return patch;
  }

}
