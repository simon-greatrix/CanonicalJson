package io.setl.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import io.setl.json.exception.IncorrectTypeException;
import io.setl.json.exception.MissingItemException;
import io.setl.json.primitive.PFalse;
import io.setl.json.primitive.PNull;
import io.setl.json.primitive.PString;
import io.setl.json.primitive.PTrue;
import io.setl.json.primitive.numbers.PNumber;
import io.setl.json.structure.JNavigableObject;
import io.setl.json.structure.JObjectEntries;
import io.setl.json.structure.JObjectEntry;
import io.setl.json.structure.JObjectValues;

public class JObject implements JsonObject, Primitive {

  /**
   * Sort object keys into Unicode code point order.
   */
  public static final Comparator<String> CODE_POINT_ORDER = (s1, s2) -> {
    int len1 = s1.length();
    int len2 = s2.length();
    int lim = Math.min(len1, len2);
    for (int i = 0; i < lim; i++) {
      int cp1 = s1.codePointAt(i);
      int cp2 = s2.codePointAt(i);
      if (cp1 != cp2) {
        return cp1 - cp2;
      }
      if (cp1 > 0xffff) {
        i++;
      }
    }
    return len1 - len2;
  };


  /**
   * Convert any map into a JObject.
   *
   * @param map the map to convert
   *
   * @return the equivalent JObject
   */
  public static JObject asJObject(Map<?, ?> map) {
    if (map instanceof JObject) {
      return (JObject) map;
    }

    JObject out = new JObject();
    for (Entry<?, ?> entry : map.entrySet()) {
      Object key = entry.getKey();
      if (key == null) {
        throw new IllegalArgumentException("Map keys must not be null");
      }
      if (!(key instanceof String)) {
        throw new IllegalArgumentException("Map keys must be Strings, not " + key.getClass());
      }

      Object value = entry.getValue();
      Primitive primitive = Primitive.create(value);
      out.put((String) key, primitive);
    }
    return out;
  }


  private static Primitive nn(JObjectEntry entry) {
    return (entry != null) ? entry.getValue() : null;
  }


  protected final NavigableMap<String, JObjectEntry> myMap;

  protected final JObjectEntry root;


  /**
   * Create a new JObject holding a deep copy of the supplied map.
   *
   * @param map      the map to copy
   * @param deepCopy if true, create a deep copy of the values
   */
  protected JObject(NavigableMap<String, JObjectEntry> map, boolean deepCopy) {
    this.root = null;
    if (deepCopy) {
      myMap = new TreeMap<>();
      map.forEach((k, v) -> myMap.put(k, v.copy()));
    } else {
      myMap = map;
    }
  }


  protected JObject(Iterator<Entry<String, Primitive>> iterator) {
    myMap = new TreeMap<>(CODE_POINT_ORDER);
    root = new JObjectEntry();

    while (iterator.hasNext()) {
      Entry<String, Primitive> e = iterator.next();
      put(e.getKey(), e.getValue());
    }
  }


  public JObject() {
    myMap = new TreeMap<>(CODE_POINT_ORDER);
    root = new JObjectEntry();
  }


  /**
   * Create a new instance as a deep copy of the provided map.
   *
   * @param map the map to copy.
   */
  public JObject(Map<String, ?> map) {
    myMap = new TreeMap<>(CODE_POINT_ORDER);
    root = new JObjectEntry();
    for (Entry<String, ?> e : map.entrySet()) {
      put(e.getKey(), Primitive.create(e.getValue()));
    }
  }


  @Override
  public JObject asJsonObject() {
    return this;
  }


  @Override
  public void clear() {
    myMap.clear();
  }


  @Override
  public Primitive compute(String key, @Nonnull BiFunction<? super String, ? super JsonValue, ? extends JsonValue> remappingFunction) {
    JObjectEntry node = myMap.get(key);

    // Update value of existing node
    if (node != null) {
      JsonValue newValue = remappingFunction.apply(key, node.getValue());
      if (newValue != null) {
        node.setValue(newValue);
        return node.getValue();
      }

      // remove node
      remove(key);
      return null;
    }

    // create new node?
    JsonValue newValue = remappingFunction.apply(key, null);
    if (newValue != null) {
      Primitive primitive = Primitive.cast(newValue);
      put(key, primitive);
      return primitive;
    }

    // don't create new node
    return null;
  }


  @Override
  public Primitive computeIfPresent(String key, @Nonnull BiFunction<? super String, ? super JsonValue, ? extends JsonValue> remappingFunction) {
    JObjectEntry node = myMap.get(key);

    // Update value of existing node
    if (node != null) {
      JsonValue newValue = remappingFunction.apply(key, node.getValue());
      if (newValue != null) {
        node.setValue(newValue);
        return node.getValue();
      }

      // remove node
      remove(key);
      return null;
    }

    // don't create new node
    return null;
  }


  @Override
  public boolean containsKey(Object key) {
    return myMap.containsKey(key);
  }


  @Override
  public boolean containsValue(Object value) {
    return values().contains(value);
  }


  @Override
  public JObject copy() {
    JObject newCopy;
    if (root != null) {
      newCopy = new JObject();
      JObjectEntry.copyInto(newCopy, root);
    } else {
      newCopy = new JObject(myMap, true);
    }
    return newCopy;
  }


  @Override
  @Nonnull
  public Set<Entry<String, JsonValue>> entrySet() {
    return new JObjectEntries(myMap.entrySet());
  }


  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof JObject) {
      return myMap.equals(((JObject) o).myMap);
    }
    if (!(o instanceof Map<?, ?>)) {
      return false;
    }

    Map<?, ?> otherMap = (Map<?, ?>) o;
    if (size() != otherMap.size()) {
      return false;
    }
    return myMap.values().stream().allMatch(e -> {
      Object otherValue = otherMap.get(e.getKey());
      if (otherValue != null) {
        return e.getValue().equals(otherValue);
      }
      return e.getValue().equals(JsonValue.NULL) && otherMap.containsKey(e.getKey());
    });
  }


  @Override
  public Primitive get(Object key) {
    return nn(myMap.get(key));
  }


  /**
   * Get an array from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the array, or the default
   */
  public JArray getArray(String key, @Nonnull Function<String, JArray> defaultValue) {
    return getQuiet(JArray.class, key, defaultValue);
  }


  /**
   * Get an array from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the array, or the default
   */
  @Nonnull
  public JArray getArray(String key, @Nonnull JArray defaultValue) {
    return getQuiet(JArray.class, key, defaultValue);
  }


  /**
   * Get an array from the object.
   *
   * @return the array
   */
  @Nonnull
  public JArray getArray(String key) {
    return getSafe(JArray.class, ValueType.ARRAY, key);
  }


  /**
   * Get a big decimal from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the big decimal, or the default
   */
  @Nonnull
  public BigDecimal getBigDecimal(String key, @Nonnull BigDecimal defaultValue) {
    Number n = getQuiet(Number.class, key);
    if (n == null) {
      return defaultValue;
    }
    return Primitive.toBigDecimal(n);
  }


  /**
   * Get a big decimal from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the big decimal, or the default
   */
  public BigDecimal getBigDecimal(String key, @Nonnull Function<String, BigDecimal> defaultValue) {
    Number n = getQuiet(Number.class, key);
    if (n == null) {
      return defaultValue.apply(key);
    }
    return Primitive.toBigDecimal(n);
  }


  /**
   * Get a big decimal from the object.
   *
   * @return the big decimal
   */
  @Nonnull
  public BigDecimal getBigDecimal(String key) {
    Number n = getSafe(Number.class, ValueType.NUMBER, key);
    return Primitive.toBigDecimal(n);
  }


  /**
   * Get a big integer from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the big integer, or the default
   */
  @Nonnull
  public BigInteger getBigInteger(String key, @Nonnull BigInteger defaultValue) {
    Number n = getQuiet(Number.class, key);
    if (n == null) {
      return defaultValue;
    }
    return Primitive.toBigInteger(n);
  }


  /**
   * Get a big integer from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the big integer, or the default
   */
  public BigInteger getBigInteger(String key, @Nonnull Function<String, BigInteger> defaultValue) {
    Number n = getQuiet(Number.class, key);
    if (n == null) {
      return defaultValue.apply(key);
    }
    return Primitive.toBigInteger(n);
  }


  /**
   * Get a big integer from the object.
   *
   * @return the big integer
   */
  @Nonnull
  public BigInteger getBigInteger(String key) {
    Number n = getSafe(Number.class, ValueType.NUMBER, key);
    return Primitive.toBigInteger(n);
  }


  /**
   * Get a Boolean from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the Boolean, or the default
   */
  public boolean getBoolean(String key, boolean defaultValue) {
    return getQuiet(Boolean.class, key, defaultValue);
  }


  /**
   * Get a Boolean from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the Boolean, or the default
   */
  public boolean getBoolean(String key, @Nonnull Predicate<String> defaultValue) {
    Boolean value = getQuiet(Boolean.class, key);
    return (value != null) ? value : defaultValue.test(key);
  }


  /**
   * Get a Boolean from the object.
   *
   * @return the Boolean
   */
  public boolean getBoolean(String key) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null) {
      throw new MissingItemException(key, Primitive.IS_BOOLEAN);
    }
    Object value = primitive.getValue();
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    throw new IncorrectTypeException(key, Primitive.IS_BOOLEAN, primitive.getValueType());
  }


  /**
   * Get a double from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the double, or the default
   */
  public double getDouble(String key, double defaultValue) {
    Double n = optDouble(key);
    return (n != null) ? n : defaultValue;
  }


  /**
   * Get a double from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the double, or the default
   */
  public double getDouble(String key, @Nonnull ToDoubleFunction<String> defaultValue) {
    Double n = optDouble(key);
    return (n != null) ? n : defaultValue.applyAsDouble(key);
  }


  /**
   * Get a double from the object.
   *
   * @return the double
   */
  public double getDouble(String key) {
    Primitive p = getPrimitive(key);
    if (p == null) {
      throw new MissingItemException(key, ValueType.NUMBER);
    }
    Double n = optDouble(key);
    if (n == null) {
      throw new IncorrectTypeException(key, ValueType.NUMBER, p.getValueType());
    }
    return n;
  }


  /**
   * Get an integer from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the integer, or the default
   */
  public int getInt(String key, int defaultValue) {
    Number n = getQuiet(Number.class, key);
    return (n != null) ? n.intValue() : defaultValue;
  }


  /**
   * Get an integer from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the integer, or the default
   */
  public int getInt(String key, @Nonnull ToIntFunction<String> defaultValue) {
    Number n = getQuiet(Number.class, key);
    return (n != null) ? n.intValue() : defaultValue.applyAsInt(key);
  }


  /**
   * Get an integer from the object.
   *
   * @return the integer
   */
  @Override
  public int getInt(String key) {
    return getSafe(Number.class, ValueType.NUMBER, key).intValue();
  }


  @Override
  public JsonArray getJsonArray(String name) {
    return optArray(name);
  }


  @Override
  public JsonNumber getJsonNumber(String name) {
    Primitive p = getPrimitive(name);
    return (p != null) ? (PNumber) p : null;
  }


  @Override
  public JsonObject getJsonObject(String name) {
    return optObject(name);
  }


  @Override
  public JsonString getJsonString(String name) {
    Primitive p = getPrimitive(name);
    return (p != null) ? (PString) p : null;
  }


  /**
   * Get a JsonValue from this object. The value must exist.
   *
   * @param key the value's key
   *
   * @return the value
   */
  public JsonValue getJsonValue(String key) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null) {
      throw new MissingItemException(key, EnumSet.allOf(ValueType.class));
    }
    return primitive;
  }


  /**
   * Get a JsonValue from this object. If the value does not exist, the default value is returned.
   *
   * @param key          the value's key
   * @param defaultValue the default value
   *
   * @return the value
   */
  public JsonValue getJsonValue(String key, JsonValue defaultValue) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null) {
      return defaultValue;
    }
    return primitive;
  }


  /**
   * Get a JsonValue from this object. If the value does not exist, the function is invoked to create a value.
   *
   * @param key          the value's key
   * @param defaultValue supplier of a default value
   *
   * @return the value
   */
  public JsonValue getJsonValue(String key, @Nonnull Function<String, JsonValue> defaultValue) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null) {
      return defaultValue.apply(key);
    }
    return primitive;
  }


  /**
   * Get a long from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the long, or the default
   */
  public long getLong(String key, long defaultValue) {
    Number n = getQuiet(Number.class, key);
    return (n != null) ? n.longValue() : defaultValue;
  }


  /**
   * Get a long from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the long, or the default
   */
  public long getLong(String key, @Nonnull ToLongFunction<String> defaultValue) {
    Number n = getQuiet(Number.class, key);
    return (n != null) ? n.longValue() : defaultValue.applyAsLong(key);
  }


  /**
   * Get a long from the object.
   *
   * @return the long
   */
  public long getLong(String key) {
    Number n = getSafe(Number.class, ValueType.NUMBER, key);
    return n.longValue();
  }


  /**
   * Get an object from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the object, or the default
   */
  public JObject getObject(String key, @Nonnull Function<String, JObject> defaultValue) {
    return getQuiet(JObject.class, key, defaultValue);
  }


  /**
   * Get an object from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the object, or the default
   */
  public JObject getObject(String key, JObject defaultValue) {
    return getQuiet(JObject.class, key, defaultValue);
  }


  /**
   * Get an object from the object.
   *
   * @return the object
   */
  @Nonnull
  public JObject getObject(String key) {
    return getSafe(JObject.class, ValueType.OBJECT, key);
  }


  @Override
  public JsonValue getOrDefault(Object key, JsonValue defaultValue) {
    //noinspection SuspiciousMethodCalls
    JObjectEntry entry = myMap.get(key);
    return entry != null ? entry.getValue() : defaultValue;
  }


  public Primitive getPrimitive(String name) {
    return get(name);
  }


  private <T> T getQuiet(Class<T> clazz, String key) {
    return getQuiet(clazz, key, (Function<String, T>) k -> null);
  }


  private <T> T getQuiet(Class<T> clazz, String key, Function<String, T> function) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null) {
      return function.apply(key);
    }
    Object value = primitive.getValue();
    if (clazz.isInstance(value)) {
      return clazz.cast(value);
    }
    return function.apply(key);
  }


  private <T> T getQuiet(Class<T> clazz, String key, T defaultValue) {
    return getQuiet(clazz, key, (Function<String, T>) k -> defaultValue);
  }


  private <T> T getSafe(Class<T> clazz, ValueType type, String key) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null) {
      throw new MissingItemException(key, type);
    }
    Object value = primitive.getValue();
    if (clazz.isInstance(value)) {
      return clazz.cast(value);
    }
    throw new IncorrectTypeException(key, type, primitive.getValueType());
  }


  /**
   * Get a String from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the String, or the default
   */
  public String getString(String key, @Nonnull UnaryOperator<String> defaultValue) {
    return getQuiet(String.class, key, defaultValue);
  }


  /**
   * Get a String from the object.
   *
   * @param key          the key
   * @param defaultValue the default
   *
   * @return the String, or the default
   */
  public String getString(String key, String defaultValue) {
    return getQuiet(String.class, key, defaultValue);
  }


  /**
   * Get a String from the object.
   *
   * @return the String
   */
  @Nonnull
  public String getString(String key) {
    return getSafe(String.class, ValueType.STRING, key);
  }


  @Override
  public <T> T getValue(Class<T> reqType, T defaultValue) {
    if (reqType.isInstance(this)) {
      return reqType.cast(this);
    }
    return defaultValue;
  }


  @Override
  public Object getValue() {
    return this;
  }


  @Override
  public <T> T getValueSafe(Class<T> reqType) {
    return reqType.cast(this);
  }


  @Override
  public ValueType getValueType() {
    return ValueType.OBJECT;
  }


  @Override
  public int hashCode() {
    return myMap.hashCode();
  }


  @Override
  public boolean isEmpty() {
    return myMap.isEmpty();
  }


  @Override
  public boolean isNull(String name) {
    Primitive p = getPrimitive(name);
    if (p == null) {
      throw new MissingItemException(name, ValueType.NULL);
    }
    return p.getValueType() == ValueType.NULL;
  }


  /**
   * Verify if the type of the specified property is as required.
   *
   * @param key  the key
   * @param type the desired type
   *
   * @return True if the property exists and has the required type. False if the property exists and does not have the required type.
   *
   * @throws MissingItemException if the property does not exist
   */
  public boolean isType(String key, ValueType type) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null) {
      throw new MissingItemException(key, type);
    }
    return primitive.getValueType() == type;
  }


  @Override
  @Nonnull
  public Set<String> keySet() {
    return myMap.keySet();
  }


  /**
   * Create a navigable view on this.
   *
   * @return a navigable view
   */
  public JNavigableObject navigableView() {
    return new JNavigableObject(myMap);
  }


  /**
   * Get an array from the object.
   *
   * @return the array, or null
   */
  @Nullable
  public JArray optArray(String key) {
    return getQuiet(JArray.class, key);
  }


  /**
   * Get a big decimal from the object.
   *
   * @return the big decimal, or null
   */
  @Nullable
  public BigDecimal optBigDecimal(String key) {
    Number n = getQuiet(Number.class, key);
    return Primitive.toBigDecimal(n);
  }


  /**
   * Get a big integer from the object.
   *
   * @return the big integer, or null
   */
  @Nullable
  public BigInteger optBigInteger(String key) {
    Number n = getQuiet(Number.class, key);
    return Primitive.toBigInteger(n);
  }


  /**
   * Get a Boolean from the object.
   *
   * @return the Boolean, or null
   */
  @Nullable
  public Boolean optBoolean(String key) {
    return getQuiet(Boolean.class, key);
  }


  /**
   * Get a double from the object.
   *
   * @return the double, or null
   */
  @Nullable
  public Double optDouble(String key) {
    Primitive p = getPrimitive(key);
    if (p == null) {
      return null;
    }
    return PNumber.toDouble(p);
  }


  /**
   * Get an integer from the object.
   *
   * @return the integer, or null
   */
  @Nullable
  public Integer optInt(String key) {
    Number n = getQuiet(Number.class, key);
    return (n != null) ? n.intValue() : null;
  }


  /**
   * Get a JsonValue from this object. If the value does not exist, null is returned.
   *
   * @param key the value's key
   *
   * @return the value
   */
  public JsonValue optJsonValue(String key) {
    return getPrimitive(key);
  }


  /**
   * Get a long from the object.
   *
   * @return the long, or null
   */
  @Nullable
  public Long optLong(String key) {
    Number n = getQuiet(Number.class, key);
    return (n != null) ? n.longValue() : null;
  }


  /**
   * Get an object from the object.
   *
   * @return the object, or null
   */
  @Nullable
  public JObject optObject(String key) {
    return getQuiet(JObject.class, key);
  }


  /**
   * Get a String from the object.
   *
   * @return the String, or null
   */
  @Nullable
  public String optString(String key) {
    return getQuiet(String.class, key);
  }


  /**
   * Ensure that all Strings and Numbers have a single representation in memory.
   */
  public void optimiseStorage() {
    optimiseStorage(new HashMap<>());
  }


  /**
   * Ensure that all Strings and Numbers have a single representation in memory.
   *
   * @param values the unique values
   */
  void optimiseStorage(HashMap<Primitive, Primitive> values) {
    for (JObjectEntry e : myMap.values()) {
      Primitive current = e.getValue();
      switch (current.getValueType()) {
        case ARRAY:
          // recurse into array
          ((JArray) current).optimiseStorage(values);
          break;
        case OBJECT:
          // recurse into object
          ((JObject) current).optimiseStorage(values);
          break;
        default:
          Primitive single = values.computeIfAbsent(current, c -> c);
          if (single != current) {
            e.setValue(single);
          }
          break;
      }
    }
  }


  @Override
  public JsonValue put(String key, JsonValue value) {
    return put(key, Primitive.cast(value));
  }


  public Primitive put(String key, Primitive value) {
    JObjectEntry entry = myMap.computeIfAbsent(key, k -> new JObjectEntry(root, k));
    return entry.setValue(value);
  }


  /**
   * Put a null value into this.
   *
   * @param key the key
   */
  public void put(String key) {
    put(key, PNull.NULL);
  }


  /**
   * Put a value into this.
   *
   * @param key   the key
   * @param value the value
   */
  public JsonValue put(String key, Boolean value) {
    if (value != null) {
      return put(key, value ? PTrue.TRUE : PFalse.FALSE);
    }
    return put(key, PNull.NULL);
  }


  /**
   * Put a value into this.
   *
   * @param key   the key
   * @param value the value
   */
  public void put(String key, JArray value) {
    if (value != null) {
      put(key, (Primitive) value);
    } else {
      put(key, PNull.NULL);
    }
  }


  /**
   * Put a value into this.
   *
   * @param key   the key
   * @param value the value
   */
  public void put(String key, JObject value) {
    if (value != null) {
      put(key, (Primitive) value);
    } else {
      put(key, PNull.NULL);
    }
  }


  /**
   * Put a value into this.
   *
   * @param key   the key
   * @param value the value
   */
  public void put(String key, Number value) {
    if (value != null) {
      put(key, PNumber.cast(value));
    } else {
      put(key, PNull.NULL);
    }
  }


  /**
   * Put a value into this.
   *
   * @param key   the key
   * @param value the value
   */
  public void put(String key, String value) {
    if (value != null) {
      put(key, PString.create(value));
    } else {
      put(key, PNull.NULL);
    }
  }


  /**
   * Put all the contents of the supplied map into this.
   *
   * @param m the map of values
   */
  public void putAll(@Nonnull Map<? extends String, ? extends JsonValue> m) {
    if (m instanceof JNavigableObject) {
      myMap.putAll(((JNavigableObject) m).myMap);
    } else {
      for (Entry<? extends String, ? extends JsonValue> e : m.entrySet()) {
        put(e.getKey(), e.getValue());
      }
    }
  }


  @Override
  public Primitive remove(Object key) {
    JObjectEntry entry = myMap.remove(key);
    if (entry != null) {
      // found and removed, so unlink
      entry.remove();
      return entry.getValue();
    }

    // was not present
    return null;
  }


  /**
   * Remove a JSON array from this.
   *
   * @param key the key to remove, if it is an array
   *
   * @return the array removed
   */
  @Nullable
  public JArray removeArray(String key) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null || primitive.getValueType() != ValueType.ARRAY) {
      return null;
    }
    remove(key);
    return (JArray) primitive.getValue();
  }


  /**
   * Remove a Boolean from this.
   *
   * @param key the key to remove, if it is a Boolean
   *
   * @return the Boolean removed
   */
  @Nullable
  public Boolean removeBoolean(String key) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null || !Primitive.IS_BOOLEAN.contains(primitive.getValueType())) {
      return null;
    }
    remove(key);
    return (Boolean) primitive.getValue();
  }


  /**
   * Remove a null from this.
   *
   * @param key the key to remove, if it is null
   */
  public void removeNull(String key) {
    Primitive primitive = getPrimitive(key);
    if (primitive != null && primitive.getValueType() == ValueType.NULL) {
      remove(key);
    }
  }


  /**
   * Remove a number from this.
   *
   * @param key the key to remove, if it is a number
   *
   * @return the number removed
   */
  @Nullable
  public Number removeNumber(String key) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null || primitive.getValueType() != ValueType.NUMBER) {
      return null;
    }
    remove(key);
    return (Number) primitive.getValue();
  }


  /**
   * Remove a JSON object from this.
   *
   * @param key the key to remove, if it is an object
   *
   * @return the object removed
   */
  @Nullable
  public JObject removeObject(String key) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null || primitive.getValueType() != ValueType.OBJECT) {
      return null;
    }
    remove(key);
    return (JObject) primitive.getValue();
  }


  /**
   * Remove a String from this.
   *
   * @param key the key to remove, if it is a String
   *
   * @return the String that was removed
   */
  @Nullable
  public String removeString(String key) {
    Primitive primitive = getPrimitive(key);
    if (primitive == null || primitive.getValueType() != ValueType.STRING) {
      return null;
    }
    remove(key);
    return (String) primitive.getValue();
  }


  @Override
  public int size() {
    return myMap.size();
  }


  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append('{');
    for (Map.Entry<String, JsonValue> e : entrySet()) {
      PString.format(buf, e.getKey());
      buf.append(':');
      buf.append(e.getValue());
      buf.append(',');
    }
    if (buf.length() > 1) {
      // remove final comma
      buf.setLength(buf.length() - 1);
    }
    buf.append('}');
    return buf.toString();
  }


  @Override
  @Nonnull
  public Collection<JsonValue> values() {
    return new JObjectValues(myMap.values());
  }


  public JObject withInsertOrder() {
    // TODO
    return null;
  }


  @Override
  public void writeTo(Appendable writer) throws IOException {
    writer.append('{');
    boolean isNotFirst = false;
    for (Map.Entry<String, JsonValue> e : entrySet()) {
      if (isNotFirst) {
        writer.append(',');
      } else {
        isNotFirst = true;
      }

      PString.format(writer, e.getKey());
      writer.append(':');
      ((Primitive) e.getValue()).writeTo(writer);
    }
    writer.append('}');
  }

}
