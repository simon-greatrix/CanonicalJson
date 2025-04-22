package com.pippsford.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.pippsford.json.exception.NotJsonException;
import com.pippsford.json.primitive.CJFalse;
import com.pippsford.json.primitive.CJNull;
import com.pippsford.json.primitive.CJString;
import com.pippsford.json.primitive.CJTrue;
import com.pippsford.json.primitive.numbers.CJNumber;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/** Helper for casting and creating Canonicals. */
class CanonicalCreator {

  /** The creators that can be used to create a Canonical from an object. */
  private static final List<CreateOp> CREATORS;

  private static Map<ValueType, Function<JsonValue, Object>> EVALUATORS;



  /** An operation to create a Canonical from an object. */
  abstract static class CreateOp {

    /**
     * Create a Canonical from the value.
     *
     * @param value the value
     *
     * @return the Canonical
     */
    abstract Canonical create(Object value);


    /**
     * Test if this operation can be applied to the value.
     *
     * @param value the value
     *
     * @return true if it can be applied
     */
    abstract boolean test(Object value);

  }


  /**
   * Create a Canonical from a JsonValue. If at all possible, the original object is returned.
   *
   * @param value the value
   *
   * @return the Canonical
   */
  static Canonical cast(JsonValue value) {
    if (value == null) {
      return CJNull.NULL;
    }
    if (value instanceof Canonical) {
      return (Canonical) value;
    }
    switch (value.getValueType()) {
      case ARRAY:
        return CJArray.asArray(value.asJsonArray());
      case FALSE:
        return CJFalse.FALSE;
      case NUMBER:
        return CJNumber.castUnsafe(((JsonNumber) value).numberValue());
      case NULL:
        return CJNull.NULL;
      case OBJECT:
        return CJObject.asJObject(value.asJsonObject());
      case STRING:
        return CJString.create(((JsonString) value).getString());
      case TRUE:
        return CJTrue.TRUE;
      default:
        throw new NotJsonException("Unknown Json Value type:" + value.getValueType());
    }
  }


  /**
   * Do the best effort conversion of any object to a Canonical, creating a new Primitive to represent the values where appropriate.
   *
   * @param value the value
   *
   * @return the Canonical
   */
  static Canonical create(Object value) {
    for (CreateOp op : CREATORS) {
      if (op.test(value)) {
        return op.create(value);
      }
    }
    throw new NotJsonException(value);
  }


  private static List<Object> getArrayValue(JsonArray jv) {
    ArrayList<Object> list = new ArrayList<>();
    for (JsonValue v : jv) {
      if (v != null) {
        list.add(null);
      }
      list.add(getValue(v));
    }
    return list;
  }


  private static Map<String, Object> getObjectValue(JsonObject jv) {
    TreeMap<String, Object> map = new TreeMap<>();
    for (var e : jv.entrySet()) {
      map.put(e.getKey(), getValue(e.getValue()));
    }
    return map;
  }


  /**
   * Extract the contained value from a JsonValue. Note: arrays and objects are fully converted.
   *
   * @param jv the JSON value
   *
   * @return the contained value
   */
  static Object getValue(JsonValue jv) {
    if (jv instanceof CJStructure) {
      return ((CJStructure) jv).getExternalValue();
    }
    if (jv instanceof Canonical) {
      return ((Canonical) jv).getValue();
    }
    if (jv == null) {
      return null;
    }

    Function<JsonValue, Object> evaluator = EVALUATORS.get(jv.getValueType());
    if (evaluator != null) {
      return evaluator.apply(jv);
    }

    throw new IllegalArgumentException("Unknown value type: " + jv.getValueType());
  }


  static {
    CREATORS = List.of(
        new CreateOp() {
          @Override
          Canonical create(Object value) {
            return CJNull.NULL;
          }


          @Override
          boolean test(Object value) {
            return value == null;
          }
        },

        new CreateOp() {
          Canonical create(Object value) {
            return ((Canonical) value).copy();
          }


          @Override
          boolean test(Object value) {
            return value instanceof Canonical;
          }
        },

        new CreateOp() {
          @Override
          Canonical create(Object value) {
            return cast((JsonValue) value);
          }


          @Override
          boolean test(Object value) {
            return (value instanceof JsonValue);
          }
        },

        new CreateOp() {
          @Override
          Canonical create(Object value) {
            return ((Boolean) value).booleanValue() ? CJTrue.TRUE : CJFalse.FALSE;
          }


          @Override
          boolean test(Object value) {
            return (value instanceof Boolean);
          }
        },

        new CreateOp() {
          @Override
          Canonical create(Object value) {
            return ((AtomicBoolean) value).get() ? CJTrue.TRUE : CJFalse.FALSE;
          }


          @Override
          boolean test(Object value) {
            return value instanceof AtomicBoolean;
          }
        },

        new CreateOp() {
          @Override
          Canonical create(Object value) {
            return CJString.create((String) value);
          }


          @Override
          boolean test(Object value) {
            return value instanceof String;
          }
        },

        new CreateOp() {
          @Override
          Canonical create(Object value) {
            return CJNumber.castUnsafe((Number) value);
          }


          @Override
          boolean test(Object value) {
            return value instanceof Number;
          }
        },

        new CreateOp() {
          @Override
          Canonical create(Object value) {
            return CJArray.asArray((Collection<?>) value);
          }


          @Override
          boolean test(Object value) {
            return value instanceof Collection<?>;
          }
        },

        new CreateOp() {
          @Override
          Canonical create(Object value) {
            return CJObject.asJObject((Map<?, ?>) value);
          }


          @Override
          boolean test(Object value) {
            return value instanceof Map<?, ?>;
          }
        },

        new CreateOp() {
          @Override
          Canonical create(Object value) {
            return CJArray.asArrayFromArray(value);
          }


          @Override
          boolean test(Object value) {
            return value.getClass().isArray();
          }
        }
    );
  }


  static {
    EnumMap<ValueType, Function<JsonValue, Object>> map = new EnumMap<>(ValueType.class);
    map.put(ValueType.ARRAY, v -> getArrayValue((JsonArray) v));
    map.put(ValueType.OBJECT, v -> getObjectValue((JsonObject) v));
    map.put(ValueType.FALSE, v -> Boolean.FALSE);
    map.put(ValueType.NUMBER, v -> ((JsonNumber) v).numberValue());
    map.put(ValueType.NULL, v -> null);
    map.put(ValueType.STRING, v -> ((JsonString) v).getString());
    map.put(ValueType.TRUE, v -> Boolean.TRUE);
    EVALUATORS = Collections.unmodifiableMap(map);
  }

}
