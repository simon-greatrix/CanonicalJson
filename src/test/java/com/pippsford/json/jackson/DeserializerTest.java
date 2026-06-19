package com.pippsford.json.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DeserializerTest {

  private static JsonParser parserFor(String json) throws IOException {
    JsonParser p = new JsonFactory().createParser(json);
    p.nextToken();
    return p;
  }


  // --- JsonArrayDeserializer ---

  @Test
  public void jsonArrayHandledType() {
    assertEquals(JsonArray.class, new JsonArrayDeserializer().handledType());
  }


  @Test
  public void jsonArrayEmptyValue() {
    assertEquals(JsonValue.EMPTY_JSON_ARRAY, new JsonArrayDeserializer().getEmptyValue(null));
  }


  @Test
  public void jsonArrayDeserialize() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());
    JsonArray array = mapper.readValue("[1,2,3]", JsonArray.class);
    assertEquals("[1,2,3]", array.toString());
  }


  @Test
  public void jsonArrayDeserializeIoException() throws IOException {
    JsonParser mock = Mockito.mock(JsonParser.class);
    Mockito.doThrow(new IOException("io")).when(mock).nextToken();
    assertThrows(IOException.class, () -> new JsonArrayDeserializer().deserialize(mock, null));
  }


  @Test
  public void jsonArrayDeserializeJsonParsingException() throws IOException {
    JsonParser parser = parserFor("true");
    assertThrows(JsonParseException.class, () -> new JsonArrayDeserializer().deserialize(parser, null));
  }


  // --- JsonObjectDeserializer ---

  @Test
  public void jsonObjectHandledType() {
    assertEquals(JsonObject.class, new JsonObjectDeserializer().handledType());
  }


  @Test
  public void jsonObjectEmptyValue() {
    assertEquals(JsonValue.EMPTY_JSON_OBJECT, new JsonObjectDeserializer().getEmptyValue(null));
  }


  @Test
  public void jsonObjectDeserialize() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());
    JsonObject obj = mapper.readValue("{\"a\":1}", JsonObject.class);
    assertEquals("{\"a\":1}", obj.toString());
  }


  @Test
  public void jsonObjectDeserializeIoException() throws IOException {
    JsonParser mock = Mockito.mock(JsonParser.class);
    Mockito.doThrow(new IOException("io")).when(mock).nextToken();
    assertThrows(IOException.class, () -> new JsonObjectDeserializer().deserialize(mock, null));
  }


  @Test
  public void jsonObjectDeserializeJsonParsingException() throws IOException {
    JsonParser parser = parserFor("true");
    assertThrows(JsonParseException.class, () -> new JsonObjectDeserializer().deserialize(parser, null));
  }


  // --- JsonValueDeserializer ---

  @Test
  public void jsonValueHandledType() {
    assertEquals(JsonValue.class, new JsonValueDeserializer().handledType());
  }


  @Test
  public void jsonValueEmptyValue() {
    assertEquals(JsonValue.NULL, new JsonValueDeserializer().getEmptyValue(null));
  }


  @Test
  public void jsonValueDeserialize() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());
    JsonValue value = mapper.readValue("\"hello\"", JsonValue.class);
    assertEquals("\"hello\"", value.toString());
  }


  @Test
  public void jsonValueDeserializeIoException() throws IOException {
    JsonParser mock = Mockito.mock(JsonParser.class);
    Mockito.doThrow(new IOException("io")).when(mock).nextToken();
    assertThrows(IOException.class, () -> new JsonValueDeserializer().deserialize(mock, null));
  }


  @Test
  public void jsonValueDeserializeJsonParsingException() throws IOException {
    // Empty input yields a null token, triggering "Value not found" JsonParsingException
    JsonParser parser = new JsonFactory().createParser(new byte[0]);
    assertThrows(JsonParseException.class, () -> new JsonValueDeserializer().deserialize(parser, null));
  }


  // --- JsonStructureDeserializer ---

  @Test
  public void jsonStructureHandledType() {
    assertEquals(JsonStructure.class, new JsonStructureDeserializer().handledType());
  }


  @Test
  public void jsonStructureDeserializeArray() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());
    JsonStructure s = mapper.readValue("[1,2]", JsonStructure.class);
    assertEquals("[1,2]", s.toString());
  }


  @Test
  public void jsonStructureDeserializeObject() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());
    JsonStructure s = mapper.readValue("{\"x\":true}", JsonStructure.class);
    assertEquals("{\"x\":true}", s.toString());
  }


  @Test
  public void jsonStructureDeserializeIoException() throws IOException {
    JsonParser mock = Mockito.mock(JsonParser.class);
    Mockito.doThrow(new IOException("io")).when(mock).nextToken();
    assertThrows(IOException.class, () -> new JsonStructureDeserializer().deserialize(mock, null));
  }


  @Test
  public void jsonStructureDeserializeJsonParsingException() throws IOException {
    JsonParser parser = parserFor("true");
    assertThrows(JsonParseException.class, () -> new JsonStructureDeserializer().deserialize(parser, null));
  }

}
