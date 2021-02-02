package io.setl.json.primitive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;

/**
 * @author Simon Greatrix on 15/01/2020.
 */
public class PJsonTest {

  CJJson pJson = new CJJson("wibble");


  @Test
  public void getValue() {
    assertEquals("wibble", pJson.getValue());
  }


  @Test
  public void getValueType() {
    assertNull(pJson.getValueType());
  }


  @Test
  public void testToString() {
    assertEquals("wibble", pJson.toString());
  }


  @Test
  public void writeTo() throws IOException {
    StringWriter writer = new StringWriter();
    pJson.writeTo(writer);
    writer.flush();
    assertEquals("wibble", writer.toString());
  }
}