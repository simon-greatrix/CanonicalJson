package com.pippsford.json.jackson3;

import java.io.IOException;

import tools.jackson.core.JsonParser;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import com.pippsford.json.exception.JsonIOException;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;
import tools.jackson.databind.ValueDeserializer;

/**
 * A deserializer for JSON objects.
 *
 * @author Simon Greatrix on 12/02/2020.
 */
public class JsonObjectDeserializer extends ValueDeserializer<JsonObject> {

  /** New instance. */
  public JsonObjectDeserializer() {
    // nothing to do
  }


  @Override
  public JsonObject deserialize(JsonParser p, DeserializationContext context) {
    try {
      JacksonReader parser = new JacksonReader(p);
      return parser.readObject();
    } catch (JsonIOException jsonIOException) {
      throw JacksonIOException.construct(jsonIOException.cause());
    } catch (JsonParsingException jsonParsingException) {
      jakarta.json.stream.JsonLocation l = jsonParsingException.getLocation();
      TokenStreamLocation location = new TokenStreamLocation(null, l.getStreamOffset(), (int) l.getLineNumber(), (int) l.getColumnNumber());
      throw new StreamReadException(p, jsonParsingException.getMessage(), location, jsonParsingException);
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
