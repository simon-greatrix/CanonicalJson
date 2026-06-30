package com.pippsford.json.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.EnumSet;

import jakarta.json.JsonStructure;
import jakarta.json.JsonValue.ValueType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import com.pippsford.json.Canonical;

public class IncorrectTypeExceptionTest {

  CJArray array;

  CJObject object;


  @BeforeEach
  public void setUp() {
    object = new CJObject();
    object.put("array", new CJArray());
    object.put("boolean", true);
    object.put("null");
    object.put("string", "text");
    object.put("number", 123);
    object.put("object", new CJObject());

    array = new CJArray();
    array.addAll(object.values());
  }


  @Test
  public void testArray() {
    IncorrectTypeException e = assertThrows(IncorrectTypeException.class, () -> array.getString(0));

    assertEquals(0, e.getIndex());
    assertNull(e.getKey());
    assertEquals(ValueType.ARRAY, e.getActual());
    assertEquals(EnumSet.of(ValueType.STRING), e.getRequired());
  }


  @Test
  public void testObject() {
    IncorrectTypeException e = assertThrows(IncorrectTypeException.class, () -> object.getString("array"));

    assertEquals(-1, e.getIndex());
    assertEquals("array", e.getKey());
    assertEquals(ValueType.ARRAY, e.getActual());
    assertEquals(EnumSet.of(ValueType.STRING), e.getRequired());
  }


  @Test
  public void testEmpty() {
    assertThrows(IncorrectTypeException.class, () -> Canonical.createEmpty(null));
    assertThrows(IncorrectTypeException.class, () -> Canonical.createEmpty(
        new JsonStructure() {
          @Override
          public ValueType getValueType() {
            return ValueType.TRUE;
          }
        }
    ));
  }
}
