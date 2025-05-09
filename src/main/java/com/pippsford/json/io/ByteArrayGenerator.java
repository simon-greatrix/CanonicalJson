package com.pippsford.json.io;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A JSON Generator that produces a byte array.
 *
 * @author Simon Greatrix on 24/04/2022.
 */
public class ByteArrayGenerator extends DelegatingGenerator<ByteArrayGenerator> {

  /** The buffer used to store the output. */
  protected final ByteArrayOutputStream buffer = new ByteArrayOutputStream();


  /**
   * New instance.
   *
   * @param factory the factory used to create the generator.
   */
  public ByteArrayGenerator(GeneratorFactory factory) {
    if (factory != null) {
      delegate = factory.createGenerator(buffer);
    } else {
      delegate = new GeneratorFactory(null).createGenerator(buffer);
    }
  }


  /**
   * New instance.
   *
   * @param config the configuration for the factory used to create the generator.
   */
  public ByteArrayGenerator(Map<String, ?> config) {
    delegate = new GeneratorFactory(config).createGenerator(buffer);
  }


  /**
   * New instance using a default generator factory.
   */

  public ByteArrayGenerator() {
    delegate = new GeneratorFactory(null).createGenerator(buffer);
  }


  /**
   * Get the byte array that has been generated.
   *
   * @return the byte array
   */
  public byte[] toByteArray() {
    return buffer.toByteArray();
  }


  @Override
  public String toString() {
    return new String(toByteArray(), StandardCharsets.UTF_8);
  }

}
