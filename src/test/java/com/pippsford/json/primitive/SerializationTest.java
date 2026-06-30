package com.pippsford.json.primitive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import org.junit.jupiter.api.Test;

public class SerializationTest {

  @Test
  public void serialize() throws IOException, ClassNotFoundException {
    CJObject object = CJObject.builder()
        .add(
            "a", CJArray.builder()
                .add(CJArray.builder().add(1).build())
                .add(CJObject.builder().add("a", "b").build())
                .add("string")
                .add(true)
                .add(false)
                .addNull()
                .add(1)
                .add(1L < 48)
                .add(BigInteger.ONE.shiftLeft(100))
                .add(Math.PI)
                .build()
        )
        .add("o", CJObject.builder().add("c", "d").build())
        .add("s", "string")
        .add("t", true)
        .add("f", false)
        .addNull("?")
        .add("i", 2)
        .add("l", 1L < 49)
        .add("bi", BigInteger.TEN.shiftLeft(70))
        .add("bd", Math.PI * Math.E)
        .build();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(object);
    }

    CJObject object2;
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    try (ObjectInputStream ois = new ObjectInputStream(bais)) {
      object2 = (CJObject) ois.readObject();
    }

    assertEquals(object, object2);
  }

}
