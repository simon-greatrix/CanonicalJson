package io.setl.json.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.json.JsonPatch;
import org.junit.jupiter.api.Test;

import io.setl.json.CJObject;
import io.setl.json.patch.PatchBuilder;

class IncorrectValueExceptionTest {

  @Test
  void test() {
    CJObject object = new CJObject();
    object.put("a", 1);

    JsonPatch patch = new PatchBuilder().test("/a", "value").build();
    IncorrectValueException e = assertThrows(IncorrectValueException.class, () -> patch.apply(object));
    assertEquals("Test failed. Value at \"/a\" is not \"value\"", e.getMessage());
  }

}
