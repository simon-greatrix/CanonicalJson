package com.pippsford.json.ijson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IJsonNumberSerializerTest {

  @Test
  void bigComparisonTest() throws IOException {
    String path;
    try (InputStream inputStream = IJsonNumberSerializerTest.class.getClassLoader().getResourceAsStream("ijson_random_numbers.txt")) {
      Assumptions.assumeTrue(
          inputStream != null,
          () -> "Skipping text as input resource is not available"
      );
      path = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    Assumptions.assumeTrue(
        new File(path).canRead(),
        () -> "Skipping text as input resource is not readable: " + path
    );

    Xoshiro256pp rng = new Xoshiro256pp(new long[]{1L, 2L, 3L, 4L});

    try (
        BufferedReader br = new BufferedReader(
            new InputStreamReader(
                new GZIPInputStream(
                    new FileInputStream(path)
                ),
                StandardCharsets.UTF_8
            )
        )
    ) {
      int lineCount = 0;
      while (true) {
        String line = br.readLine();
        if (line == null) {
          break;
        }
        lineCount++;

        double d;
        do {
          d = Double.longBitsToDouble(rng.next());
        } while (!Double.isFinite(d));

        assertEquals(line, IJsonNumberSerializer.serialize(d));
      }
      System.out.println("Verified: " + lineCount);
    }
  }


  @ParameterizedTest
  @CsvSource({
      "ijson_serial_numbers.txt",
      "ijson_static_numbers.txt"
  })
  void comparisonTest(String resource) throws IOException {
    // To generate input files, see the JS scripts in src/test/js/ijson

    try (InputStream inputStream = IJsonNumberSerializerTest.class.getClassLoader().getResourceAsStream(resource)) {
      Assumptions.assumeTrue(
          inputStream != null,
          () -> "Skipping text as input resource is not available: " + resource
      );
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      while (true) {
        String line = br.readLine();
        if (line == null) {
          break;
        }

        int commaPos = line.indexOf(',');
        long bits = Long.parseLong(line.substring(0, commaPos));
        String json = line.substring(commaPos + 1);
        assertEquals(json, IJsonNumberSerializer.serialize(Double.longBitsToDouble(bits)));
      }
    }
  }


  @Test
  void powersOfTwo() {
    for (long k = -20; k <= 20; k++) {
      double d = Double.longBitsToDouble(((k + 1023L) & 0x7ff) << 52);
      String s = IJsonNumberSerializer.serialize(d);
    }
  }


  @ParameterizedTest
  @CsvSource({
      "0000000000000000,0",
      "8000000000000000,0",
      "0000000000000001,5e-324",
      "8000000000000001,-5e-324",
      "7fefffffffffffff,1.7976931348623157e+308",
      "ffefffffffffffff,-1.7976931348623157e+308",
      "4340000000000000,9007199254740992",
      "c340000000000000,-9007199254740992",
      "4430000000000000,295147905179352830000",
      "44b52d02c7e14af5,9.999999999999997e+22",
      "44b52d02c7e14af6,1e+23",
      "44b52d02c7e14af7,1.0000000000000001e+23",
      "444b1ae4d6e2ef4e,999999999999999700000",
      "444b1ae4d6e2ef4f,999999999999999900000",
      "444b1ae4d6e2ef50,1e+21",
      "3eb0c6f7a0b5ed8c,9.999999999999997e-7",
      "3eb0c6f7a0b5ed8d,0.000001",
      "41b3de4355555553,333333333.3333332",
      "41b3de4355555554,333333333.33333325",
      "41b3de4355555555,333333333.3333333",
      "41b3de4355555556,333333333.3333334",
      "41b3de4355555557,333333333.33333343",
      "becbf647612f3696,-0.0000033333333333333333",
      "43143ff3c1cb0959,1424953923781206.2",
      "0000000100000000,2.121995791e-314",
      "1000000100000000,1.2882309824710566e-231",
      "1000000100000001,1.288230982471057e-231",
      "1000000100000002,1.2882309824710572e-231",
      "1000000100000003,1.2882309824710575e-231",
      "1000000100000004,1.2882309824710577e-231",
      "1000000100000005,1.288230982471058e-231"
  })
  void testBits(String longBits, String json) throws IOException {
    long l = new BigInteger(longBits, 16).longValue();
    double d = Double.longBitsToDouble(l);
    String s = IJsonNumberSerializer.serialize(d);
    assertEquals(json, s);

    if (d != -0.0) {
      assertEquals(d, Double.parseDouble(s));
    }
  }


  @Test
  void testNan() {
    assertThrows(ForbiddenIJsonException.class, () -> IJsonNumberSerializer.serialize(Double.NaN));
    assertThrows(ForbiddenIJsonException.class, () -> IJsonNumberSerializer.serialize(Double.POSITIVE_INFINITY));
    assertThrows(ForbiddenIJsonException.class, () -> IJsonNumberSerializer.serialize(Double.NEGATIVE_INFINITY));
  }


  @Test
  void testNines() {
    for (int i = 10; i <= 25; i++) {
      double d = Double.parseDouble("9".repeat(i));
      String s = IJsonNumberSerializer.serialize(d);
      assertEquals(d, Double.parseDouble(s));
    }
  }


  @Test
  void testRoundOff() {
    for (int i = -300; i < 300; i++) {
      double d = Double.parseDouble("1e" + i);
      String s = IJsonNumberSerializer.serialize(d);
      assertEquals(d, Double.parseDouble(s));
    }
  }


  @ParameterizedTest
  @CsvSource({
      "0,0",
      "0.125,0.125",
      "34,34",
      "18446744073709552000,18446744073709552000",
      "18446744073709553000,18446744073709552000",
      "18446744073709558000,18446744073709560000",
      "4.8,4.8",
      "0.001,0.001",
      "-123e-35,-1.23e-33",
      "-123e+35,-1.23e+37",
      "123e-35,1.23e-33",
      "123e+35,1.23e+37",
      "1234567890.1234567890,1234567890.1234567",
      "-34,-34",
      "-18446744073709552000,-18446744073709552000",
      "-4.8,-4.8",
      "-0.001,-0.001",
      "-1234567890.1234567890,-1234567890.1234567",
      "9.9999999999999999,10",
      "8.8888888888888888,8.88888888888889",
      "7.7777777777777777,7.777777777777778",
      "58032273484243780,58032273484243780",
      "5.51443399744797e-309,5.51443399744797e-309",
      "-996501487687274400,-996501487687274400"
  })
  void testSimple(String text, String json) throws IOException {
    double d = Double.parseDouble(text);
    String s = IJsonNumberSerializer.serialize(d);
    assertEquals(json, s);
    assertEquals(d, Double.parseDouble(s));
  }


  @Test
  void testSmallValues() {
    for (int i = -10_000; i < 10_000; i++) {
      double d = i * 0.01;
      for (int j = 0; j < 5; j++) {
        String json = IJsonNumberSerializer.serialize(d);
        assertEquals(d, Double.parseDouble(json));
        d *= 10;
      }
    }
  }

}
