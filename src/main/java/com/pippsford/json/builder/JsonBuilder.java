package com.pippsford.json.builder;

import com.pippsford.json.exception.NonFiniteNumberException;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import java.math.BigDecimal;
import java.math.BigInteger;

/** A utility class for starting a build operation. */
public class JsonBuilder {

  /**
   * Adds a name/{@code JsonValue} pair to the JSON object associated with
   * this object builder. If the object contains a mapping for the specified
   * name, this method replaces the old value with the specified value.
   *
   * @param name  name in the name/value pair
   * @param value value in the name/value pair
   *
   * @return a new object builder
   * @throws NullPointerException if the specified name is null
   */
  public static ObjectBuilder add(String name, JsonValue value) {
    return new ObjectBuilder().add(name, value);
  }


  /**
   * Adds a name/{@code JsonString} pair to the JSON object associated with
   * this object builder. If the object contains a mapping for the specified
   * name, this method replaces the old value with the specified value.
   *
   * @param name  name in the name/value pair
   * @param value value in the name/value pair
   *
   * @return a new object builder
   * @throws NullPointerException if the specified name is null
   */
  public static ObjectBuilder add(String name, String value) {
    return new ObjectBuilder().add(name, value);
  }


  /**
   * Adds a name/{@code JsonNumber} pair to the JSON object associated with
   * this object builder. If the object contains a mapping for the specified
   * name, this method replaces the old value with the specified value.
   *
   * @param name  name in the name/value pair
   * @param value value in the name/value pair
   *
   * @return a new object builder
   * @throws NullPointerException if the specified name is null
   * @see jakarta.json.JsonNumber
   */
  public static ObjectBuilder add(String name, BigInteger value) {
    return new ObjectBuilder().add(name, value);
  }


  /**
   * Adds a name/{@code JsonNumber} pair to the JSON object associated with
   * this object builder. If the object contains a mapping for the specified
   * name, this method replaces the old value with the specified value.
   *
   * @param name  name in the name/value pair
   * @param value value in the name/value pair
   *
   * @return a new object builder
   * @throws NullPointerException if the specified name is null
   * @see jakarta.json.JsonNumber
   */
  public static ObjectBuilder add(String name, BigDecimal value) {
    return new ObjectBuilder().add(name, value);
  }


  /**
   * Adds a name/{@code JsonNumber} pair to the JSON object associated with
   * this object builder. If the object contains a mapping for the specified
   * name, this method replaces the old value with the specified value.
   *
   * @param name  name in the name/value pair
   * @param value value in the name/value pair
   *
   * @return a new object builder
   * @throws NullPointerException if the specified name is null
   * @see jakarta.json.JsonNumber
   */
  public static ObjectBuilder add(String name, int value) {
    return new ObjectBuilder().add(name, value);
  }


  /**
   * Adds a name/{@code JsonNumber} pair to the JSON object associated with
   * this object builder. If the object contains a mapping for the specified
   * name, this method replaces the old value with the specified value.
   *
   * @param name  name in the name/value pair
   * @param value value in the name/value pair
   *
   * @return a new object builder
   * @throws NullPointerException if the specified name is null
   * @see jakarta.json.JsonNumber
   */
  public static ObjectBuilder add(String name, long value) {
    return new ObjectBuilder().add(name, value);
  }


  /**
   * Adds a name/{@code JsonNumber} pair to the JSON object associated with
   * this object builder. If the object contains a mapping for the specified
   * name, this method replaces the old value with the specified value.
   *
   * @param name  name in the name/value pair
   * @param value value in the name/value pair
   *
   * @return a new object builder
   * @throws NonFiniteNumberException if the value is Not-a-Number (NaN) or infinity
   * @throws NullPointerException     if the specified name is null
   * @see jakarta.json.JsonNumber
   */
  public static ObjectBuilder add(String name, double value) {
    return new ObjectBuilder().add(name, value);
  }


  /**
   * Adds a name/{@code JsonValue#TRUE} or name/{@code JsonValue#FALSE} pair
   * to the JSON object associated with this object builder. If the object
   * contains a mapping for the specified name, this method replaces the old
   * value with the specified value.
   *
   * @param name  name in the name/value pair
   * @param value value in the name/value pair
   *
   * @return a new object builder
   * @throws NullPointerException if the specified name is null
   */
  public static ObjectBuilder add(String name, boolean value) {
    return new ObjectBuilder().add(name, value);
  }


  /**
   * Adds a name/{@code JsonObject} pair to the JSON object associated
   * with this object builder. The value {@code JsonObject} is built from the
   * specified object builder. If the object contains a mapping for the
   * specified name, this method replaces the old value with the
   * {@code JsonObject} from the specified object builder.
   *
   * @param name    name in the name/value pair
   * @param builder the value is the object associated with this builder
   *
   * @return a new object builder
   * @throws NullPointerException if the specified name or builder is null
   */
  public static ObjectBuilder add(String name, JsonObjectBuilder builder) {
    return new ObjectBuilder().add(name, builder.build());
  }


  /**
   * Adds a name/{@code JsonArray} pair to the JSON object associated with
   * this object builder. The value {@code JsonArray} is built from the
   * specified array builder. If the object contains a mapping for the
   * specified name, this method replaces the old value with the
   * {@code JsonArray} from the specified array builder.
   *
   * @param name    the name in the name/value pair
   * @param builder the value is the object array with this builder
   *
   * @return a new object builder
   * @throws NullPointerException if the specified name or builder is null
   */
  public static ObjectBuilder add(String name, JsonArrayBuilder builder) {
    return new ObjectBuilder().add(name, builder.build());
  }


  /**
   * Adds a value to the array.
   *
   * @param value the JSON value
   *
   * @return a new array builder
   */
  public static ArrayBuilder add(JsonValue value) {
    return new ArrayBuilder().add(value);
  }


  /**
   * Adds a value to the array as a {@link jakarta.json.JsonString}.
   *
   * @param value the string value
   *
   * @return a new array builder
   */
  public static ArrayBuilder add(String value) {
    return new ArrayBuilder().add(value);
  }


  /**
   * Adds a value to the array as a {@link jakarta.json.JsonNumber}.
   *
   * @param value the number value
   *
   * @return a new array builder
   * @see jakarta.json.JsonNumber
   */
  public static ArrayBuilder add(BigDecimal value) {
    return new ArrayBuilder().add(value);
  }


  /**
   * Adds a value to the array as a {@link jakarta.json.JsonNumber}.
   *
   * @param value the number value
   *
   * @return a new array builder
   * @see jakarta.json.JsonNumber
   */
  public static ArrayBuilder add(BigInteger value) {
    return new ArrayBuilder().add(value);
  }


  /**
   * Adds a value to the array as a {@link jakarta.json.JsonNumber}.
   *
   * @param value the number value
   *
   * @return a new array builder
   * @see jakarta.json.JsonNumber
   */
  public static ArrayBuilder add(int value) {
    return new ArrayBuilder().add(value);
  }


  /**
   * Adds a value to the array as a {@link jakarta.json.JsonNumber}.
   *
   * @param value the number value
   *
   * @return a new array builder
   * @see jakarta.json.JsonNumber
   */
  public static ArrayBuilder add(long value) {
    return new ArrayBuilder().add(value);
  }


  /**
   * Adds a value to the array as a {@link jakarta.json.JsonNumber}.
   *
   * @param value the number value
   *
   * @return a new array builder
   * @throws NonFiniteNumberException if the value is Not-a-Number (NaN) or infinity
   * @see jakarta.json.JsonNumber
   */
  public static ArrayBuilder add(double value) {
    return new ArrayBuilder().add(value);
  }


  /**
   * Adds a {@link JsonValue#TRUE} or {@link JsonValue#FALSE} value to the array.
   *
   * @param value the boolean value
   *
   * @return a new array builder
   */
  public static ArrayBuilder add(boolean value) {
    return new ArrayBuilder().add(value);
  }


  /**
   * Adds a {@link jakarta.json.JsonObject} from an object builder to the array.
   *
   * @param builder the object builder
   *
   * @return a new array builder
   * @throws NullPointerException if the specified builder is null
   */
  public static ArrayBuilder add(JsonObjectBuilder builder) {
    return new ArrayBuilder().add(builder);
  }


  /**
   * Adds a {@link jakarta.json.JsonArray} from an array builder to the array.
   *
   * @param builder the array builder
   *
   * @return a new array builder
   * @throws NullPointerException if the specified builder is null
   */
  public static ArrayBuilder add(JsonArrayBuilder builder) {
    return new ArrayBuilder().add(builder);
  }


  /**
   * Adds all name/value pairs in the JSON object associated with the specified
   * object builder to the JSON object associated with this object builder.
   * The newly added name/value pair will replace any existing name/value pair with
   * the same name.
   *
   * @param builder the specified object builder
   *
   * @return a new object builder
   * @throws NullPointerException if the specified builder is null
   */
  public static ObjectBuilder addAll(JsonObjectBuilder builder) {
    return new ObjectBuilder().addAll(builder);
  }


  /**
   * Adds all elements of the array in the specified array builder to the array.
   *
   * @param builder the array builder
   *
   * @return a new array builder
   * @throws NullPointerException if the specified builder is null
   */
  public static ArrayBuilder addAll(JsonArrayBuilder builder) {
    return new ArrayBuilder().addAll(builder);
  }


  /**
   * Adds a name/{@code JsonValue#NULL} pair to the JSON object associated
   * with this object builder where the value is {@code null}.
   * If the object contains a mapping for the specified name, this method
   * replaces the old value with {@code null}.
   *
   * @param name name in the name/value pair
   *
   * @return a new object builder
   */
  public static ObjectBuilder addNull(String name) {
    return new ObjectBuilder().addNull(name);
  }


  /**
   * Adds a {@link JsonValue#NULL} value to the array.
   *
   * @return a new array builder
   */
  public static ArrayBuilder addNull() {
    return new ArrayBuilder().addNull();
  }


  private JsonBuilder() {
  }

}
