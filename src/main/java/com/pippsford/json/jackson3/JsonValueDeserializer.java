package com.pippsford.json.jackson3;

import com.pippsford.json.exception.JsonIOException;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * A deserializer for JSON values.
 *
 * @author Simon Greatrix on 12/02/2020.
 */
public class JsonValueDeserializer extends ValueDeserializer<JsonValue> {

  /** New instance. */
  public JsonValueDeserializer() {
    // nothing to do
  }


  @Override
  public JsonValue deserialize(JsonParser p, DeserializationContext context) {
    try {
      JacksonReader parser = new JacksonReader(p);
      return parser.readValue();
    } catch (JsonIOException jsonIOException) {
      throw JacksonIOException.construct(jsonIOException.cause());
    } catch (JsonParsingException jsonParsingException) {
      jakarta.json.stream.JsonLocation l = jsonParsingException.getLocation();
      TokenStreamLocation location = new TokenStreamLocation(null, l.getStreamOffset(), (int) l.getLineNumber(), (int) l.getColumnNumber());
      throw new StreamReadException(p, jsonParsingException.getMessage(), location, jsonParsingException);
    }
  }


  @Override
  public JsonValue getEmptyValue(DeserializationContext context) {
    return JsonValue.NULL;
  }


  @Override
  public Class<?> handledType() {
    return JsonValue.class;
  }

}
