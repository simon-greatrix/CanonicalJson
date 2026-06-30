package com.pippsford.json.jackson3;

import java.util.Collections;

import com.pippsford.json.exception.JsonIOException;
import com.pippsford.json.patch.Patch;
import jakarta.json.JsonArray;
import jakarta.json.JsonPatch;
import jakarta.json.stream.JsonParsingException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * A deserializer for JsonPatch instances.
 *
 * @author Simon Greatrix on 18/02/2020.
 */
public class JsonPatchDeserializer extends ValueDeserializer<JsonPatch> {

  /** New instance. */
  public JsonPatchDeserializer() {
    // nothing to do
  }


  @Override
  public JsonPatch deserialize(JsonParser p, DeserializationContext context) {
    try {
      JacksonReader parser = new JacksonReader(p);
      JsonArray jsonArray = parser.readArray();
      return new Patch(jsonArray);
    } catch (JsonIOException jsonIOException) {
      throw JacksonIOException.construct(jsonIOException.cause());
    } catch (JsonParsingException jsonParsingException) {
      jakarta.json.stream.JsonLocation l = jsonParsingException.getLocation();
      TokenStreamLocation location = new TokenStreamLocation(null, l.getStreamOffset(), (int) l.getLineNumber(), (int) l.getColumnNumber());
      throw new StreamReadException(p, jsonParsingException.getMessage(), location, jsonParsingException);
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
