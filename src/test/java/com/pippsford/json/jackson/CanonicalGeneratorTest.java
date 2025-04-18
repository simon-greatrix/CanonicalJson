package com.pippsford.json.jackson;

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

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;

/**
 * @author Simon Greatrix on 06/01/2020.
 */
public class CanonicalGeneratorTest {

  CanonicalGenerator instance;

  StringWriter writer = new StringWriter();


  @Test
  public void badEndArray() throws IOException {
    instance.writeStartObject();
    JsonGenerationException e = assertThrows(JsonGenerationException.class, () -> instance.writeEndArray());
    assertEquals("Current context not Array but Object", e.getMessage());
  }


  @Test
  public void badEndObject() throws IOException {
    instance.writeStartArray();
    JsonGenerationException e = assertThrows(JsonGenerationException.class, () -> instance.writeEndObject());
    assertEquals("Current context not Object but Array", e.getMessage());
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
  public void disable() {
    assertTrue(instance.isEnabled(Feature.AUTO_CLOSE_JSON_CONTENT));
    instance.disable(Feature.AUTO_CLOSE_JSON_CONTENT);
    assertFalse(instance.isEnabled(Feature.AUTO_CLOSE_JSON_CONTENT));
    instance.enable(Feature.AUTO_CLOSE_JSON_CONTENT);
    assertTrue(instance.isEnabled(Feature.AUTO_CLOSE_JSON_CONTENT));
  }


  @SuppressWarnings("deprecation")
  @Test
  public void disable_bad() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.disable(Feature.QUOTE_FIELD_NAMES));
    assertEquals("Feature QUOTE_FIELD_NAMES may not be disabled for Canonical JSON", e.getMessage());
  }


  @SuppressWarnings("deprecation")
  @Test
  public void enable_bad() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.enable(Feature.ESCAPE_NON_ASCII));
    assertEquals("Feature ESCAPE_NON_ASCII may not be enabled for Canonical JSON", e.getMessage());
  }


  @Test
  public void flush() throws IOException {
    instance.enable(Feature.FLUSH_PASSED_TO_STREAM);
    instance.writeStartArray();
    instance.writeStartObject();
    instance.flush();

    // Output is empty despite the flush
    assertEquals("", writer.toString());
  }


  @Test
  public void getCodec() {
    assertNotNull(instance.getCodec());
  }


  @SuppressWarnings("deprecation")
  @Test
  public void getFeatureMask() {
    int m = instance.getFeatureMask();
    assertNotEquals(0, m & Feature.QUOTE_FIELD_NAMES.getMask());
    assertNotEquals(0, m & Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask());
  }


  @Test
  public void getOutputContext() {
    assertNotNull(instance.getOutputContext());
  }


  @Test
  public void isClosed() {
    assertFalse(instance.isClosed());
  }


  @SuppressWarnings("deprecation")
  @Test
  public void isEnabled() {
    assertTrue(instance.isEnabled(Feature.QUOTE_FIELD_NAMES));
  }


  @Test
  public void setCodec() {
    assertSame(instance, instance.setCodec(null));
  }


  @Deprecated
  @Test
  public void setFeatureMask1() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.setFeatureMask(Feature.QUOTE_FIELD_NAMES.getMask()));
    assertEquals("Does not contain required features: 8", e.getMessage());
  }


  @Deprecated
  @Test
  public void setFeatureMask2() {
    UnsupportedOperationException e = assertThrows(
        UnsupportedOperationException.class,
        () -> instance.setFeatureMask(Feature.QUOTE_FIELD_NAMES.getMask() + Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask() + Feature.ESCAPE_NON_ASCII.getMask())
    );
    assertEquals("Contains disallowed features: 56", e.getMessage());
  }


  @Deprecated
  @Test
  public void setFeatureMask3() {
    int mask = Feature.QUOTE_FIELD_NAMES.getMask() + Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask();
    instance.setFeatureMask(mask);
    assertEquals(mask, instance.getFeatureMask());
  }


  @BeforeEach
  public void setUp() throws IOException {
    instance = (CanonicalGenerator) new CanonicalFactory(new ObjectMapper()).createGenerator(writer);
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

    instance.writeObject(array);

    assertEquals("[\"A\",1,{\"A\":1,\"B\":2}]", writer.toString());
  }


  @Test
  public void useDefaultPrettyPrinter() {
    UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> instance.useDefaultPrettyPrinter());
    assertEquals("Pretty printing is not allowed for canonical form", e.getMessage());
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
    instance.writeFieldName("chalk");
    JsonGenerationException e = assertThrows(JsonGenerationException.class, () -> instance.writeFieldName(new SerializedString("cheese")));
    assertEquals("Can not write a field name, expecting a value", e.getMessage());
  }


  @Test
  public void writeNull() throws IOException {
    instance.writeStartObject();
    instance.writeFieldName("null");
    instance.writeNull();
    instance.writeEndObject();
    instance.close();
    assertEquals("{\"null\":null}", writer.toString());
  }


  @Test
  public void writeNull2() throws IOException {
    instance.writeNull();
    instance.writeNull();
    instance.close();
    assertEquals("null null", writer.toString());
  }


  @Test
  public void writeNull_Bad() throws IOException {
    instance.writeStartObject();
    JsonGenerationException e = assertThrows(JsonGenerationException.class, () -> instance.writeNull());
    assertEquals("Can not write NULL, expecting field name (context: Object)", e.getMessage());
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
  public void writeObject() throws IOException {
    instance.writeObject(null);
    instance.writeObject("chalk");
    instance.writeObject(List.of(1, 2, 3));
    instance.close();
    assertEquals("null \"chalk\" [1,2,3]", writer.toString());
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
    instance.writeFieldName("a");
    instance.writeStartObject();
    instance.writeFieldName("b");
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
  public void writeTree() throws IOException {
    instance.writeTree(null);
    instance.writeTree(IntNode.valueOf(7));
    instance.close();
    assertEquals("null 7", writer.toString());
  }


  @Test
  public void writeUTF8String() throws IOException {
    byte[] data = "Hello, World!".getBytes(UTF_8);
    instance.writeUTF8String(data, 0, data.length);
    assertEquals("\"Hello, World!\"", writer.toString());
  }

}
