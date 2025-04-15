package com.pippsford.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.pippsford.json.exception.IncorrectTypeException;
import com.pippsford.json.exception.NotJsonException;
import com.pippsford.json.primitive.CJFalse;
import com.pippsford.json.primitive.CJNull;
import com.pippsford.json.primitive.CJString;
import com.pippsford.json.primitive.numbers.CJNumber;
import jakarta.json.JsonNumber;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import org.junit.jupiter.api.Test;

public class CanonicalTest {

  @Test
  public void test() {
    Canonical canonical = Canonical.create("123");
    assertEquals(ValueType.STRING, canonical.getValueType());
    assertEquals("123", canonical.getValue());
    assertEquals(123, canonical.getValue(Number.class, 123));
    assertEquals("123", canonical.getValue(String.class, "abc"));

    assertEquals("123", canonical.getValueSafe(String.class));
    try {
      canonical.getValueSafe(Number.class);
      fail();
    } catch (ClassCastException e) {
      // correct
    }
  }


  @Test
  public void testAsJsonArray() {
    assertTrue(Canonical.toJsonArray("[1]").asJsonArray() instanceof List);
    assertThrows(IncorrectTypeException.class, () -> CJFalse.FALSE.asJsonArray());
  }


  @Test
  public void testCannotCreate() {
    Throwable t = assertThrows(NotJsonException.class, () -> Canonical.create(this.getClass()));
    assertEquals("Cannot represent instances of class java.lang.Class as JSON directly", t.getMessage());
  }


  @Test
  public void testCreate() {
    testCreate(ValueType.NULL, null, null);
    testCreate(ValueType.NULL, null, Canonical.NULL);
    testCreate(ValueType.TRUE, true, true);
    testCreate(ValueType.FALSE, false, false);
    testCreate(ValueType.FALSE, false, new AtomicBoolean(false));
    testCreate(ValueType.TRUE, true, new AtomicBoolean(true));
    testCreate(ValueType.STRING, "abc", "abc");
    testCreate(ValueType.NUMBER, 123, 123);
    testCreate(ValueType.NUMBER, 123, new AtomicLong(123));
    testCreate(ValueType.ARRAY, new CJArray(), new CJArray());
    testCreate(ValueType.OBJECT, new CJObject(), new CJObject());
    testCreate(ValueType.ARRAY, new CJArray(), new ArrayList<>());
    testCreate(ValueType.OBJECT, new CJObject(), new HashMap<>());
  }


  private void testCreate(ValueType type, Object check, Object value) {
    Canonical canonical = Canonical.create(value);
    assertEquals(type, canonical.getValueType());
    assertEquals(check, canonical.getValue());
  }


  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testEquals() {
    Canonical canonical = Canonical.create("123");
    canonical.hashCode();
    Canonical.NULL.hashCode();
    assertEquals(canonical, canonical);
    assertNotEquals(null, canonical);
    assertEquals(canonical, CJString.create("123"));
    assertNotEquals(canonical, CJString.create("456"));
    assertNotEquals(canonical, CJNumber.create(123));
    assertNotEquals(canonical, CJNull.NULL);
    assertEquals(Canonical.NULL, CJNull.NULL);
    assertNotEquals(Canonical.NULL, CJString.create("123"));
    assertNotEquals("null", Canonical.NULL);
  }


  @Test
  public void testGetValue() {
    assertTrue(Canonical.getValue(JsonValue.EMPTY_JSON_ARRAY) instanceof List<?>);
    assertTrue(Canonical.getValue(JsonValue.EMPTY_JSON_OBJECT) instanceof Map<?, ?>);
    assertTrue(Canonical.getValue(new CJArray()) instanceof List<?>);
    assertTrue(Canonical.getValue(new CJObject()) instanceof Map<?, ?>);

    assertEquals(Boolean.FALSE, Canonical.getValue(JsonValue.FALSE));
    assertNull(Canonical.getValue(JsonValue.NULL));
    assertNull(Canonical.getValue(null));
    assertEquals(Boolean.TRUE, Canonical.getValue(JsonValue.TRUE));

    JsonNumber number = mock(JsonNumber.class);
    when(number.numberValue()).thenReturn(123);
    when(number.getValueType()).thenReturn(ValueType.NUMBER);
    assertEquals(123, Canonical.getValue(number));

    JsonString string = mock(JsonString.class);
    when(string.getString()).thenReturn("abc");
    when(string.getValueType()).thenReturn(ValueType.STRING);
    assertEquals("abc", Canonical.getValue(string));
  }


  @Test
  public void testToBigDecimal() {
    assertNull(Canonical.toBigDecimal(null));

    BigDecimal bd = new BigDecimal(123);
    BigInteger bi = BigInteger.valueOf(123);
    assertSame(bd, Canonical.toBigDecimal(bd));
    assertEquals(bd, Canonical.toBigDecimal((byte) 123));
    assertEquals(bd, Canonical.toBigDecimal((short) 123));
    assertEquals(bd, Canonical.toBigDecimal(123));
    assertEquals(bd, Canonical.toBigDecimal((long) 123));
    assertEquals(bd, Canonical.toBigDecimal(bi));

    bd = new BigDecimal("0.5");
    assertEquals(bd, Canonical.toBigDecimal(0.5));
    assertEquals(bd, Canonical.toBigDecimal(0.5f));
  }


  @Test
  public void testToBigInteger() {
    assertNull(Canonical.toBigInteger(null));

    BigDecimal bd = new BigDecimal(123);
    BigInteger bi = BigInteger.valueOf(123);
    assertSame(bi, Canonical.toBigInteger(bi));
    assertEquals(bi, Canonical.toBigInteger((byte) 123));
    assertEquals(bi, Canonical.toBigInteger((short) 123));
    assertEquals(bi, Canonical.toBigInteger(123));
    assertEquals(bi, Canonical.toBigInteger((long) 123));
    assertEquals(bi, Canonical.toBigInteger(bd));

    assertEquals(bi, Canonical.toBigInteger(123.0));
    assertEquals(bi, Canonical.toBigInteger(123.0f));
  }


  @Test
  public void testToJsonArray() {
    CJArray array = Canonical.toJsonArray("[ 1, 2, 3 ]");
    assertEquals(List.of(1, 2, 3), array.getExternalValue());
    assertEquals("[1,2,3]", Canonical.toText(array));
  }


  @Test
  public void testToJsonObject() {
    CJObject object = Canonical.toJsonObject("{ \"b\":false, \"a\":1 }");
    assertEquals(Map.of("a", 1, "b", false), object.getExternalValue());
    assertEquals("{\"a\":1,\"b\":false}", Canonical.toText(object));
  }


  @Test
  public void testToString() {
    assertEquals("\"abc\"", CJString.create("abc").toString());
    assertEquals("true", Canonical.TRUE.toString());
    assertEquals("5.0E-1", CJNumber.cast(0.5).toString());
  }

}
