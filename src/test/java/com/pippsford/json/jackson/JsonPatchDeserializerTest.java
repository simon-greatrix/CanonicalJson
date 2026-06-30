package com.pippsford.json.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.pippsford.json.CJObject;

public class JsonPatchDeserializerTest {

  @Test
  public void handledType() {
    assertEquals(JsonPatch.class, new JsonPatchDeserializer().handledType());
  }


  @Test
  public void getEmptyValue() {
    JsonPatch patch = new JsonPatchDeserializer().getEmptyValue(null);
    assertNotNull(patch);
    JsonObject result = patch.apply(new CJObject());
    assertEquals("{}", result.toString());
  }


  @Test
  public void deserialize() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());
    JsonPatch patch = mapper.readValue("[{\"op\":\"add\",\"path\":\"/x\",\"value\":1}]", JsonPatch.class);
    JsonObject result = patch.apply(new CJObject());
    assertEquals("{\"x\":1}", result.toString());
  }


  @Test
  public void deserializeIoException() throws IOException {
    JsonParser mock = Mockito.mock(JsonParser.class);
    Mockito.doThrow(new IOException("io error")).when(mock).nextToken();
    assertThrows(IOException.class, () -> new JsonPatchDeserializer().deserialize(mock, null));
  }


  @Test
  public void deserializeJsonParsingException() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());
    assertThrows(JsonParseException.class, () -> mapper.readValue("true", JsonPatch.class));
  }

}
