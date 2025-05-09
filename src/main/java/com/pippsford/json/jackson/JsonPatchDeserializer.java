package com.pippsford.json.jackson;

import java.io.IOException;
import java.util.Collections;
import jakarta.json.JsonArray;
import jakarta.json.JsonPatch;
import jakarta.json.stream.JsonParsingException;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import com.pippsford.json.exception.JsonIOException;
import com.pippsford.json.patch.Patch;

/**
 * A deserializer for JsonPatch instances.
 *
 * @author Simon Greatrix on 18/02/2020.
 */
public class JsonPatchDeserializer extends JsonDeserializer<JsonPatch> {

  /** New instance. */
  public JsonPatchDeserializer() {
    // nothing to do
  }


  @Override
  public JsonPatch deserialize(JsonParser p, DeserializationContext context) throws IOException {
    try {
      JacksonReader parser = new JacksonReader(p);
      JsonArray jsonArray = parser.readArray();
      return new Patch(jsonArray);
    } catch (JsonIOException jsonIOException) {
      throw jsonIOException.cause();
    } catch (JsonParsingException jsonParsingException) {
      jakarta.json.stream.JsonLocation l = jsonParsingException.getLocation();
      JsonLocation location = new JsonLocation(null, l.getStreamOffset(), (int) l.getLineNumber(), (int) l.getColumnNumber());
      throw new JsonParseException(p, jsonParsingException.getMessage(), location, jsonParsingException);
    }
  }


  @Override
  public JsonPatch getEmptyValue(DeserializationContext context) {
    return new Patch(Collections.emptyList());
  }


  @Override
  public Class<?> handledType() {
    return JsonPatch.class;
  }

}
