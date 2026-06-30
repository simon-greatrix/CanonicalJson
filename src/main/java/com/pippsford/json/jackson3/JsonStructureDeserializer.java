package com.pippsford.json.jackson3;

import com.pippsford.json.exception.JsonIOException;
import jakarta.json.JsonStructure;
import jakarta.json.stream.JsonParsingException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * A deserializer for JSON structures.
 *
 * @author Simon Greatrix on 12/02/2020.
 */
public class JsonStructureDeserializer extends ValueDeserializer<JsonStructure> {

  /** New instance. */
  public JsonStructureDeserializer() {
    // nothing to do
  }


  @Override
  public JsonStructure deserialize(JsonParser p, DeserializationContext context) {
    try {
      JacksonReader parser = new JacksonReader(p);
      return parser.read();
    } catch (JsonIOException jsonIOException) {
      throw JacksonIOException.construct(jsonIOException.cause());
    } catch (JsonParsingException jsonParsingException) {
      jakarta.json.stream.JsonLocation l = jsonParsingException.getLocation();
      TokenStreamLocation location = new TokenStreamLocation(null, l.getStreamOffset(), (int) l.getLineNumber(), (int) l.getColumnNumber());
      throw new StreamReadException(p, jsonParsingException.getMessage(), location, jsonParsingException);
    }
  }


  @Override
  public Class<?> handledType() {
    return JsonStructure.class;
  }

}
