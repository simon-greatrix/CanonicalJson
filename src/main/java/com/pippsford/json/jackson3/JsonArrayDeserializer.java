package com.pippsford.json.jackson3;

import com.pippsford.json.exception.JsonIOException;
import jakarta.json.JsonArray;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Deserializer for JSON arrays.
 *
 * @author Simon Greatrix on 12/02/2020.
 */
public class JsonArrayDeserializer extends ValueDeserializer<JsonArray> {

  /** New instance. */
  public JsonArrayDeserializer() {
    // nothing to do
  }


  @Override
  public JsonArray deserialize(JsonParser p, DeserializationContext context) {
    try {
      JacksonReader parser = new JacksonReader(p);
      return parser.readArray();
    } catch (JsonIOException jsonIOException) {
      throw JacksonIOException.construct(jsonIOException.cause());
    } catch (JsonParsingException jsonParsingException) {
      jakarta.json.stream.JsonLocation l = jsonParsingException.getLocation();
      TokenStreamLocation location = new TokenStreamLocation(null, l.getStreamOffset(), (int) l.getLineNumber(), (int) l.getColumnNumber());
      throw new StreamReadException(p, jsonParsingException.getMessage(), location, jsonParsingException);
    }
  }


  @Override
  public JsonArray getEmptyValue(DeserializationContext context) {
    return JsonValue.EMPTY_JSON_ARRAY;
  }


  @Override
  public Class<?> handledType() {
    return JsonArray.class;
  }

}
