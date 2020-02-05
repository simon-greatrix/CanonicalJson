package io.setl.json;

import static io.setl.json.JArray.fixCollection;
import static io.setl.json.JArray.fixPrimitiveCollection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.setl.json.exception.IncorrectTypeException;
import io.setl.json.exception.MissingItemException;
import io.setl.json.primitive.PTrue;
import io.setl.json.primitive.numbers.PInt;
import io.setl.json.primitive.numbers.PNumber;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Spliterator;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import org.junit.Test;

public class JArrayTest {

  @Test
  public void indexOf() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3, null, 2, 3, null));
    assertEquals(1, ja.indexOf(2));
    assertEquals(2, ja.indexOf(PNumber.create(3)));
    assertEquals(3, ja.indexOf(null));
  }


  @Test
  public void isNull() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3, null));
    assertFalse(ja.isNull(2));
    assertTrue(ja.isNull(3));
  }


  @Test
  public void lastIndexOf() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3, null, 2, 3, null));
    assertEquals(4, ja.lastIndexOf(2));
    assertEquals(5, ja.lastIndexOf(PNumber.create(3)));
    assertEquals(6, ja.lastIndexOf(null));
  }


  @Test
  public void optValue() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3, null));
    assertNull(ja.optValue(-3));
    assertNotNull(ja.optValue(3));
  }


  @Test
  public void optimiseStorage() {
    JArray ja1 = new JArray(Arrays.asList(1, 1, 2));
    JArray ja2 = new JArray(Arrays.asList(1, 2, 3, ja1, JsonValue.EMPTY_JSON_OBJECT));
    ja2.optimiseStorage();
    assertSame(ja1.get(0), ja1.get(1));
    assertSame(ja1.get(0), ja2.get(0));
    assertSame(ja1.get(2), ja2.get(1));
    assertEquals("[1,2,3,[1,1,2],{}]", ja2.toString());
  }


  @Test
  public void parallelStream() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3, null));
    assertNotNull(ja.parallelStream());
    assertEquals(4, ja.parallelStream().count());
  }


  @Test
  public void remove() {
    JArray ja = new JArray(Arrays.asList(1, "2", 3, null));
    assertTrue(ja.remove("2"));
    assertFalse(ja.remove("2"));
  }


  @Test
  public void removeAll() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3, 4, 5, 6));
    assertTrue(ja.removeAll(Arrays.asList(1, 2, 3)));
    assertFalse(ja.removeAll(Arrays.asList(1, 2, 3)));
  }


  @Test
  public void sort() {
    JArray ja = new JArray(Arrays.asList(3, 1, 4, 1, 5, 2, null, 9, 6));
    ja.sort(Comparator.comparing(JsonValue::toString));
    assertEquals("[1,1,2,3,4,5,6,9,null]", ja.toString());
  }


  @Test
  public void stream() {
    JArray ja = new JArray(Arrays.asList(3, 1, 4, 1, 5, 2, null, 9, 6));
    assertEquals(ja.size(), ja.stream().count());
  }


  @Test
  public void subList() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    List<JsonValue> ja2 = ja.subList(4, 7);
    ja2.set(1, Primitive.create("5"));
    assertEquals("[0,1,2,3,4,\"5\",6,7,8,9]", ja.toString());
  }


  @Test
  public void testAddAllCollectionOfQextendsPrimitive() {
    JArray ja = new JArray();
    ja.addAll(Arrays.asList(Primitive.TRUE, null));
    assertEquals(Primitive.TRUE, ja.get(0));
    assertEquals(Primitive.NULL, ja.get(1));
  }


  @Test
  public void testAddAllIntCollectionOfQextendsPrimitive() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    ja.addAll(1, Arrays.asList(Primitive.TRUE, null));
    assertEquals(Primitive.TRUE, ja.get(1));
    assertEquals(Primitive.NULL, ja.get(2));
  }


  @Test
  public void testAddBoolean() {
    JArray ja = new JArray();
    ja.add(false);
    ja.add(true);
    ja.add((Boolean) null);
    assertEquals(3, ja.size());
    assertEquals(Boolean.FALSE, ja.optBoolean(0));
    assertEquals(Boolean.TRUE, ja.optBoolean(1));
    assertNull(ja.optBoolean(2));
  }


  @Test
  public void testAddIntBoolean() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    ja.add(2, true);
    ja.add(2, false);
    ja.add(2, (Boolean) null);
    assertEquals(1, ja.getInt(1, -1));
    assertNull(ja.optBoolean(2));
    assertFalse(ja.optBoolean(3));
    assertTrue(ja.optBoolean(4));
    assertEquals(2, ja.getInt(5, -1));
  }


  @Test
  public void testAddIntJsonArray() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    JArray ja2 = new JArray(Arrays.asList(8, 9));
    ja.add(2, ja2);
    ja.add(2, (JArray) null);
    assertEquals(1, ja.getInt(1, -1));
    assertNull(ja.optArray(2));
    assertEquals(ja2, ja.optArray(3));
    assertEquals(2, ja.getInt(4, -1));
  }


  @Test
  public void testAddIntJsonObject() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    JObject jo = new JObject(Collections.singletonMap("a", 1));
    ja.add(2, jo);
    ja.add(2, (JObject) null);
    assertEquals(1, ja.getInt(1, -1));
    assertNull(ja.optObject(2));
    assertEquals(jo, ja.optObject(3));
    assertEquals(2, ja.getInt(4, -1));
  }


  @Test
  public void testAddIntNumber() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    ja.add(2, 10);
    ja.add(2, (Number) null);
    assertEquals(1, ja.getInt(1, -1));
    assertNull(ja.optInt(2));
    assertEquals(10, ja.getInt(3, -1));
    assertEquals(2, ja.getInt(4, -1));
  }


  @Test
  public void testAddIntPrimitive() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    ja.add(2, Primitive.TRUE);
    ja.add(2, (Primitive) null);
    assertEquals(1, ja.getInt(1, -1));
    assertEquals(Primitive.NULL, ja.get(2));
    assertEquals(Primitive.TRUE, ja.get(3));
    assertEquals(2, ja.getInt(4, -1));
  }


  @Test
  public void testAddIntString() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    ja.add(2, "abc");
    ja.add(2, (String) null);
    assertEquals(1, ja.getInt(1, -1));
    assertEquals(Primitive.NULL, ja.get(2));
    assertEquals("abc", ja.optString(3));
    assertEquals(2, ja.getInt(4, -1));
  }


  @Test
  public void testAddJsonArray() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    JArray ja2 = new JArray(Arrays.asList(8, 9));
    ja.add(ja2);
    ja.add((JArray) null);
    assertEquals(2, ja.getInt(2, -1));
    assertEquals(ja2, ja.optArray(3));
    assertNull(ja.optArray(4));
  }


  @Test
  public void testAddJsonObject() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    JObject jo = new JObject(Collections.singletonMap("a", 1));
    ja.add(jo);
    ja.add((JObject) null);
    assertEquals(2, ja.getInt(2, -1));
    assertEquals(jo, ja.optObject(3));
    assertNull(ja.optObject(4));
  }


  @Test
  public void testAddNull() {
    JArray ja = new JArray();
    ja.addNull();
    assertEquals(1, ja.size());
    assertEquals(Primitive.NULL, ja.get(0));
  }


  @Test
  public void testAddNullInt() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    ja.addNull(2);
    assertEquals(1, ja.getInt(1, -1));
    assertNull(ja.optInt(2));
    assertEquals(2, ja.getInt(3, -1));
  }


  @Test
  public void testAddNumber() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    ja.add(3);
    ja.add((Number) null);
    assertEquals(2, ja.getInt(2, -1));
    assertEquals(3, ja.getInt(3, -1));
    assertNull(ja.optInt(4));
  }


  @Test
  public void testAddPrimitive() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    ja.add(Primitive.TRUE);
    ja.add((Primitive) null);
    assertEquals(2, ja.getInt(2, -1));
    assertEquals(Primitive.TRUE, ja.get(3));
    assertEquals(Primitive.NULL, ja.get(4));
  }


  @Test
  public void testAddString() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    ja.add("abc");
    ja.add((String) null);
    assertEquals(2, ja.getInt(2, -1));
    assertEquals("abc", ja.optString(3));
    assertNull(ja.optString(4));
  }


  @Test
  public void testAsJsonArray() {
    JArray ja = new JArray();
    assertSame(ja, ja.asJsonArray());
  }


  @Test(expected = ClassCastException.class)
  public void testAsJsonObject() {
    JArray ja = new JArray();
    ja.asJsonObject();
  }


  @Test
  public void testContains() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    assertTrue(ja.contains(0));
    assertTrue(ja.contains(PNumber.create(1)));
    assertFalse(ja.contains("x"));
  }


  @Test
  public void testContainsAll() {
    JArray ja = new JArray(Arrays.asList(0, null, true, 1, 2));
    assertTrue(ja.containsAll(Arrays.asList(JsonValue.NULL, true, 2)));
    assertFalse(ja.containsAll(Arrays.asList(JsonValue.NULL, true, 2, 3, 4, 5)));
  }


  @Test
  public void testFixCollection() {
    JArray ja = new JArray();
    assertSame(ja, fixCollection(ja));
    ja.add("abc");
    ja.add(123);
    ja.addNull();
    ja.add(true);
    assertEquals(ja, fixCollection(Arrays.asList("abc", PNumber.create(123), null, JsonValue.TRUE)));
  }


  @Test
  public void testFixPrimitiveCollection() {
    JArray ja = new JArray();
    ja.add(true);
    ja.addNull();
    ja.add(123);

    Collection<? extends JsonValue> fixed = fixPrimitiveCollection(Arrays.asList(Primitive.TRUE, null, Primitive.create(123)));
    assertEquals(ja, fixed);
  }


  @Test
  public void testGetArrayInt() {
    List<Integer> int1 = Arrays.asList(8, 9);
    List<Integer> int2 = Arrays.asList(-8, -9);

    JArray ja = new JArray(Arrays.asList(0, int1, 4));
    JArray j1 = new JArray(int1);
    JArray j2 = new JArray(int2);

    assertNull(ja.optArray(0));
    assertEquals(j1, ja.optArray(1));

    assertEquals(j2, ja.getArray(0, j2));
    assertEquals(j1, ja.getArray(1, j2));

    assertEquals(j2, ja.getArray(0, k -> j2));
    assertEquals(j1, ja.getArray(1, k -> j2));

    assertEquals(j1, ja.getArray(1));
    try {
      ja.getArray(0);
      fail();
    } catch (IncorrectTypeException e) {
      // correct
    }
    try {
      ja.getArray(10);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
    try {
      ja.getArray(-1);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
  }


  @Test
  public void testGetBigDecimalInt() {
    BigDecimal bd1 = new BigDecimal(123);
    BigDecimal bd2 = new BigDecimal(789);

    JArray ja = new JArray(Arrays.asList("a", bd1, "d"));

    assertNull(ja.optBigDecimal(0));
    assertEquals(bd1, ja.optBigDecimal(1));

    assertEquals(bd2, ja.getBigDecimal(0, bd2));
    assertEquals(bd1, ja.getBigDecimal(1, bd2));

    assertEquals(bd2, ja.getBigDecimal(0, k -> bd2));
    assertEquals(bd1, ja.getBigDecimal(1, k -> bd2));

    assertEquals(bd1, ja.getBigDecimal(1));
    try {
      ja.getBigDecimal(0);
      fail();
    } catch (IncorrectTypeException e) {
      // correct
    }
    try {
      ja.getBigDecimal(10);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
    try {
      ja.getBigDecimal(-1);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
  }


  @Test
  public void testGetBigInteger() {
    BigInteger bd1 = new BigInteger("123");
    BigInteger bd2 = new BigInteger("789");

    JArray ja = new JArray(Arrays.asList("a", bd1, "d"));

    assertNull(ja.optBigInteger(0));
    assertEquals(bd1, ja.optBigInteger(1));

    assertEquals(bd2, ja.getBigInteger(0, bd2));
    assertEquals(bd1, ja.getBigInteger(1, bd2));

    assertEquals(bd2, ja.getBigInteger(0, k -> bd2));
    assertEquals(bd1, ja.getBigInteger(1, k -> bd2));

    assertEquals(bd1, ja.getBigInteger(1));
    try {
      ja.getBigInteger(0);
      fail();
    } catch (IncorrectTypeException e) {
      // correct
    }
    try {
      ja.getBigInteger(10);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
    try {
      ja.getBigInteger(-1);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
  }


  @Test
  public void testGetBooleanInt() {
    JArray ja = new JArray(Arrays.asList("a", true, "d"));

    assertNull(ja.optBoolean(0));
    assertEquals(Boolean.TRUE, ja.optBoolean(1));

    assertEquals(Boolean.FALSE, ja.getBoolean(0, Boolean.FALSE));
    assertEquals(Boolean.TRUE, ja.getBoolean(1, Boolean.FALSE));

    assertEquals(Boolean.FALSE, ja.getBoolean(0, k -> Boolean.FALSE));
    assertEquals(Boolean.FALSE, ja.getBoolean(-5, k -> Boolean.FALSE));
    assertEquals(Boolean.FALSE, ja.getBoolean(10, k -> Boolean.FALSE));
    assertEquals(Boolean.TRUE, ja.getBoolean(1, k -> Boolean.FALSE));

    assertEquals(Boolean.TRUE, ja.getBoolean(1));
    try {
      ja.getBoolean(0);
      fail();
    } catch (IncorrectTypeException e) {
      // correct
    }
    try {
      ja.getBoolean(10);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
    try {
      ja.getBoolean(-1);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
  }


  @Test
  public void testGetDoubleInt() {
    JArray ja = new JArray(Arrays.asList("a", 3.5, "d"));

    assertNull(ja.optDouble(0));
    assertEquals(3.5, ja.optDouble(1), 0.0000001);

    assertEquals(-1.0, ja.getDouble(0, -1.0), 0.0000001);
    assertEquals(3.5, ja.getDouble(1, -1.0), 0.0000001);

    assertEquals(-1.0, ja.getDouble(0, k -> -1.0), 0.0000001);
    assertEquals(3.5, ja.getDouble(1, k -> -1.0), 0.0000001);

    assertEquals(3.5, ja.getDouble(1), 0.0000001);
    try {
      ja.getDouble(0);
      fail();
    } catch (IncorrectTypeException e) {
      // correct
    }
    try {
      ja.getDouble(10);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
    try {
      ja.getDouble(-1);
      fail();
    } catch (MissingItemException e) {
      // correct
    }

    ja = new JArray(Arrays.asList(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
    assertEquals("[\"NaN\",\"-Infinity\",\"Infinity\",\"NaN\",\"-Infinity\",\"Infinity\"]", ja.toString());
    assertTrue(Double.isNaN(ja.optDouble(0)));
    assertTrue(Double.isInfinite(ja.getDouble(1)));
    assertTrue(Double.isInfinite(ja.getDouble(2, 5.0)));
  }


  @Test
  public void testGetInt() {
    JArray ja = new JArray(Arrays.asList("a", 35, "d"));

    assertNull(ja.optInt(0));
    assertEquals(Integer.valueOf(35), ja.optInt(1));

    assertEquals(-1, ja.getInt(0, -1));
    assertEquals(35, ja.getInt(1, -1));

    assertEquals(-1, ja.getInt(0, k -> -1));
    assertEquals(35, ja.getInt(1, k -> -1));

    assertEquals(35, ja.getInt(1));
    try {
      ja.getInt(0);
      fail();
    } catch (IncorrectTypeException e) {
      // correct
    }
    try {
      ja.getInt(10);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
    try {
      ja.getInt(-1);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
  }


  @Test
  public void testGetJsonArray() {
    JArray ja = new JArray(Arrays.asList(JsonValue.EMPTY_JSON_ARRAY, JsonValue.EMPTY_JSON_OBJECT, 123, "abc"));
    assertNotNull(ja.getJsonArray(0));
  }


  @Test
  public void testGetJsonNumber() {
    JArray ja = new JArray(Arrays.asList(JsonValue.EMPTY_JSON_ARRAY, JsonValue.EMPTY_JSON_OBJECT, 123, "abc"));
    assertNotNull(ja.getJsonNumber(2));
  }


  @Test
  public void testGetJsonObject() {
    JArray ja = new JArray(Arrays.asList(JsonValue.EMPTY_JSON_ARRAY, JsonValue.EMPTY_JSON_OBJECT, 123, "abc"));
    assertNotNull(ja.getJsonObject(1));
  }


  @Test
  public void testGetJsonString() {
    JArray ja = new JArray(Arrays.asList(JsonValue.EMPTY_JSON_ARRAY, JsonValue.EMPTY_JSON_OBJECT, 123, "abc"));
    assertNotNull(ja.getJsonString(3));
  }


  @Test
  public void testGetLong() {
    JArray ja = new JArray(Arrays.asList("a", 35, "d"));

    assertNull(ja.optLong(0));
    assertEquals(Long.valueOf(35), ja.optLong(1));

    assertEquals(-1, ja.getLong(0, -1));
    assertEquals(35, ja.getLong(1, -1));

    assertEquals(-1, ja.getLong(0, k -> -1));
    assertEquals(35, ja.getLong(1, k -> -1));

    assertEquals(35, ja.getLong(1));
    try {
      ja.getLong(0);
      fail();
    } catch (IncorrectTypeException e) {
      // correct
    }
    try {
      ja.getLong(10);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
    try {
      ja.getLong(-1);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
  }


  @Test
  public void testGetObject() {
    Map<String, String> map1 = Collections.singletonMap("a", "a");
    Map<String, Number> map2 = Collections.singletonMap("b", 1);

    JArray ja = new JArray(Arrays.asList(0, map1, 4));
    JObject j1 = new JObject(map1);
    JObject j2 = new JObject(map2);

    assertNull(ja.optObject(0));
    assertEquals(j1, ja.optObject(1));

    assertEquals(j2, ja.getObject(0, j2));
    assertEquals(j1, ja.getObject(1, j2));

    assertEquals(j2, ja.getObject(0, k -> j2));
    assertEquals(j1, ja.getObject(1, k -> j2));

    assertEquals(j1, ja.getObject(1));
    try {
      ja.getObject(0);
      fail();
    } catch (IncorrectTypeException e) {
      // correct
    }
    try {
      ja.getObject(10);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
    try {
      ja.getObject(-1);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
  }


  @Test
  public void testGetStringInt() {
    JArray ja = new JArray(Arrays.asList(1, "xyz", 2));

    assertNull(ja.optString(0));
    assertEquals("xyz", ja.optString(1));

    assertEquals("abc", ja.getString(0, "abc"));
    assertEquals("xyz", ja.getString(1, "abc"));

    assertEquals("abc", ja.getString(0, k -> "abc"));
    assertEquals("xyz", ja.getString(1, k -> "abc"));

    assertEquals("xyz", ja.getString(1));
    try {
      ja.getString(0);
      fail();
    } catch (IncorrectTypeException e) {
      // correct
    }
    try {
      ja.getString(10);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
    try {
      ja.getString(-1);
      fail();
    } catch (MissingItemException e) {
      // correct
    }
  }


  @Test
  public void testGetValue2() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    assertNotNull(ja.getValue(List.class, null));
    assertNull(ja.getValue(Map.class, null));
  }


  @Test(expected = ClassCastException.class)
  public void testGetValueAs() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2));
    ja.getValuesAs(JsonString.class);
  }


  @Test
  public void testHashCode() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    int h = ja.hashCode();
    ja.add(4);
    assertNotEquals(h, ja.hashCode());
  }


  @Test
  public void testJsonArray() {
    JArray ja = new JArray();
    assertTrue(ja.isEmpty());
    assertEquals(0, ja.size());
  }


  @Test
  public void testJsonArrayCollectionOfQ() {
    JArray ja = new JArray(Arrays.asList("abc", 123, true, null));
    assertEquals(4, ja.size());
    assertEquals(123, ja.getInt(1, -1));
  }


  @Test
  public void testListIterator() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    ListIterator<JsonValue> iter = ja.listIterator();
    assertTrue(iter.hasNext());
    assertFalse(iter.hasPrevious());
    assertEquals(0, iter.nextIndex());
    assertEquals(-1, iter.previousIndex());
    assertEquals(PNumber.create(1), iter.next());
    iter.remove();
    assertEquals(2, ja.size());
    iter.next();
    iter.set(JsonValue.TRUE);

    Primitive p = ja.getPrimitive(0);
    assertTrue(p instanceof PTrue);

    iter.add(JsonValue.EMPTY_JSON_ARRAY);
    iter.previous();
    assertEquals("[true,[],3]", ja.toString());
  }


  @Test
  public void testRemoveIf() {
    JArray ja = new JArray(Arrays.asList(1, "2", 3));
    ja.removeIf(jv -> jv.getValueType() == ValueType.NUMBER);
    assertEquals(1, ja.size());
  }


  @Test
  public void testReplaceAllUnaryOperatorOfPrimitive() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    ja.replaceAll(p -> Primitive.create(p.toString()));
    assertEquals("1", ja.optString(0));
  }


  @Test
  public void testRetainAll() {
    JArray ja = new JArray(Arrays.asList(1, "2", 3, 4, 5));
    ja.retainAll(Arrays.asList(1, 2, 3, 4, 5));
    assertEquals(4, ja.size());
    assertFalse(ja.contains("2"));
  }


  @Test
  public void testSetArray() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    JArray j2 = new JArray(Arrays.asList(4, 5, 6));
    ja.set(0, (JArray) null);
    ja.set(1, j2);
    assertEquals(Primitive.NULL, ja.get(0));
    assertEquals(j2, ja.optArray(1));
  }


  @Test
  public void testSetBoolean() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    ja.set(0, (Boolean) null);
    ja.set(1, true);
    ja.set(2, false);
    assertEquals(Primitive.NULL, ja.get(0));
    assertTrue(ja.optBoolean(1));
    assertFalse(ja.optBoolean(2));
  }


  @Test
  public void testSetNull() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    ja.setNull(0);
    assertEquals(Primitive.NULL, ja.get(0));
  }


  @Test
  public void testSetNumber() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    ja.set(0, (Number) null);
    ja.set(1, 123456);
    assertEquals(Primitive.NULL, ja.get(0));
    assertEquals(123456, ja.getInt(1, -1));
  }


  @Test
  public void testSetObject() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    JObject j2 = new JObject(Collections.singletonMap("x", "y"));
    ja.set(0, (JObject) null);
    ja.set(1, j2);
    assertEquals(Primitive.NULL, ja.get(0));
    assertEquals(j2, ja.optObject(1));
  }


  @Test
  public void testSetPrimitive() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    ja.set(0, (Primitive) null);
    ja.set(1, Primitive.TRUE);
    assertEquals(Primitive.NULL, ja.get(0));
    assertEquals(Primitive.TRUE, ja.get(1));
  }


  @Test
  public void testSetString() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    ja.set(0, (String) null);
    ja.set(1, "xyz");
    assertEquals(Primitive.NULL, ja.get(0));
    assertEquals("xyz", ja.optString(1));
  }


  @Test
  public void testSpliterator() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3, true));
    Spliterator<JsonValue> spliterator = ja.spliterator();
    assertEquals(4, spliterator.estimateSize());
    assertEquals(4, spliterator.getExactSizeIfKnown());
    assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
    assertTrue(spliterator.tryAdvance(a -> {
    }));
    assertNotNull(spliterator.trySplit());
    assertNotNull(spliterator.trySplit());
    assertNull(spliterator.trySplit());
  }


  @Test
  public void testToString() {
    JArray ja = new JArray(Arrays.asList(1, 2, 3));
    assertEquals("[1,2,3]", ja.toString());
    ja.clear();
    assertEquals("[]", ja.toString());
  }


  @Test
  public void toArray() {
    JArray ja = new JArray(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    assertEquals(10, ja.toArray().length);
    assertEquals(10, ja.toArray(new JsonValue[5]).length);
    assertEquals(10, ja.toArray(JsonValue[]::new).length);
  }
}
