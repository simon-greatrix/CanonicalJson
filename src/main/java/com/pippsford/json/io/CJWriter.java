package com.pippsford.json.io;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.stream.JsonGenerator;

/**
 * A JSON writer implementation. The format of the output is controlled by a specified generator.
 *
 * @author Simon Greatrix on 10/01/2020.
 */
public class CJWriter implements JsonWriter {

  private final JsonGenerator generator;

  private boolean isUsed = false;


  CJWriter(JsonGenerator generator) {
    this.generator = generator;
  }


  private void checkUsed() {
    if (isUsed) {
      throw new IllegalStateException("This JsonWriter has already been used");
    }
    isUsed = true;
  }


  @Override
  public void close() {
    isUsed = true;
    generator.close();
  }


  @Override
  public void write(JsonValue value) {
    checkUsed();
    generator.write(value);
  }


  @Override
  public void write(JsonStructure value) {
    write((JsonValue) value);
  }


  @Override
  public void writeArray(JsonArray array) {
    write((JsonValue) array);
  }


  @Override
  public void writeObject(JsonObject object) {
    write((JsonValue) object);
  }

}
