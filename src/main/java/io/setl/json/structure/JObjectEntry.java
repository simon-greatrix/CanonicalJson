package io.setl.json.structure;

import java.util.Map.Entry;
import java.util.Objects;
import javax.json.JsonObject;
import javax.json.JsonValue;

import io.setl.json.Primitive;
import io.setl.json.primitive.PNull;

/**
 * @author Simon Greatrix on 12/08/2020.
 */
public class JObjectEntry implements Entry<String, JsonValue> {

  /**
   * Copy all the entries in one list of entries into another Json Object, creating a deep copy of the values.
   *
   * @param jsonObject the object to copy into
   * @param root       the root of the circular linked list
   */
  public static void copyInto(JsonObject jsonObject, JObjectEntry root) {
    JObjectEntry entry = root.after;
    while (entry != root) {
      jsonObject.put(entry.key, entry.value.copy());
      entry = entry.after;
    }
  }


  /** The key for this entry. */
  final String key;

  /** The entry inserted after this one. */
  JObjectEntry after;

  /** The entry inserted before this one. */
  JObjectEntry before;

  /** The value of this entry. */
  Primitive value;


  public JObjectEntry() {
    after = this;
    before = this;
    key = "";
    value = PNull.NULL;
  }


  public JObjectEntry(JObjectEntry root, String key) {
    if (root != null) {
      before = root.before;
      before.after = this;

      after = root;
      after.before = this;
    } else {
      after = this;
      before = this;
    }

    this.key = key;
  }


  /**
   * Create a copy of this. The copy will not be part of an insert-order list and the value will be a deep copy.
   *
   * @return a copy of this
   */
  public JObjectEntry copy() {
    JObjectEntry newCopy = new JObjectEntry(null, key);
    newCopy.value = this.getValue().copy();
    return newCopy;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof Entry) {
      Entry<?, ?> other = (Entry<?, ?>) o;
      return Objects.equals(key, other.getKey()) && Objects.equals(value, other.getValue());
    }
    return false;
  }


  @Override
  public String getKey() {
    return key;
  }


  @Override
  public Primitive getValue() {
    return value;
  }


  @Override
  public int hashCode() {
    return key.hashCode() ^ value.hashCode();
  }


  public void remove() {
    after.before = before;
    before.after = after;
  }


  @Override
  public JsonValue setValue(JsonValue newValue) {
    return setValue(Primitive.cast(newValue));
  }


  public Primitive setValue(Primitive newValue) {
    Primitive oldValue = value;
    value = newValue != null ? newValue : PNull.NULL;
    return oldValue;
  }

}
