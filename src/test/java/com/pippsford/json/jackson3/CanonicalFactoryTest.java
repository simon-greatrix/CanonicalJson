package com.pippsford.json.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.pippsford.json.jackson.objects.Pojo;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonEncoding;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.databind.ObjectMapper;

/**
 * @author Simon Greatrix on 03/01/2020.
 */
public class CanonicalFactoryTest {

  CanonicalFactory instance = new CanonicalFactory();


  @Test
  public void canHandleBinaryNatively() {
    assertFalse(instance.canHandleBinaryNatively());
  }


  @Test
  public void createGeneratorNotUtf8() throws IOException {
    UnsupportedOperationException e =
        assertThrows(UnsupportedOperationException.class, () -> instance.createGenerator(ObjectWriteContext.empty(), new File("test"), JsonEncoding.UTF16_BE));
    assertEquals("Canonical encoding must be UTF-8, not UTF16_BE", e.getMessage());
  }


  @Test
  public void createGeneratorNotUtf8_2() throws IOException {
    UnsupportedOperationException e =
        assertThrows(UnsupportedOperationException.class, () -> instance.createGenerator(new ByteArrayOutputStream(), JsonEncoding.UTF16_BE));
    assertEquals("Canonical encoding must be UTF-8, not UTF16_BE", e.getMessage());
  }


  @Test
  public void createGeneratorUtf8_1() throws IOException {
    // We just assume the generator works
    assertNotNull(instance.createGenerator(new ByteArrayOutputStream(), JsonEncoding.UTF8));
  }


  @Test
  public void createGeneratorUtf8_2() throws IOException {
    File file = File.createTempFile("delete_me.", ".json");
    JsonGenerator jsonGenerator = instance.createGenerator(ObjectWriteContext.empty(), file, JsonEncoding.UTF8);

    // We just assume the generator works
    assertNotNull(jsonGenerator);

    jsonGenerator.close();
    file.delete();
  }


  @Test
  public void doTest() {
    ObjectMapper objectMapper = new ObjectMapper(new CanonicalFactory());
    Random random = new Random(0x7e57ab1e);
    Pojo root = new Pojo(random, true);

    String json = objectMapper.writeValueAsString(root);
    assertEquals(
        "{\"count\":24,\"data\":[5.156311201658089E-1,8.997613044339822E-1,8.898458711493542E-1],"
            + "\"sibling\":{\"count\":39,\"data\":[2.6323510263178707E-1,6.379952106951381E-1,9.824964487440964E-1],"
            + "\"sibling\":null,\"text\":\"14z05unwoqriq\"},\"text\":\"-fxc8p6ycqbui\"}", json
    );
  }


  @Test
  public void getFormatName() {
    assertEquals("JSON", instance.getFormatName());
  }


  @Test
  public void requiresPropertyOrdering() {
    assertTrue(instance.requiresPropertyOrdering());
  }

}
