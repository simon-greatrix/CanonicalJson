package io.setl.json.io;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.Map;

import jakarta.json.JsonException;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;

/**
 * A factory for creating JSON Generator instances.
 *
 * @author Simon Greatrix on 27/01/2020.
 */
public class GeneratorFactory implements JsonGeneratorFactory {

  /** The default value for the small structure limit. */
  public static final int DEFAULT_SMALL_STRUCTURE_LIMIT = 30;

  /** The maximum character size for a small structure which will be printed without new-lines. */
  public static final String SMALL_STRUCTURE_LIMIT = "setl.json.generator.smallStructureLimit";

  /** Should the generator trust the client to put keys in canonical order. */
  public static final String TRUST_KEY_ORDER = "setl.json.generator.trustKeyOrder";

  private boolean prettyPrinting = false;

  private int smallStructureLimit = DEFAULT_SMALL_STRUCTURE_LIMIT;

  private boolean trustKeyOrder = false;


  /**
   * Create a new factory. The configuration may specify a boolean value for TRUST_KEY_ORDER. If true, the generator will write immediately to the output
   * without buffering, but the client MUST provide Object keys in the correct order.
   *
   * @param config the configuration
   */
  public GeneratorFactory(Map<String, ?> config) {
    if (config == null) {
      return;
    }

    if (config.containsKey(TRUST_KEY_ORDER)) {
      String val = String.valueOf(config.get(TRUST_KEY_ORDER));
      // defaults to false
      trustKeyOrder = Boolean.parseBoolean(val);
    }
    if (config.containsKey(JsonGenerator.PRETTY_PRINTING)) {
      // The specification says that the value can be anything without saying what any value should mean.
      // We assume that if the value is specified at all, it is probably intended to turn it on, so anything
      // other than "false" is treated as activating pretty printing.
      String val = String.valueOf(config.get(JsonGenerator.PRETTY_PRINTING));
      prettyPrinting = !val.equalsIgnoreCase("false");
    }
    if (config.containsKey(SMALL_STRUCTURE_LIMIT)) {
      Object o = config.get(SMALL_STRUCTURE_LIMIT);
      // Integers are easy
      if (o instanceof Integer) {
        smallStructureLimit = Math.max(0, ((Number) o).intValue());
      } else {
        // Try to convert to a valid number
        try {
          smallStructureLimit = new BigDecimal(String.valueOf(o))
              .setScale(0, RoundingMode.HALF_EVEN)
              .min(BigDecimal.valueOf(Integer.MAX_VALUE))
              .max(BigDecimal.valueOf(0))
              .intValueExact();
        } catch (NumberFormatException e) {
          // just use the length of the string
          smallStructureLimit = ((String) o).length();
        }
      }
    }
  }


  Formatter createFormatter(Appendable appendable) {
    if (prettyPrinting) {
      return new PrettyFormatter(appendable, smallStructureLimit);
    }
    return new NoOpFormatter(appendable);
  }


  /**
   * Create a generator encapsulating the given appendable.
   *
   * @param appendable the appendable
   *
   * @return the generator
   */
  public Generator<?> createGenerator(Appendable appendable) {
    if (trustKeyOrder) {
      return new TrustedGenerator(createFormatter(appendable));
    }
    return new SafeGenerator(createFormatter(appendable));
  }


  @Override
  public Generator<?> createGenerator(OutputStream out) {
    return createGenerator((Appendable) new OutputStreamWriter(out, UTF_8));
  }


  @Override
  public Generator<?> createGenerator(OutputStream out, Charset charset) {
    if (!UTF_8.equals(charset)) {
      throw new JsonException("Canonical JSON must be in UTF-8");
    }
    return createGenerator(out);
  }


  @Override
  public Generator<?> createGenerator(Writer writer) {
    return createGenerator((Appendable) writer);
  }


  @Override
  public Map<String, ?> getConfigInUse() {
    if (prettyPrinting) {
      return Map.of(TRUST_KEY_ORDER, trustKeyOrder, JsonGenerator.PRETTY_PRINTING, true, SMALL_STRUCTURE_LIMIT, smallStructureLimit);
    }
    return Map.of(TRUST_KEY_ORDER, trustKeyOrder, JsonGenerator.PRETTY_PRINTING, false);
  }

}
