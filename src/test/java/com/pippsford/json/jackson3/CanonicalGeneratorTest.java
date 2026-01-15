package com.pippsford.json.jackson3;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.List;

import tools.jackson.core.Base64Variants;
import tools.jackson.core.JacksonException;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.Version;
import tools.jackson.core.io.SerializedString;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.IntNode;
import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Greatrix on 06/01/2020.
 */
public class CanonicalGeneratorTest {

  CanonicalGenerator instance;

  StringWriter writer = new StringWriter();


  @Test
  public void badEndArray() throws IOException {
    instance.writeStartObject();
    JacksonException e = assertThrows(JacksonException.class, () -> instance.writeEndArray());
    assertEquals("Current context not Array but Object\n"
        + " at [No location information]", e.getMessage());
  }


  @Test
  public void badEndObject() throws IOException {
    instance.writeStartArray();
    JacksonException e = assertThrows(JacksonException.class, () -> instance.writeEndObject());
    assertEquals("Current context not Object but Array\n"
        + " at [No location information]", e.getMessage());
  }


  @Test
  public void close() throws IOException {
    instance.writeStartArray();
    instance.writeStartObject();
    instance.close();
    assertEquals("[{}]", writer.toString());
    assertTrue(instance.isClosed());
  }


  @Test
  public void flush() throws IOException {
    instance.writeStartArray();
    instance.writeStartObject();
    instance.flush();

    // Output is empty despite the flush
    assertEquals("", writer.toString());
  }


  @Test
  public void isClosed() {
    assertFalse(instance.isClosed());
  }


  @BeforeEach
  public void setUp() throws IOException {
    instance = (CanonicalGenerator) new CanonicalFactory(new JsonFactory()).createGenerator(writer);
  }


  @Test
  public void testWritingJson() throws IOException {
    CJArray array = new CJArray();
    array.add("A");
    array.add(1);
    CJObject object = new CJObject();
    object.put("A", 1);
    object.put("B", 2);
    array.add(object);

    instance.writeRawCanonicalValue(array);

    assertEquals("[\"A\",1,{\"A\":1,\"B\":2}]", writer.toString());
  }


  @Test
  public void version() {
    Version pv = instance.version();
    assertNotNull(pv);
  }


  @Test
  public void writeBinary() throws IOException {
    byte[] data = new byte[80];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) i;
    }
    instance.writeBinary(Base64Variants.MIME, data, 0, data.length);
    instance.close();

    String text = writer.toString();
    text = text.substring(1, text.length() - 1);
    text = text.replaceAll("\\\\n", "\n");
    text = text.replaceAll("\\\\r", "\r");

    byte[] output = Base64.getMimeDecoder().decode(text);
    assertArrayEquals(data, output);
  }


  @Test
  public void writeBinary1() throws IOException {
    instance.writeBinary(Base64Variants.MIME, new byte[0], 0, 0);
    instance.close();

    String text = writer.toString();
    assertEquals("\"\"", text);
  }


  @Test
  public void writeBinaryShort() throws IOException {
    instance.writeStartArray();
    for (int i = 0; i < 5; i++) {
      instance.writeBinary(Base64Variants.MIME, new byte[i], 0, i);
    }
    instance.close();

    String text = writer.toString();
    assertEquals("[\"\",\"AA==\",\"AAA=\",\"AAAA\",\"AAAAAA==\"]", text);
  }


  @Test
  public void writeBoolean() throws IOException {
    instance.writeStartArray();
    instance.writeBoolean(true);
    instance.writeBoolean(false);
    instance.close();
    assertEquals("[true,false]", writer.toString());
  }


  @Test
  public void writeField_Bad() throws IOException {
    instance.writeStartObject();
    instance.writeName("chalk");
    JacksonException e = assertThrows(JacksonException.class, () -> instance.writeName(new SerializedString("cheese")));
    assertEquals("Can not write a field name, expecting a value\n"
        + " at [No location information]", e.getMessage());
  }


  @Test
  public void writeNull() {
    instance.writeStartObject();
    instance.writeName("null");
    instance.writeNull();
    instance.writeEndObject();
    instance.close();
    assertEquals("{\"null\":null}", writer.toString());
  }


  @Test
  public void writeNull2() {
    instance.writeNull();
    instance.writeNull();
    instance.close();
    assertEquals("null null", writer.toString());
  }


  @Test
  public void writeNull_Bad() throws IOException {
    instance.writeStartObject();
    JacksonException e = assertThrows(JacksonException.class, () -> instance.writeNull());
    assertEquals("Can not write NULL, expecting field name (context: Object)\n"
        + " at [No location information]", e.getMessage());
  }


  @Test
  public void writeNumber() throws IOException {
    instance.writeNumber(1);
    instance.writeNumber(4_000_000_000L);
    instance.writeNumber(BigInteger.valueOf(8_000_000_000_000_000_000L).multiply(BigInteger.valueOf(10)));
    instance.writeNumber(0.0055);
    instance.writeNumber(0.005f);
    instance.writeNumber(BigDecimal.valueOf(0.0001));
    instance.writeNumber("0xcafebabe");
    instance.close();
    assertEquals("1 4000000000 80000000000000000000 5.5E-3 5.0E-3 1.0E-4 \"0xcafebabe\"", writer.toString());
  }


  @Test
  public void writeRawChar() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.writeRaw('x'));
    assertEquals("Canonical JSON does not support raw content", e.getMessage());
  }


  @Test
  public void writeRawCharArray() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.writeRaw(new char[10], 3, 3));
    assertEquals("Canonical JSON does not support raw content", e.getMessage());
  }


  @Test
  public void writeRawString1() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.writeRaw("xxxxxxxxxxxxx", 3, 3));
    assertEquals("Canonical JSON does not support raw content", e.getMessage());
  }


  @Test
  public void writeRawString2() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.writeRaw("xxxx"));
    assertEquals("Canonical JSON does not support raw content", e.getMessage());
  }


  @Test
  public void writeRawUTF8String() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.writeRawUTF8String(new byte[10], 0, 8));
    assertEquals("Canonical JSON does not support raw content", e.getMessage());
  }


  @Test
  public void writeRawValue() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.writeRawValue("yyyy"));
    assertEquals("Canonical JSON does not support raw content", e.getMessage());
  }


  @Test
  public void writeRawValue2() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.writeRawValue("xxxxxxxxxxxxx", 3, 3));
    assertEquals("Canonical JSON does not support raw content", e.getMessage());
  }


  @Test
  public void writeRawValueCharArray() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.writeRawValue(new char[10], 3, 3));
    assertEquals("Canonical JSON does not support raw content", e.getMessage());
  }


  @Test
  public void writeStartArray() throws IOException {
    instance.writeStartArray();
    instance.writeStartArray();
    instance.writeStartObject();
    instance.writeName("a");
    instance.writeStartObject();
    instance.writeName("b");
    instance.writeStartArray();
    instance.writeNumber(1);
    instance.close();
    assertEquals("[[{\"a\":{\"b\":[1]}}]]", writer.toString());
  }


  @Test
  public void writeString() throws IOException {
    instance.writeString("Hello, World!");
    assertEquals("\"Hello, World!\"", writer.toString());
  }


  @Test
  public void writeString2() throws IOException {
    char[] array = "Hello, World!".toCharArray();
    instance.writeString(array, 0, 5);
    assertEquals("\"Hello\"", writer.toString());
  }


  @Test
  public void writeString3() throws IOException {
    instance.writeString(new SerializedString("Hello, World!"));
    assertEquals("\"Hello, World!\"", writer.toString());
  }


  @Test
  public void writeUTF8String() throws IOException {
    byte[] data = "Hello, World!".getBytes(UTF_8);
    instance.writeUTF8String(data, 0, data.length);
    assertEquals("\"Hello, World!\"", writer.toString());
  }

}
