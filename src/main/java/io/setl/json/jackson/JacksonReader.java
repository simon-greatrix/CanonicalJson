package io.setl.json.jackson;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;

import io.setl.json.CJArray;
import io.setl.json.CJObject;
import io.setl.json.exception.JsonIOException;
import io.setl.json.io.Location;
import io.setl.json.primitive.CJFalse;
import io.setl.json.primitive.CJNull;
import io.setl.json.primitive.CJString;
import io.setl.json.primitive.CJTrue;
import io.setl.json.primitive.numbers.CJNumber;

/**
 * Read JSON using Jackson's parser.
 *
 * @author Simon Greatrix on 31/01/2020.
 */
public class JacksonReader implements JsonReader {

  private static final Map<JsonToken, ReaderLambda> VALUE_READERS;

  /** A lambda function that reads a value from the parser. */
  @FunctionalInterface
  private interface ReaderLambda {

    /** Read the value. */
    JsonValue read(JacksonReader reader) throws IOException;

  }

  static {
    EnumMap<JsonToken, ReaderLambda> map = new EnumMap<>(JsonToken.class);
    map.put(JsonToken.START_ARRAY, JacksonReader::doReadArray);
    map.put(JsonToken.START_OBJECT, r -> r.doReadObject(true));
    map.put(JsonToken.FIELD_NAME, r -> r.doReadObject(false));
    map.put(JsonToken.END_OBJECT, r -> JsonValue.EMPTY_JSON_OBJECT);
    map.put(JsonToken.END_ARRAY, r -> JsonValue.EMPTY_JSON_ARRAY);
    map.put(JsonToken.VALUE_FALSE, r -> CJFalse.FALSE);
    map.put(JsonToken.VALUE_TRUE, r -> CJTrue.TRUE);
    map.put(JsonToken.VALUE_NULL, r -> CJNull.NULL);
    map.put(JsonToken.VALUE_STRING, r -> CJString.create(r.jsonParser.getText()));
    map.put(JsonToken.VALUE_EMBEDDED_OBJECT, r -> CJString.create(r.jsonParser.getText()));
    map.put(JsonToken.VALUE_NUMBER_INT, JacksonReader::parseNumber);
    map.put(JsonToken.VALUE_NUMBER_FLOAT, r -> CJNumber.cast(r.jsonParser.getDecimalValue()));
    VALUE_READERS = Collections.unmodifiableMap(map);
  }

  private final JsonParser jsonParser;

  private boolean isUsed = false;


  /**
   * New instance.
   *
   * @param jsonParser Jackson parser to read from
   */
  public JacksonReader(JsonParser jsonParser) {
    this.jsonParser = jsonParser;
  }


  /**
   * New instance.
   *
   * @param treeNode Jackson Tree Node to read from
   */
  public JacksonReader(TreeNode treeNode) {
    jsonParser = treeNode.traverse();
  }


  private void checkUsed() {
    if (isUsed) {
      throw new IllegalStateException("Parser has already read a value");
    }
    isUsed = true;
  }


  @Override
  public void close() {
    try {
      jsonParser.close();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }


  private JsonArray doReadArray() throws IOException {
    // Read the array. The start array token has already been read.
    CJArray array = new CJArray();
    while (true) {
      JsonToken token = jsonParser.nextToken();
      if (token == JsonToken.END_ARRAY) {
        return array;
      }
      array.add(doReadValue(token));
    }
  }


  private JsonObject doReadObject(boolean getNext) throws IOException {
    // Read the object. The start object token has already been read.
    CJObject object = new CJObject();

    // Due to weirdness in Jackson, sometimes the START_OBJECT and FIELD_NAME have been read, sometimes just START_OBJECT
    JsonToken token = getNext ? jsonParser.nextToken() : jsonParser.currentToken();
    while (true) {
      if (token == JsonToken.END_OBJECT) {
        return object;
      }

      if (token != JsonToken.FIELD_NAME) {
        // hopefully unreachable
        throw new IllegalStateException("Expected a field name inside object, but saw " + token);
      }
      String fieldName = jsonParser.getText();
      object.put(fieldName, doReadValue(jsonParser.nextToken()));

      // Advance to next field
      token = jsonParser.nextToken();
    }
  }


  private JsonValue doReadValue(JsonToken token) throws IOException {
    if (token == null) {
      throw new JsonParsingException("Value not found", getLocation());
    }
    ReaderLambda reader = VALUE_READERS.get(token);
    if (reader != null) {
      return reader.read(this);
    }

    // Reachable if the encoding uses stuff beyond JSON like embedded objects and reference IDs
    throw new IllegalStateException("Unhandled token from Jackson: " + token);
  }


  private Location getLocation() {
    JsonLocation l = jsonParser.getTokenLocation();
    return new Location(l.getColumnNr(), l.getLineNr(), l.getByteOffset());
  }


  private JsonValue parseNumber() throws IOException {
    switch (jsonParser.getNumberType()) {
      case INT:
        return CJNumber.create(jsonParser.getIntValue());
      case LONG:
        return CJNumber.create(jsonParser.getLongValue());
      case BIG_INTEGER:
        return CJNumber.cast(jsonParser.getBigIntegerValue());
      default:
        // hopefully unreachable
        throw new IllegalStateException("Unexpected integer type: " + jsonParser.getNumberType());
    }
  }


  @Override
  public JsonStructure read() {
    checkUsed();
    try {
      JsonToken token = jsonParser.hasCurrentToken() ? jsonParser.currentToken() : jsonParser.nextToken();
      if (token == JsonToken.START_ARRAY) {
        return doReadArray();
      }
      if (token == JsonToken.START_OBJECT) {
        return doReadObject(true);
      }
      throw new JsonParsingException("Cannot create structure when next item in JSON stream is " + token, getLocation());
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }


  @Override
  public JsonArray readArray() {
    checkUsed();
    try {
      JsonToken token = jsonParser.hasCurrentToken() ? jsonParser.currentToken() : jsonParser.nextToken();
      if (token == JsonToken.START_ARRAY) {
        return doReadArray();
      }
      throw new JsonParsingException("Cannot create array when next item in JSON stream is " + token, getLocation());
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }


  @Override
  public JsonObject readObject() {
    checkUsed();
    try {
      JsonToken token = jsonParser.hasCurrentToken() ? jsonParser.currentToken() : jsonParser.nextToken();
      if (token == JsonToken.START_OBJECT) {
        return doReadObject(true);
      }
      throw new JsonParsingException("Cannot create object when next item in JSON stream is " + token, getLocation());
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }


  @Override
  public JsonValue readValue() {
    checkUsed();
    try {
      JsonToken token = jsonParser.hasCurrentToken() ? jsonParser.currentToken() : jsonParser.nextToken();
      return doReadValue(token);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

}
