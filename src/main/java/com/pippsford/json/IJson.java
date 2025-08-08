package com.pippsford.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.TreeSet;

import com.pippsford.json.exception.ForbiddenIJsonException;
import com.pippsford.json.exception.JsonIOException;
import com.pippsford.json.ijson.IJsonNumberSerializer;
import com.pippsford.json.primitive.numbers.CJNumber;
import jakarta.annotation.Nonnull;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * Support for "Internet JSON" (RFC-7493) and the "JSON Canonicalization Scheme" based upon it (RFC-8785).
 *
 * <p>
 * The "I-JSON" is a subset of JSON. Not all JSON data can be represented in I-JSON. Numeric values are limited to IEEE 64-bit floating point precision.
 * Strings may not contain invalid Unicode such as lone surrogates.
 * </p>
 */
public class IJson {

  /** Bytes for false. */
  private static final byte[] BYTES_FALSE = {(byte) 'f', (byte) 'a', (byte) 'l', (byte) 's', (byte) 'e'};

  /** Bytes for null. */
  private static final byte[] BYTES_NULL = {(byte) 'n', (byte) 'u', (byte) 'l', (byte) 'l'};

  /** Bytes for true. */
  private static final byte[] BYTES_TRUE = {(byte) 't', (byte) 'r', (byte) 'u', (byte) 'e'};

  /** Escapes for the ASCII range. */
  private static final byte[] ESCAPES;

  /** Canonical form uses lower-case hexadecimal. */
  private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


  /**
   * Serialize the JsonValue to the specified stream.
   *
   * @param out   the output stream to write to
   * @param value the JsonValue to write as canonical I-JSON
   */
  public static void serialize(@Nonnull OutputStream out, JsonValue value) {
    try {
      serializeInternal(out, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }


  /**
   * Serialize the JsonValue to a byte array.
   *
   * @param value the value to write
   *
   * @return the JsonValue as canonical I-JSON
   */
  public static byte[] serialize(JsonValue value) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    serialize(baos, value);
    return baos.toByteArray();
  }


  /** Write an array. */
  private static void serializeArray(@Nonnull OutputStream out, JsonArray value) throws IOException {
    if (value.isEmpty()) {
      out.write('[');
      out.write(']');
      return;
    }

    byte prefix = '[';
    for (JsonValue v : value) {
      out.write(prefix);
      prefix = ',';

      serializeInternal(out, v);
    }
    out.write(']');
  }


  private static int serializeAscii(OutputStream out, String value, int i) throws IOException {
    final int l = value.length();
    while (i < l) {
      char ch = value.charAt(i);
      if (ch >= 128) {
        return i;
      }

      switch (ESCAPES[ch]) {
        case 0:
          // normal character
          out.write((byte) ch);
          break;
        case 1:
          // Unicode escape
          out.write('\\');
          out.write('u');
          out.write('0');
          out.write('0');
          out.write(HEX[ch >>> 4]);
          out.write(HEX[ch & 0xf]);
          break;
        default:
          // special escape
          out.write('\\');
          out.write(ESCAPES[ch]);
          break;
      }

      i++;
    }
    return i;
  }


  /** Write a boolean. */
  private static void serializeBoolean(@Nonnull OutputStream out, boolean value) throws IOException {
    out.write(value ? BYTES_TRUE : BYTES_FALSE);
  }


  private static void serializeInternal(@Nonnull OutputStream out, JsonValue value) throws IOException {
    if (value == null) {
      serializeNull(out);
      return;
    }

    switch (value.getValueType()) {
      case OBJECT:
        serializeObject(out, value.asJsonObject());
        break;
      case ARRAY:
        serializeArray(out, value.asJsonArray());
        break;
      case FALSE:
        serializeBoolean(out, false);
        break;
      case TRUE:
        serializeBoolean(out, true);
        break;
      case STRING:
        serializeString(out, ((JsonString) value).getString());
        break;
      case NUMBER:
        serializeNumber(out, (JsonNumber) value);
        break;
      default:
        serializeNull(out);
        break;
    }
  }


  private static void serializeNull(@Nonnull OutputStream out) throws IOException {
    out.write(BYTES_NULL);
  }


  private static void serializeNumber(OutputStream out, JsonNumber value) throws IOException {
    CJNumber cjNumber = CJNumber.cast(value);

    // Integers are so common it is worth optimising for.
    if (cjNumber.getNumberType() == CJNumber.TYPE_INT) {
      IJsonNumberSerializer.serialize(out, value.intValue());
      return;
    }

    IJsonNumberSerializer.serialize(out, value.doubleValue());
  }


  private static void serializeObject(@Nonnull OutputStream out, JsonObject value) throws IOException {
    if (value.isEmpty()) {
      out.write('{');
      out.write('}');
      return;
    }

    // Canonical IJSON uses the same String order that Java uses naturally.
    TreeSet<String> keys = new TreeSet<>(Comparator.naturalOrder());
    keys.addAll(value.keySet());

    byte prefix = '{';
    for (String key : keys) {
      out.write(prefix);
      prefix = ',';

      serializeString(out, key);
      out.write(':');
      serializeInternal(out, value.get(key));
    }
    out.write('}');
  }


  private static void serializeString(OutputStream out, String value) throws IOException {
    // I-JSON strings may not contain non-characters nor lone surrogates per section 2.1 of RFC-7493
    out.write('"');

    int i = 0;
    int len = value.length();
    while (i < len) {
      char ch = value.charAt(i);

      if (ch < 128) {
        // Handle the ASCII characters, and C0 block
        i = serializeAscii(out, value, i);
      } else if (ch < Character.MIN_HIGH_SURROGATE || Character.MAX_LOW_SURROGATE < ch) {
        // Normal character, append as UTF-8
        serializeUTF8(out, ch);
        i++;
      } else {
        // it's a surrogate
        i = serializeSurrogate(out, value, i);
      }
    }

    out.write('"');
  }


  private static int serializeSurrogate(OutputStream out, String value, int i) throws IOException {
    int cp = value.codePointAt(i);
    if (cp < 0x1_0000) {
      throw new ForbiddenIJsonException(String.format("Lone surrogate \\u%04x at index %d", cp, i));
    }
    serializeUTF8(out, cp);
    return i + 2;
  }


  /**
   * Serialize the JsonValue to a String.
   *
   * @param value the value to write
   *
   * @return the JsonValue as canonical I-JSON
   */
  public static String serializeToText(JsonValue value) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    serialize(baos, value);
    return baos.toString(StandardCharsets.UTF_8);
  }


  private static void serializeUTF8(OutputStream out, int cp) throws IOException {
    if (cp < 0x80) {
      out.write(cp);
    } else if (cp < 0x800) {
      out.write(0xc0 | (cp >> 6));
      out.write(0x80 | (cp & 0x3f));
    } else if (cp < 0x1_0000) {
      out.write(0xe0 | (cp >> 12));
      out.write(0x80 | ((cp >> 6)) & 0x3f);
      out.write(0x80 | (cp & 0x3f));
    } else {
      out.write(0xf0 | (cp >> 18));
      out.write(0x80 | ((cp >> 12)) & 0x3f);
      out.write(0x80 | ((cp >> 6)) & 0x3f);
      out.write(0x80 | (cp & 0x3f));
    }
  }


  static {
    byte[] escaped = new byte[128];

    // 0 to 31 must be escaped
    for (int i = 0; i < 32; i++) {
      escaped[i] = 1;
    }

    // These have special escapes
    escaped[8] = 'b';
    escaped[9] = 't';
    escaped[10] = 'n';
    escaped[12] = 'f';
    escaped[13] = 'r';
    escaped['\\'] = '\\';
    escaped['"'] = '"';

    ESCAPES = escaped;
  }

}
