package com.pippsford.json.io;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import jakarta.json.JsonException;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;

/**
 * A factory for JSON writers.
 *
 * @author Simon Greatrix on 10/01/2020.
 */
public class WriterFactory implements JsonWriterFactory {

  /** A factory with the default configuration. */
  public static final WriterFactory STANDARD = new WriterFactory(new GeneratorFactory(Map.of()));

  /** A factory with configuration to produce compact JSON and to trust the input data submits the object keys in the correct order. */
  public static final WriterFactory TRUSTED = new WriterFactory(
      new GeneratorFactory(
          Map.of(GeneratorFactory.TRUST_KEY_ORDER, true, JsonGenerator.PRETTY_PRINTING, false)
      )
  );

  private final JsonGeneratorFactory generatorFactory;


  /**
   * New instance.
   *
   * @param generatorFactory the factory used to create writers.
   */
  public WriterFactory(JsonGeneratorFactory generatorFactory) {
    this.generatorFactory = generatorFactory;
  }


  @Override
  public JsonWriter createWriter(Writer writer) {
    return new CJWriter(generatorFactory.createGenerator(writer));
  }


  @Override
  public JsonWriter createWriter(OutputStream out) {
    return new CJWriter(generatorFactory.createGenerator(new OutputStreamWriter(out, UTF_8)));
  }


  @Override
  public JsonWriter createWriter(OutputStream out, Charset charset) {
    if (!UTF_8.equals(charset)) {
      throw new JsonException("Canonical JSON must be in UTF-8");
    }
    return createWriter(out);
  }


  @Override
  public Map<String, ?> getConfigInUse() {
    return generatorFactory.getConfigInUse();
  }

}
