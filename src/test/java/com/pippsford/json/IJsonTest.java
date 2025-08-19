package com.pippsford.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.pippsford.json.builder.ArrayBuilder;
import com.pippsford.json.builder.ObjectBuilder;
import com.pippsford.json.ijson.ForbiddenIJsonException;
import com.pippsford.json.exception.JsonIOException;
import com.pippsford.json.ijson.IJsonNumberSerializer;
import com.pippsford.json.primitive.CJString;
import com.pippsford.json.primitive.numbers.CJNumber;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;

class IJsonTest {

  @Test
  void arrays() {
    CJArray array = new ArrayBuilder()
        .add(56)
        .add(new ObjectBuilder()
            .add("d", true)
            .addNull("10")
            .add("1", JsonValue.EMPTY_JSON_ARRAY))
        .build();
    String json = IJson.serializeToText(array);
    assertEquals("[56,{\"1\":[],\"10\":null,\"d\":true}]", json);
  }


  @Test
  void french() {
    CJObject object = new ObjectBuilder()
        .add("peach", "This sorting order")
        .add("péché", "is wrong according to French")
        .add("pêche", "but canonicalization MUST")
        .add("sin", "ignore locale")
        .build();
    String json = IJson.serializeToText(object);
    assertEquals(
        "{\"peach\":\"This sorting order\",\"péché\":\"is wrong according to French\",\"pêche\":\"but canonicalization MUST\",\"sin\":\"ignore locale\"}",
        json
    );
  }


  @Test
  void serializeToBytes() {
    assertEquals("null", new String(IJson.serialize(null), StandardCharsets.UTF_8));
  }


  @Test
  void structures() {
    CJObject object = new ObjectBuilder()
        .add(
            "1", new ObjectBuilder()
                .add(
                    "f", new ObjectBuilder()
                        .add("f", "hi")
                        .add("F", 5)
                )
                .add("\n", 56.0)
        )
        .add("10", JsonValue.EMPTY_JSON_OBJECT)
        .add("", "empty")
        .add("a", JsonValue.EMPTY_JSON_OBJECT)
        .add(
            "111", new ArrayBuilder()
                .add(new ObjectBuilder()
                    .add("e", "yes")
                    .add("E", "no"))
        )
        .add(
            "A", new ObjectBuilder()
                .add("b", "123")
        )
        .build();
    String json = IJson.serializeToText(object);
    assertEquals(
        "{\"\":\"empty\",\"1\":{\"\\n\":56,\"f\":{\"F\":5,\"f\":\"hi\"}},\"10\":{},\"111\":[{\"E\":\"no\",\"e\":\"yes\"}],\"A\":{\"b\":\"123\"},\"a\":{}}",
        json
    );
  }


  @Test
  void test1() {
    CJObject object = new ObjectBuilder()
        .add(
            "numbers", new ArrayBuilder()
                .add(new BigDecimal("333333333.3333333"))
                .add(new BigDecimal("1e+30"))
                .add(new BigDecimal("4.50"))
                .add(new BigDecimal("2e-3"))
                .add(new BigDecimal("0.000000000000000000000000001"))
        )
        .add("string", "€$\u000f\nA'B\"\\\\\"/")
        .add(
            "literals", new ArrayBuilder()
                .addNull()
                .add(true)
                .add(false)
        )
        .build();

    String json = IJson.serializeToText(object);
    assertEquals(
        "{\"literals\":[null,true,false],\"numbers\":[333333333.3333333,"
            + "1e+30,4.5,0.002,1e-27],\"string\":\"€$\\u000f\\nA'B\\\"\\\\\\\\\\\"/\"}", json
    );
  }


  @Test
  void test2() {
    Map<String, String> map = Map.of(
        "\u20ac", "Euro Sign",
        "\r", "Carriage Return",
        "\ufb33", "Hebrew Letter Dalet With Dagesh",
        "1", "One",
        "\ud83d\ude00", "Emoji: Grinning Face",
        "\u0080", "Control",
        "\u00f6", "Latin Small Letter O With Diaeresis"
    );
    CJObject object = new CJObject();
    for (var e : map.entrySet()) {
      object.put(e.getKey(), e.getValue());
    }
    String json = IJson.serializeToText(object);
    assertEquals(
        "{\"\\r\":\"Carriage Return\","
            + "\"1\":\"One\","
            + "\"\u0080\":\"Control\","
            + "\"ö\":\"Latin Small Letter O With Diaeresis\","
            + "\"€\":\"Euro Sign\","
            + "\"\uD83D\uDE00\":\"Emoji: Grinning Face\","

            // Hebrew is written right-to-left so this string is backwards
            + "\"דּ\":\"Hebrew Letter Dalet With Dagesh\"}", json
    );
  }


  @Test
  void testIsCompatible() {
    assertTrue(IJson.isCompatible(null));
    assertTrue(IJson.isCompatible(new ObjectBuilder()
        .add("\u20ac", "Euro Sign")
        .add("\ud83d\ude00", "Emoji: Grinning Face")
        .add("x", 1)
        .addNull("n")
        .add(
            "a", new ArrayBuilder()
                .add(true)
                .add(false)
                .build()
        )
        .build()
    ));

    // Lone leading surrogate
    assertFalse(IJson.isCompatible(CJString.create("\ud83dx")));
    assertFalse(IJson.isCompatible(CJString.create("\ud83d")));
    assertFalse(IJson.isCompatible(CJString.create("x\ude00")));

    assertFalse(IJson.isCompatible(new ObjectBuilder().add("\ud83d", "x").build()));
    assertFalse(IJson.isCompatible(new ObjectBuilder().add("x,", "\ud83d").build()));
    assertFalse(IJson.isCompatible(new ArrayBuilder().add("\ud83d").build()));

    // Numbers
    assertTrue(IJson.isCompatible(CJNumber.cast(Integer.MIN_VALUE)));
    assertTrue(IJson.isCompatible(CJNumber.cast(Integer.MAX_VALUE)));
    assertTrue(IJson.isCompatible(CJNumber.cast(Integer.MIN_VALUE * 3L)));
    assertTrue(IJson.isCompatible(CJNumber.cast(Integer.MAX_VALUE * 3L)));

    assertFalse(IJson.isCompatible(CJNumber.cast(Long.MAX_VALUE - 2)));
    assertFalse(IJson.isCompatible(CJNumber.cast(Long.MIN_VALUE + 2)));

    BigDecimal mult = new BigDecimal("10.1");
    BigDecimal min = new BigDecimal(IJsonNumberSerializer.serialize(Double.MIN_VALUE));
    assertTrue(IJson.isCompatible(CJNumber.cast(min)));
    assertTrue(IJson.isCompatible(CJNumber.cast(min.scaleByPowerOfTen(1))));

    // Too precise
    assertFalse(IJson.isCompatible(CJNumber.cast(min.multiply(mult))));

    // Too small
    assertFalse(IJson.isCompatible(CJNumber.cast(min.scaleByPowerOfTen(-1))));

    BigDecimal max = new BigDecimal(IJsonNumberSerializer.serialize(Double.MAX_VALUE));
    assertTrue(IJson.isCompatible(CJNumber.cast(max)));
    assertFalse(IJson.isCompatible(CJNumber.cast(max.multiply(mult))));
  }


  @Test
  void testNumbers() {
    CJArray array = new ArrayBuilder()
        .add(0)
        .add(-1)
        .add(1)
        .add(-7)
        .add(10000)
        .add(0.7)
        .add(1000000.0)
        .add(new BigInteger("1234567890123456789012345678901234567890").doubleValue())
        .add(new BigDecimal("7.7").pow(100).doubleValue())
        .build();
    String json = IJson.serializeToText(array);
    assertEquals("[0,-1,1,-7,10000,0.7,1000000,1.2345678901234568e+39,4.45730669014002e+88]", json);

    assertThrows(ForbiddenIJsonException.class, () -> {
      IJson.serializeToText(CJNumber.cast(new BigInteger("1234567890123456789012345678901234567890")));
    });

    assertThrows(ForbiddenIJsonException.class, () -> {
      IJson.serializeToText(CJNumber.cast(new BigDecimal("7.7").pow(100)));
    });
  }


  @Test
  void throwsForbiddenIJsonException() throws IOException {
    assertThrows(ForbiddenIJsonException.class, () -> IJson.serialize(CJString.create("\ud8ff")));
    assertThrows(ForbiddenIJsonException.class, () -> IJson.serialize(CJString.create("\ufddd")));
    assertThrows(ForbiddenIJsonException.class, () -> IJson.serialize(CJString.create("\ufffe")));
    assertThrows(ForbiddenIJsonException.class, () -> IJson.serialize(CJString.create("\uffff")));
    assertThrows(ForbiddenIJsonException.class, () -> IJson.serialize(CJString.create(Character.toString(0xbfffe))));

    IJson.serialize(CJString.create("\ufffc"));
  }


  @Test
  void throwsJsonIOException() throws IOException {
    OutputStream stream = mock(OutputStream.class);
    doThrow(new IOException("TEST")).when(stream).write(anyInt());
    doThrow(new IOException("TEST")).when(stream).write(any(byte[].class));

    assertThrows(JsonIOException.class, () -> IJson.serialize(stream, null));
  }


  @Test
  void weird() {
    String input = "{\n"
        + "  \"€\": \"Euro Sign\",\n"
        + "  \"1\": \"One\",\n"
        + "  \"\\u0080\": \"Control\",\n"
        + "  \"\\ud83d\\ude02\": \"Smiley\",\n"
        + "  \"ö\": \"Latin Small Letter O With Diaeresis\",\n"
        + "  \"דּ\": \"Hebrew Letter Dalet With Dagesh\",\n"
        + "  \"</script>\": \"Browser Challenge\"\n"
        + "}";
    CJObject object = Canonical.toJsonObject(input);
    String json = IJson.serializeToText(object);
    assertEquals(
        "{\"1\":\"One\","
            + "\"</script>\":\"Browser Challenge\","
            + "\"\u0080\":\"Control\","
            + "\"ö\":\"Latin Small Letter O With Diaeresis\","
            + "\"€\":\"Euro Sign\","
            + "\"\uD83D\uDE02\":\"Smiley\","
            + "\"דּ\":\"Hebrew Letter Dalet With Dagesh\"}", json
    );
  }

}
