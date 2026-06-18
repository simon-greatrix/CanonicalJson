package com.pippsford.json.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import com.pippsford.json.builder.JsonBuilder;
import com.pippsford.json.jackson.JsonModule;
import com.pippsford.json.jackson3.CanonicalJsonModule;
import com.pippsford.json.patch.ops.Test.Type;
import com.pippsford.json.pointer.JsonExtendedPointer.ResultOfAdd;
import com.pippsford.json.primitive.CJString;
import jakarta.json.JsonException;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.util.Base64;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Simon Greatrix on 11/02/2020.
 */
public class PatchTest {

  Patch patch;


  @BeforeEach
  public void createPatch() {
    PatchBuilder builder = new PatchBuilder();
    builder.add("/a/b/c", 1);
    builder.copy("/a/b/ex1", "/a/b/ex2");
    builder.digest("/a/b/c", "SHA-256", CJString.create("Hello, World!"));
    builder.move("/a/x", "/a/y");
    builder.remove("/c/d");
    builder.replace("/x", "/y");
    builder.test("/a/b/d1", true);

    patch = builder.build();
  }


  @Test
  public void getOperations() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());
    String json = mapper.writeValueAsString(patch);
    Patch copy = mapper.readValue(json, Patch.class);
    assertEquals(patch, copy);
  }


  @Test
  public void getOperations_jackson3() {
    JsonMapper mapper = JsonMapper.builder()
        .addModule(new CanonicalJsonModule())
        .build();
    String json = mapper.writeValueAsString(patch);
    Patch copy = mapper.readValue(json, Patch.class);
    assertEquals(patch, copy);
  }


  @Test
  void testDigest() {
    assertEquals("99914b932bd37a50b983c5e7c90ae93b", HexFormat.of().formatHex(com.pippsford.json.patch.ops.Test.digest("MD5", CJObject.EMPTY)));
    assertEquals("bf21a9e8fbc5a3846fb05b4fa0859e0917b2202f", HexFormat.of().formatHex(com.pippsford.json.patch.ops.Test.digest("SHA1", CJObject.EMPTY)));
    assertEquals("a6202e7635867740d52b081ffef7d187d78aa51e316ef7450de56f916e60eeb8",
        HexFormat.of().formatHex(com.pippsford.json.patch.ops.Test.digest("", CJObject.EMPTY)));
    assertEquals("a6202e7635867740d52b081ffef7d187d78aa51e316ef7450de56f916e60eeb8",
        HexFormat.of().formatHex(com.pippsford.json.patch.ops.Test.digest(null, CJObject.EMPTY)));
    assertThrows(JsonException.class, () -> com.pippsford.json.patch.ops.Test.digest("not-a-digest", CJObject.EMPTY));
  }


  @Test
  public void testEquals() {
    Patch copy = new Patch(patch.toJsonArray());
    assertEquals(patch, copy);
    assertEquals(patch.hashCode(), copy.hashCode());

    var t1 = com.pippsford.json.patch.ops.Test.create("/a/b/c", null, null, ResultOfAdd.CREATE);
    var t2 = com.pippsford.json.patch.ops.Test.create("/a/b/c", null, null, ResultOfAdd.CREATE);
    var t3 = com.pippsford.json.patch.ops.Test.create("/d/e/f", null, null, ResultOfAdd.CREATE);
    var t4 = com.pippsford.json.patch.ops.Test.create("/a/b/c", null, null, ResultOfAdd.UPDATE);

    assertTrue(t1.equals(t1));
    assertTrue(t1.equals(t2));
    assertFalse(t2.equals(null));
    assertFalse(t1.equals(""));
    assertFalse(t1.equals(t3));
    assertFalse(t1.equals(t4));

    CJObject digest = JsonBuilder
        .add("algorithm", "MD5")
        .add("value", Base64.getUrlEncoder().encodeToString(com.pippsford.json.patch.ops.Test.digest("MD5", CJArray.EMPTY)))
        .build();
    var t5 = com.pippsford.json.patch.ops.Test.create("/d/e/f", digest, null, null);
    var t6 = com.pippsford.json.patch.ops.Test.create("/a/b/c", null, digest, null);
    assertFalse(t5.equals(t6));

  }


  @Test
  void testTests() {
    JsonValue value = CJString.create("hello");
    var test = com.pippsford.json.patch.ops.Test.create("/a/b/c", value, null, null);
    assertEquals(Type.VALUE, test.getType());
    assertEquals("/a/b/c", test.getPath());
    assertEquals(value, test.getValue());
    assertNull(test.getDigest());
    assertNull(test.getResultOfAdd());

    assertEquals(test, com.pippsford.json.patch.ops.Test.testValue("/a/b/c", value));

    CJObject digest = JsonBuilder
        .add("algorithm", "MD5")
        .add("value", Base64.getUrlEncoder().encodeToString(com.pippsford.json.patch.ops.Test.digest("MD5", value)))
        .build();
    test = com.pippsford.json.patch.ops.Test.create("/a/b/c2", null, digest, null);
    assertEquals(Type.DIGEST, test.getType());
    assertEquals("/a/b/c2", test.getPath());
    assertNull(test.getValue());
    assertEquals(digest, test.getDigest());
    assertNull(test.getResultOfAdd());

    assertEquals(test, com.pippsford.json.patch.ops.Test.testDigest("/a/b/c2", "MD5", value));

    test = com.pippsford.json.patch.ops.Test.create("/a/b/c3", null, null, ResultOfAdd.CREATE);
    assertEquals(Type.RESULT, test.getType());
    assertEquals("/a/b/c3", test.getPath());
    assertNull(test.getValue());
    assertNull(test.getDigest());
    assertEquals(ResultOfAdd.CREATE, test.getResultOfAdd());

    assertEquals(test, com.pippsford.json.patch.ops.Test.testResult("/a/b/c3", ResultOfAdd.CREATE));

    assertThrows(IllegalArgumentException.class, () ->
        com.pippsford.json.patch.ops.Test.create("/a/b/c", null, null, null));

    assertThrows(IllegalArgumentException.class, () ->
        com.pippsford.json.patch.ops.Test.create("/a/b/c", value, digest, null));

    assertThrows(IllegalArgumentException.class, () ->
        com.pippsford.json.patch.ops.Test.create("/a/b/c", value, null, ResultOfAdd.CREATE));

    assertThrows(IllegalArgumentException.class, () ->
        com.pippsford.json.patch.ops.Test.create("/a/b/c", null, digest, ResultOfAdd.CREATE));
  }


  @Test
  public void testToString() {
    assertEquals(patch.toString(), patch.toJsonArray().toString());
  }
}
