package io.setl.json.pointer;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * @author Simon Greatrix on 27/01/2020.
 */
public class JPointer implements JsonPointer {

  public static String escapeKey(String key) {
    if (key.indexOf('~') == -1 && key.indexOf('/') == -1) {
      return key;
    }
    return key.replace("~", "~0").replace("/", "~1");
  }


  protected final String path;

  protected final PathElement root;


  public JPointer(String path, PathElement root) {
    this.path = path;
    this.root = root;
  }


  public JPointer(PathElement root) {
    this.root = root;
    StringBuilder builder = new StringBuilder();
    root.buildPath(builder);
    path = builder.toString();
  }


  @Override
  public <T extends JsonStructure> T add(T target, JsonValue value) {
    if (target.getValueType() == ValueType.OBJECT) {
      root.add((JsonObject) target, value);
    } else {
      root.add((JsonArray) target, value);
    }
    return target;
  }


  @Override
  public boolean containsValue(JsonStructure target) {
    if (target.getValueType() == ValueType.OBJECT) {
      return root.containsValue((JsonObject) target);
    }
    return root.containsValue((JsonArray) target);
  }


  @Override
  public JsonValue getValue(JsonStructure target) {
    if (target.getValueType() == ValueType.OBJECT) {
      return root.getValue((JsonObject) target);
    }
    return root.getValue((JsonArray) target);
  }


  @Override
  public <T extends JsonStructure> T remove(T target) {
    if (target.getValueType() == ValueType.OBJECT) {
      root.remove((JsonObject) target);
    } else {
      root.remove((JsonArray) target);
    }
    return target;
  }


  @Override
  public <T extends JsonStructure> T replace(T target, JsonValue value) {
    if (target.getValueType() == ValueType.OBJECT) {
      root.replace((JsonObject) target, value);
    } else {
      root.replace((JsonArray) target, value);
    }
    return target;
  }

}
