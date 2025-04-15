package com.pippsford.json.io;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.pippsford.json.io.GeneratorFactory.TRUST_KEY_ORDER;

import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.json.JsonException;
import jakarta.json.stream.JsonGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Simon Greatrix on 28/01/2020.
 */
public class GeneratorFactoryTest {

  private static Stream<Arguments> configsToTest() {
    // Possible values we can pass as an input boolean
    Object[] booleanValues = {
        false, "false", "FaLsE",
        true, "true", "tRUe",
        123, "Womble", null
    };

    // How we expect the trust value to be interpreted
    boolean[] expectedTrust = {
        false, false, false,
        true, true, true,
        false, false, false
    };

    // How we expect the pretty value to be interpreted
    boolean[] expectedPretty = {
        false, false, false,
        true, true, true,
        true, true, false
    };

    // Small structure limit values
    Object[] limits = {
        -1, 0, 1,
        "-10", "0", "10",
        Math.PI, Math.E, BigDecimal.TEN,
        Long.MAX_VALUE / 10, "Womble", null
    };

    // How we expect the limit to be interpreted
    Integer[] expectedLimit = {
        0, 0, 1,
        0, 0, 10,
        3, 3, 10,
        Integer.MAX_VALUE, 6, GeneratorFactory.DEFAULT_SMALL_STRUCTURE_LIMIT
    };

    ArrayList<Arguments> argList = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      // Test trust key order
      HashMap<String, Object> input = new HashMap<>();
      if (booleanValues[i] != null) {
        input.put(TRUST_KEY_ORDER, booleanValues[i]);
      }
      HashMap<String, Object> output = new HashMap<>();
      output.put(TRUST_KEY_ORDER, expectedTrust[i]);
      output.put(JsonGenerator.PRETTY_PRINTING, false);
      argList.add(Arguments.of(input, output));

      // Test pretty printing
      input = new HashMap<>();
      if (booleanValues[i] != null) {
        input.put(JsonGenerator.PRETTY_PRINTING, booleanValues[i]);
      }
      output = new HashMap<>();
      output.put(TRUST_KEY_ORDER, false);
      output.put(JsonGenerator.PRETTY_PRINTING, expectedPretty[i]);
      if (expectedPretty[i]) {
        output.put(GeneratorFactory.SMALL_STRUCTURE_LIMIT, GeneratorFactory.DEFAULT_SMALL_STRUCTURE_LIMIT);
      }
      argList.add(Arguments.of(input, output));
    }

    for (int i = 0; i < 12; i++) {
      // Test small structure limit
      HashMap<String, Object> input = new HashMap<>();
      input.put(JsonGenerator.PRETTY_PRINTING, true);
      if (limits[i] != null) {
        input.put(GeneratorFactory.SMALL_STRUCTURE_LIMIT, limits[i]);
      }
      HashMap<String, Object> output = new HashMap<>();
      output.put(TRUST_KEY_ORDER, false);
      output.put(JsonGenerator.PRETTY_PRINTING, true);
      output.put(GeneratorFactory.SMALL_STRUCTURE_LIMIT, expectedLimit[i]);
      argList.add(Arguments.of(input, output));
    }

    return argList.stream();
  }


  GeneratorFactory factory = new GeneratorFactory(null);


  @Test
  public void createGenerator() {
    JsonGenerator generator = factory.createGenerator(Writer.nullWriter());
    assertTrue(generator instanceof SafeGenerator);

    factory = new GeneratorFactory(Map.of(TRUST_KEY_ORDER, "true"));
    generator = factory.createGenerator(Writer.nullWriter());
    assertTrue(generator instanceof TrustedGenerator);
  }


  @ParameterizedTest
  @MethodSource("configsToTest")
  public void getConfigInUse(Map<String, Object> input, Map<String, Object> expected) {
    factory = new GeneratorFactory(input);
    Map<String, ?> map = factory.getConfigInUse();
    assertEquals(expected, map);
  }


  @Test
  public void getDefaultConfigInUse() {
    Map<String, ?> map = factory.getConfigInUse();
    assertEquals(
        Map.of(
            TRUST_KEY_ORDER, false,
            JsonGenerator.PRETTY_PRINTING, false
        ), map
    );
  }


  @Test
  public void testCreateGenerator() {
    JsonGenerator generator = factory.createGenerator(OutputStream.nullOutputStream());
    assertTrue(generator instanceof SafeGenerator);
  }


  @Test
  public void testCreateGenerator1() {
    JsonGenerator generator = factory.createGenerator(OutputStream.nullOutputStream(), UTF_8);
    assertTrue(generator instanceof SafeGenerator);
  }


  @Test
  public void testCreateGenerator2() {
    JsonException exception = assertThrows(JsonException.class, () -> factory.createGenerator(OutputStream.nullOutputStream(), ISO_8859_1));
    assertEquals("Canonical JSON must be in UTF-8", exception.getMessage());
  }

}
