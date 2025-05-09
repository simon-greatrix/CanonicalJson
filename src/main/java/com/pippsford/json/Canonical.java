package com.pippsford.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.pippsford.json.exception.IncorrectTypeException;
import com.pippsford.json.io.CJReader;
import com.pippsford.json.io.ReaderFactory;
import com.pippsford.json.io.Utf8Appendable;
import com.pippsford.json.io.WriterFactory;
import com.pippsford.json.primitive.CJNull;
import com.pippsford.json.primitive.numbers.NumberParser;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;

/**
 * JSON values in canonical form.
 *
 * @author Simon Greatrix on 08/01/2020.
 */
@SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
public interface Canonical extends JsonValue, FormattedJson {

  /**
   * Test for whether a value is a Boolean. The ValueType enumeration distinguishes between true and false, but there are times we want either.
   */
  Set<ValueType> IS_BOOLEAN = Collections.unmodifiableSet(EnumSet.of(ValueType.TRUE, ValueType.FALSE));

  /**
   * Set of structure types.
   */
  Set<ValueType> IS_STRUCTURE = Collections.unmodifiableSet(EnumSet.of(ValueType.OBJECT, ValueType.ARRAY));

  /**
   * Create a Canonical from a JsonValue. If at all possible, the original object is returned.
   *
   * @param value the value
   *
   * @return the Canonical
   */
  static Canonical cast(JsonValue value) {
    return CanonicalCreator.cast(value);
  }

  /**
   * Create a Canonical from a JsonValue. If at all possible, the original object is returned.
   *
   * @param value the value
   *
   * @return the Canonical
   */
  static Canonical cast(Object value) {
    if (value == null) {
      return CJNull.NULL;
    }
    if (value instanceof Canonical) {
      return (Canonical) value;
    }
    if (value instanceof JsonValue) {
      return cast((JsonValue) value);
    }
    return create(value);
  }

  /**
   * Do the best effort conversion of any object to a Canonical, creating a new Primitive to represent the values where appropriate.
   *
   * @param value the value
   *
   * @return the Canonical
   */
  static Canonical create(Object value) {
    return CanonicalCreator.create(value);
  }

  /**
   * Return an empty structure of the same type is the example.
   *
   * @param example the example
   * @param <T>     the desired type
   *
   * @return the empty structure
   */
  @SuppressWarnings("unchecked")
  static <T extends JsonStructure> T createEmpty(T example) {
    if (example == null) {
      throw new IncorrectTypeException(IS_STRUCTURE, ValueType.NULL);
    }
    switch (example.getValueType()) {
      case ARRAY:
        return (T) EMPTY_JSON_ARRAY;
      case OBJECT:
        return (T) EMPTY_JSON_OBJECT;
      default:
        throw new IncorrectTypeException(IS_STRUCTURE, example.getValueType());
    }
  }

  /**
   * Extract the contained value from a JsonValue. Note: arrays and objects are fully converted.
   *
   * @param jv the JSON value
   *
   * @return the contained value
   */
  static Object getValue(JsonValue jv) {
    return CanonicalCreator.getValue(jv);
  }

  /**
   * Get the value enclosed in a JSON value.
   *
   * @param reqType      the required type
   * @param jv           the JSON value
   * @param defaultValue the default value to return if the value is missing or not the correct type
   * @param <T>          the required type
   *
   * @return the value if possible, otherwise the default
   */
  static <T> T getValue(Class<T> reqType, JsonValue jv, T defaultValue) {
    Object value = getValue(jv);
    if (reqType.isInstance(value)) {
      return reqType.cast(value);
    }
    return defaultValue;
  }

  /**
   * Get the value enclosed in a JSON value.
   *
   * @param reqType the required type
   * @param jv      the JSON value
   * @param <T>     the required type
   *
   * @return the value if possible
   *
   * @throws ClassCastException if the value is not of the required type
   */
  static <T> T getValue(Class<T> reqType, JsonValue jv) {
    return reqType.cast(getValue(jv));
  }

  /**
   * Test if the value is an array.
   *
   * @param jv the value
   *
   * @return true if the value is not null and is an array
   */
  static boolean isArray(JsonValue jv) {
    return jv != null && jv.getValueType() == ValueType.ARRAY;
  }

  /**
   * Test if the value is a Boolean.
   *
   * @param jv the value
   *
   * @return true if the value is not null and is an array
   */
  static boolean isBoolean(JsonValue jv) {
    return (jv != null) && (jv.getValueType() == ValueType.TRUE || jv.getValueType() == ValueType.FALSE);
  }

  /**
   * Test if the value is a JSON null.
   *
   * @param jv the value
   *
   * @return true if the value is not null and is a JSON null
   */
  static boolean isNull(JsonValue jv) {
    return jv != null && jv.getValueType() == ValueType.NULL;
  }

  /**
   * Test if the value is a number.
   *
   * @param jv the value
   *
   * @return true if the value is not null and is a number
   */
  static boolean isNumber(JsonValue jv) {
    return jv != null && jv.getValueType() == ValueType.NUMBER;
  }

  /**
   * Test if the value is a JSON object.
   *
   * @param jv the value
   *
   * @return true if the value is not null and is an array
   */
  static boolean isObject(JsonValue jv) {
    return jv != null && jv.getValueType() == ValueType.OBJECT;
  }

  /**
   * Test if the value is a string.
   *
   * @param jv the value
   *
   * @return true if the value is not null and is an array
   */
  static boolean isString(JsonValue jv) {
    return jv != null && jv.getValueType() == ValueType.STRING;
  }

  /**
   * Convert any number to a BigDecimal.
   *
   * @param n the number
   *
   * @return the BigDecimal
   */
  static BigDecimal toBigDecimal(Number n) {
    if (n == null) {
      return null;
    }
    if (n instanceof BigDecimal) {
      return (BigDecimal) n;
    }
    if (n instanceof BigInteger) {
      return new BigDecimal((BigInteger) n);
    }
    if (NumberParser.isPrimitiveIntegerType(n)) {
      return BigDecimal.valueOf(n.longValue());
    }
    if (n instanceof Double || n instanceof Float) {
      return BigDecimal.valueOf(n.doubleValue());
    }

    // unknown numeric type
    return new BigDecimal(n.toString());
  }

  /**
   * Convert any number to a BigInteger, possibly losing precision in the conversion.
   *
   * @param n the number
   *
   * @return the BigInteger
   */
  static BigInteger toBigInteger(Number n) {
    if (n == null) {
      return null;
    }
    if (n instanceof BigInteger) {
      return (BigInteger) n;
    }
    if (n instanceof BigDecimal) {
      return ((BigDecimal) n).toBigInteger();
    }
    if (NumberParser.isPrimitiveIntegerType(n)) {
      return BigInteger.valueOf(n.longValue());
    }
    return new BigDecimal(n.toString()).toBigInteger();
  }

  /**
   * Get the canonical JSON representation of the specified value.
   *
   * @param value the value
   *
   * @return the canonical JSON
   */
  static String toCanonicalString(Canonical value) {
    return (value != null) ? value.toCanonicalString() : CJNull.NULL.toCanonicalString();
  }

  /**
   * Convert text to a JSON Array.
   *
   * @param text the text to convert
   *
   * @return the JSON
   */
  static CJArray toJsonArray(String text) {
    try (
        CJReader reader = ReaderFactory.STANDARD.createReader(new StringReader(text))
    ) {
      return reader.readArray();
    }
  }

  /**
   * Convert text to a JSON Object.
   *
   * @param text the text to convert
   *
   * @return the JSON
   */
  static CJObject toJsonObject(String text) {
    try (
        CJReader reader = ReaderFactory.STANDARD.createReader(new StringReader(text))
    ) {
      return reader.readObject();
    }
  }

  /**
   * Get a pretty JSON representation of the specified value.
   *
   * @param value the value
   *
   * @return the pretty JSON
   */
  static String toPrettyString(Canonical value) {
    return (value != null) ? value.toPrettyString() : CJNull.NULL.toPrettyString();
  }

  /**
   * Convert any JSON to text.
   *
   * @param value the json value
   *
   * @return the textual representation
   */
  static String toText(JsonValue value) {
    StringWriter sw = new StringWriter();
    try (
        JsonWriter jw = WriterFactory.STANDARD.createWriter(sw);
    ) {
      jw.write(value);
    }
    return sw.toString();
  }

  @Override
  default CJArray asJsonArray() {
    try {
      return (CJArray) this;
    } catch (ClassCastException c) {
      IncorrectTypeException e = new IncorrectTypeException(ValueType.ARRAY, getValueType());
      e.initCause(c);
      throw e;
    }
  }


  @Override
  default CJObject asJsonObject() {
    try {
      return (CJObject) this;
    } catch (ClassCastException c) {
      IncorrectTypeException e = new IncorrectTypeException(ValueType.OBJECT, getValueType());
      e.initCause(c);
      throw e;
    }
  }


  /**
   * Get a copy of this. If this is immutable, then returns this. Otherwise, returns a deep copy.
   *
   * @return a copy of this
   */
  Canonical copy();


  /**
   * Get the value encapsulated by this instance.
   *
   * @return the value
   */
  @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
  Object getValue();


  /**
   * Get the value encapsulated by this instance.
   *
   * @param <T>          required type
   * @param reqType      the required type
   * @param defaultValue default value if type is not correct
   *
   * @return the value
   */
  @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
  <T> T getValue(Class<T> reqType, T defaultValue);


  /**
   * Get the value encapsulated by this primitive. Throws a ClassCastException if the type is incorrect.
   *
   * @param <T>     required type
   * @param reqType the required type
   *
   * @return the value
   */
  <T> T getValueSafe(Class<T> reqType);


  /**
   * Create the canonical textual JSON representation of this.
   *
   * @return the canonical JSON.
   */
  default String toCanonicalString() {
    return toString();
  }


  /**
   * Create the pretty JSON representation of this.
   *
   * @return the pretty JSON
   */
  default String toPrettyString() {
    return toString();
  }


  /**
   * Write this to the specified stream in UTF-8.
   *
   * @param out the stream
   *
   * @throws IOException if the write fails
   */
  default void writeTo(OutputStream out) throws IOException {
    Utf8Appendable utf8Appendable = new Utf8Appendable(out);
    writeTo(utf8Appendable);
    utf8Appendable.finish();
  }


  /**
   * Write this to the specified writer.
   *
   * @param writer the writer
   *
   * @throws IOException if writing fails
   */
  void writeTo(Appendable writer) throws IOException;

}
