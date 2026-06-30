package com.pippsford.json.jackson;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import org.junit.jupiter.api.Test;

import com.pippsford.json.CJArray;
import com.pippsford.json.builder.ArrayBuilder;
import com.pippsford.json.builder.ObjectBuilder;
import com.pippsford.json.primitive.CJString;

/**
 * @author Simon Greatrix on 28/02/2020.
 */
public class ConvertTest {

  @Test
  public void test() {
    ObjectBuilder builder = new ObjectBuilder();
    builder.add("a", new ArrayBuilder()
            .add(1).add(2.0).add(1_000_000_000_000L).add(BigInteger.ONE.shiftLeft(100)).add(BigDecimal.valueOf(Math.PI)).add("Hello"))
        .add("b0", true)
        .add("b1", false)
        .addNull("n")
        .add("o", new ObjectBuilder().add("x", "y").add("a", "b"));
    JsonObject jsonObject = builder.build();

    JsonNode jsonNode = Convert.toJackson(jsonObject);
    JsonValue jsonValue = Convert.toJson(jsonNode);
    assertEquals(jsonObject.toString(), jsonValue.toString());
  }


  @Test
  public void testArray() {
    JsonArray array = new ArrayBuilder()
        .add(1).add(2.0).add(1_000_000_000_000L).add(BigInteger.ONE.shiftLeft(100)).add(BigDecimal.valueOf(Math.PI)).add("Hello").build();

    ArrayNode jsonNode = Convert.toJackson(array);
    JsonArray jsonValue = Convert.toJson(jsonNode);
    assertEquals(array.toString(), jsonValue.toString());
  }


  @Test
  public void testBinary() throws IOException {
    byte[] binary = new byte[]{0, 1, 2, 3, 4};
    BinaryNode node = new BinaryNode(binary);
    JsonValue json = Convert.toJson(node);
    JsonNode out = Convert.toJackson(json);
    assertArrayEquals(binary, out.binaryValue());
  }


  @Test
  public void testObject() {
    ObjectBuilder builder = new ObjectBuilder();
    builder.add("b0", true)
        .add("b1", false)
        .addNull("n");
    JsonObject jsonObject = builder.build();

    ObjectNode jsonNode = Convert.toJackson(jsonObject);
    JsonObject jsonValue = Convert.toJson(jsonNode);
    assertEquals(jsonObject.toString(), jsonValue.toString());
  }


  @Test
  public void toJacksonJsonValuePrimitives() {
    assertEquals("hello", Convert.toJackson((JsonValue) CJString.create("hello")).textValue());
    assertTrue(Convert.toJackson((JsonValue) JsonValue.TRUE).booleanValue());
    assertTrue(Convert.toJackson((JsonValue) JsonValue.FALSE).isBoolean());
    assertTrue(Convert.toJackson((JsonValue) JsonValue.NULL).isNull());
  }


  @Test
  public void toJacksonJsonValueNull() {
    assertNull(Convert.toJackson((JsonValue) null));
  }


  @Test
  public void toJacksonJsonStructureArray() {
    JsonStructure array = new CJArray(Arrays.asList(1, 2));
    ContainerNode<?> node = Convert.toJackson(array);
    assertTrue(node.isArray());
    assertEquals(2, node.size());
  }


  @Test
  public void toJacksonJsonStructureObject() {
    JsonStructure obj = new ObjectBuilder().add("k", "v").build();
    ContainerNode<?> node = Convert.toJackson(obj);
    assertTrue(node.isObject());
    assertEquals("v", node.get("k").textValue());
  }


  @Test
  public void toJsonContainerNodeArray() {
    ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
    arrayNode.add(10).add(20);
    JsonStructure result = Convert.toJson((ContainerNode<?>) arrayNode);
    assertInstanceOf(JsonArray.class, result);
    assertEquals("[10,20]", result.toString());
  }


  @Test
  public void toJsonContainerNodeObject() {
    ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
    objectNode.put("x", 99);
    JsonStructure result = Convert.toJson((ContainerNode<?>) objectNode);
    assertInstanceOf(JsonObject.class, result);
    assertEquals("{\"x\":99}", result.toString());
  }


  @Test
  public void toJacksonWithNodeCreator() {
    JsonNode node = Convert.toJackson(JsonNodeFactory.instance, CJString.create("test"));
    assertEquals("test", node.textValue());
  }


  @Test
  public void toJsonPojoThrows() {
    POJONode pojoNode = new POJONode("some-object");
    assertThrows(JsonException.class, () -> Convert.toJson(pojoNode));
  }


  @Test
  public void toJsonMissingThrows() {
    assertThrows(JsonException.class, () -> Convert.toJson(MissingNode.getInstance()));
  }

}
