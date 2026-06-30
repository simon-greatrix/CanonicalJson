package com.pippsford.json.io;

import java.io.Reader;

import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import com.pippsford.json.CJStructure;
import com.pippsford.json.Canonical;
import jakarta.json.JsonConfig.KeyStrategy;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParsingException;

import com.pippsford.json.parser.Parser;

/**
 * A JSON reader implementation.
 *
 * @author Simon Greatrix on 10/01/2020.
 */
public class CJReader implements JsonReader {

  private final Parser parser;

  private boolean isUsed = false;


  /**
   * New instance.
   *
   * @param reader      the text source
   * @param keyStrategy the key strategy
   */
  CJReader(Reader reader, KeyStrategy keyStrategy) {
    parser = new Parser(reader, keyStrategy);
  }


  private JsonParsingException badType(String expected, ValueType actual) {
    return new JsonParsingException("Datum was a " + actual + ", not a " + expected, Location.UNSET);
  }


  @Override
  public void close() {
    if (isUsed && parser.hasNext()) {
      // Currently, JParser.hasNext fails if there is more than one root, so this line is unnecessary
      throw new JsonParsingException("Additional data found after first value", parser.getLocation());
    }
    isUsed = true;

    // closing the parser also closes the reader
    parser.close();
  }


  @Override
  public CJStructure read() {
    JsonValue value = readValue();
    if (value instanceof CJStructure) {
      return (CJStructure) value;
    }
    throw badType("STRUCTURE", value.getValueType());
  }


  @Override
  public CJArray readArray() {
    JsonValue value = readValue();
    if (value instanceof CJArray) {
      return (CJArray) value;
    }
    throw badType("ARRAY", value.getValueType());
  }


  @Override
  public CJObject readObject() {
    JsonValue value = readValue();
    if (value instanceof CJObject) {
      return (CJObject) value;
    }
    throw badType("OBJECT", value.getValueType());
  }


  @Override
  public Canonical readValue() {
    if (isUsed) {
      throw new IllegalStateException("This JsonReader has already been used");
    }
    isUsed = true;
    if (!parser.hasNext()) {
      throw new JsonParsingException("No data found in document", Location.UNSET);
    }
    parser.next();
    return parser.getValue();
  }

}
