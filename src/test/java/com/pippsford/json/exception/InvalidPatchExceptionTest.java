package com.pippsford.json.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.json.JsonArray;
import jakarta.json.JsonPatch;
import org.junit.jupiter.api.Test;

import com.pippsford.json.patch.PatchBuilder;
import com.pippsford.json.primitive.CJString;

class InvalidPatchExceptionTest {

  @Test
  void test() {
    JsonPatch patch = new PatchBuilder().add("/a", 1).build();
    JsonArray array = patch.toJsonArray();
    array.getJsonObject(0).put("op", CJString.create("invalid"));
    InvalidPatchException e = assertThrows(InvalidPatchException.class, ()-> new PatchBuilder(array));
    assertEquals("Unknown operation: \"invalid\"",e.getMessage());
  }

}
