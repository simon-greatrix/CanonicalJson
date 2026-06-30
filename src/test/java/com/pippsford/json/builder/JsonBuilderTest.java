package com.pippsford.json.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pippsford.json.exception.NonFiniteNumberException;
import java.math.BigDecimal;
import java.math.BigInteger;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Greatrix on 18/06/2026.
 */
public class JsonBuilderTest {

  // --- Object-building overloads (add with name) ---

  @Test
  public void addObjectJsonValue() {
    JsonObject obj = JsonBuilder.add("a", JsonValue.TRUE).build();
    assertEquals("{\"a\":true}", obj.toString());
  }


  @Test
  public void addObjectString() {
    JsonObject obj = JsonBuilder.add("a", "hello").build();
    assertEquals("{\"a\":\"hello\"}", obj.toString());
  }


  @Test
  public void addObjectBigInteger() {
    JsonObject obj = JsonBuilder.add("a", BigInteger.valueOf(42)).build();
    assertEquals("{\"a\":42}", obj.toString());
  }


  @Test
  public void addObjectBigDecimal() {
    JsonObject obj = JsonBuilder.add("a", BigDecimal.valueOf(1, 2)).build();
    assertEquals("{\"a\":1.0E-2}", obj.toString());
  }


  @Test
  public void addObjectInt() {
    JsonObject obj = JsonBuilder.add("a", 7).build();
    assertEquals("{\"a\":7}", obj.toString());
  }


  @Test
  public void addObjectLong() {
    JsonObject obj = JsonBuilder.add("a", 4_000_000_000L).build();
    assertEquals("{\"a\":4000000000}", obj.toString());
  }


  @Test
  public void addObjectDouble() {
    JsonObject obj = JsonBuilder.add("a", 1.024).build();
    assertEquals("{\"a\":1.024E0}", obj.toString());
  }


  @Test
  public void addObjectDoubleNaN() {
    assertThrows(NonFiniteNumberException.class, () -> JsonBuilder.add("a", Double.NaN));
  }


  @Test
  public void addObjectDoubleInfinity() {
    assertThrows(NonFiniteNumberException.class, () -> JsonBuilder.add("a", Double.POSITIVE_INFINITY));
  }


  @Test
  public void addObjectBoolean() {
    JsonObject obj = JsonBuilder.add("a", false).build();
    assertEquals("{\"a\":false}", obj.toString());
  }


  @Test
  public void addObjectJsonObjectBuilder() {
    ObjectBuilder inner = new ObjectBuilder();
    inner.add("x", 1);
    JsonObject obj = JsonBuilder.add("a", inner).build();
    assertEquals("{\"a\":{\"x\":1}}", obj.toString());
  }


  @Test
  public void addObjectJsonArrayBuilder() {
    ArrayBuilder inner = new ArrayBuilder();
    inner.add("z").add(2);
    JsonObject obj = JsonBuilder.add("a", inner).build();
    assertEquals("{\"a\":[\"z\",2]}", obj.toString());
  }


  @Test
  public void addAllObjectBuilder() {
    ObjectBuilder src = new ObjectBuilder();
    src.add("x", 1).add("y", 2);
    JsonObject obj = JsonBuilder.addAll(src).build();
    assertEquals("{\"x\":1,\"y\":2}", obj.toString());
  }


  @Test
  public void addNullObject() {
    JsonObject obj = JsonBuilder.addNull("a").build();
    assertEquals("{\"a\":null}", obj.toString());
  }


  // --- Array-building overloads (add without name) ---

  @Test
  public void addArrayJsonValue() {
    JsonArray arr = JsonBuilder.add(JsonValue.EMPTY_JSON_OBJECT).build();
    assertEquals("[{}]", arr.toString());
  }


  @Test
  public void addArrayString() {
    JsonArray arr = JsonBuilder.add("world").build();
    assertEquals("[\"world\"]", arr.toString());
  }


  @Test
  public void addArrayBigDecimal() {
    JsonArray arr = JsonBuilder.add(BigDecimal.TEN).build();
    assertEquals("[10]", arr.toString());
  }


  @Test
  public void addArrayBigInteger() {
    JsonArray arr = JsonBuilder.add(BigInteger.TEN).build();
    assertEquals("[10]", arr.toString());
  }


  @Test
  public void addArrayInt() {
    JsonArray arr = JsonBuilder.add(99).build();
    assertEquals("[99]", arr.toString());
  }


  @Test
  public void addArrayLong() {
    JsonArray arr = JsonBuilder.add(1234567890123456789L).build();
    assertEquals("[1234567890123456789]", arr.toString());
  }


  @Test
  public void addArrayDouble() {
    JsonArray arr = JsonBuilder.add(20.48).build();
    assertEquals("[2.048E1]", arr.toString());
  }


  @Test
  public void addArrayDoubleNaN() {
    assertThrows(NonFiniteNumberException.class, () -> JsonBuilder.add(Double.NaN));
  }


  @Test
  public void addArrayDoubleInfinity() {
    assertThrows(NonFiniteNumberException.class, () -> JsonBuilder.add(Double.NEGATIVE_INFINITY));
  }


  @Test
  public void addArrayBoolean() {
    JsonArray arr = JsonBuilder.add(true).build();
    assertEquals("[true]", arr.toString());
  }


  @Test
  public void addArrayJsonObjectBuilder() {
    ObjectBuilder inner = new ObjectBuilder();
    inner.add("k", 5);
    JsonArray arr = JsonBuilder.add(inner).build();
    assertEquals("[{\"k\":5}]", arr.toString());
  }


  @Test
  public void addArrayJsonArrayBuilder() {
    ArrayBuilder inner = new ArrayBuilder();
    inner.add(1).add(2);
    JsonArray arr = JsonBuilder.add(inner).build();
    assertEquals("[[1,2]]", arr.toString());
  }


  @Test
  public void addAllArrayBuilder() {
    ArrayBuilder src = new ArrayBuilder();
    src.add(10).add(20);
    JsonArray arr = JsonBuilder.addAll(src).build();
    assertEquals("[10,20]", arr.toString());
  }


  @Test
  public void addNullArray() {
    JsonArray arr = JsonBuilder.addNull().build();
    assertEquals("[null]", arr.toString());
  }

}
