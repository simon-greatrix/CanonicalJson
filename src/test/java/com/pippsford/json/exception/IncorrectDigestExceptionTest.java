package com.pippsford.json.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.json.JsonPatch;
import org.junit.jupiter.api.Test;

import com.pippsford.json.CJObject;
import com.pippsford.json.patch.PatchBuilder;
import com.pippsford.json.primitive.CJTrue;

class IncorrectDigestExceptionTest {

  @Test
  void test() {
    CJObject object = new CJObject();
    object.put("a", 1);

    JsonPatch patch = new PatchBuilder().digest("/a", "MD5", CJTrue.TRUE).build();
    IncorrectDigestException e = assertThrows(IncorrectDigestException.class, () -> patch.apply(object));
    assertEquals("Test failed. Digest for \"/a\" is \"xMpCOKC5I4INzFCab3WEmw==\".", e.getMessage());
  }

}
