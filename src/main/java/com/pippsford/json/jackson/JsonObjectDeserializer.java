package com.pippsford.json.jackson;

import java.io.IOException;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import com.pippsford.json.exception.JsonIOException;

/**
 * A deserializer for JSON objects.
 *
 * @author Simon Greatrix on 12/02/2020.
 */
public class JsonObjectDeserializer extends JsonDeserializer<JsonObject> {

  /** New instance. */
  public JsonObjectDeserializer() {
    // nothing to do
  }


  @Override
  public JsonObject deserialize(JsonParser p, DeserializationContext context) throws IOException {
    try {
      JacksonReader parser = new JacksonReader(p);
      return parser.readObject();
    } catch (JsonIOException jsonIOException) {
      throw jsonIOException.cause();
    } catch (JsonParsingException jsonParsingException) {
      jakarta.json.stream.JsonLocation l = jsonParsingException.getLocation();
      JsonLocation location = new JsonLocation(null, l.getStreamOffset(), (int) l.getLineNumber(), (int) l.getColumnNumber());
      throw new JsonParseException(p, jsonParsingException.getMessage(), location, jsonParsingException);
    }
  }


  @Override
  public JsonObject getEmptyValue(DeserializationContext context) {
    return JsonValue.EMPTY_JSON_OBJECT;
  }


  @Override
  public Class<?> handledType() {
    return JsonObject.class;
  }

}
