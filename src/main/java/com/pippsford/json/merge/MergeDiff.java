package com.pippsford.json.merge;

import java.util.HashSet;
import java.util.Iterator;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

import com.pippsford.json.CJObject;

/**
 * Generator of merge patches.
 *
 * @author Simon Greatrix on 28/01/2020.
 */
public class MergeDiff {

  /**
   * Create the "diff" of two values using a JSON Merge Patch.
   *
   * @param input  the input value
   * @param output the desired result of the patch
   *
   * @return the patch
   */
  public static Merge create(JsonValue input, JsonValue output) {
    if (input.getValueType() != ValueType.OBJECT || output.getValueType() != ValueType.OBJECT) {
      return new Merge(output);
    }

    JsonObject merge = new CJObject();
    JsonObject inObject = (JsonObject) input;
    JsonObject outObject = (JsonObject) output;

    doDiff(merge, inObject, outObject);

    return new Merge(merge);
  }


  private static void doDiff(JsonObject merge, JsonObject inObject, JsonObject outObject) {
    HashSet<String> inKeys = new HashSet<>(inObject.keySet());
    HashSet<String> outKeys = new HashSet<>(outObject.keySet());

    // do "removes"
    doDiffRemoves(merge, inKeys, outKeys);

    // do "adds", and trim "outKeys" so that it is just the common keys.
    doDiffAdds(merge, inKeys, outKeys, outObject);

    // do "replaces"
    doDiffReplaces(outKeys, merge, inObject, outObject);
  }


  private static void doDiffAdds(JsonObject merge, HashSet<String> inKeys, HashSet<String> outKeys, JsonObject outObject) {
    Iterator<String> iterator = outKeys.iterator();
    while (iterator.hasNext()) {
      String s = iterator.next();
      if (!inKeys.contains(s)) {
        merge.put(s, outObject.get(s));
        iterator.remove();
      }
    }
  }


  private static void doDiffRemoves(JsonObject merge, HashSet<String> inKeys, HashSet<String> outKeys) {
    for (String s : inKeys) {
      if (!outKeys.contains(s)) {
        merge.put(s, JsonValue.NULL);
      }
    }
  }


  private static void doDiffReplaces(HashSet<String> outKeys, JsonObject merge, JsonObject inObject, JsonObject outObject) {
    for (String s : outKeys) {
      JsonValue inValue = inObject.get(s);
      JsonValue outValue = outObject.get(s);

      if (inValue.getValueType() == ValueType.OBJECT && outValue.getValueType() == ValueType.OBJECT) {
        CJObject childMerge = new CJObject();
        doDiff(childMerge, (JsonObject) inValue, (JsonObject) outValue);
        if (!childMerge.isEmpty()) {
          merge.put(s, childMerge);
        }
        continue;
      }

      if ((inValue.getValueType() != outValue.getValueType()) || !inValue.equals(outValue)) {
        merge.put(s, outValue);
      }
    }
  }


  private MergeDiff() {
    // do nothing
  }

}
