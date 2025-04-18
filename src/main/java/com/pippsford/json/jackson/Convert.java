package com.pippsford.json.jackson;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import com.pippsford.json.primitive.CJFalse;
import com.pippsford.json.primitive.CJNull;
import com.pippsford.json.primitive.CJString;
import com.pippsford.json.primitive.CJTrue;
import com.pippsford.json.primitive.numbers.CJNumber;

/**
 * A utility class to convert between Jackson's JsonNode and javax's JsonValue.
 *
 * @author Simon Greatrix on 26/02/2020.
 */
public class Convert {

  private static final Map<JsonNodeType, Function<JsonNode, JsonValue>> CONVERTERS;


  private static ArrayNode createArrayNode(JsonNodeCreator nodeCreator, JsonArray jsonArray) {
    ArrayNode arrayNode = nodeCreator.arrayNode(jsonArray.size());
    for (JsonValue value : jsonArray) {
      arrayNode.add(toJackson(nodeCreator, value));
    }
    return arrayNode;
  }


  private static JsonValue createJsonArray(ArrayNode node) {
    int s = node.size();
    CJArray array = new CJArray(s);
    for (int i = 0; i < s; i++) {
      array.add(toJson(node.get(i)));
    }
    return array;
  }


  private static JsonValue createJsonObject(ObjectNode node) {
    CJObject object = new CJObject();
    Iterator<Entry<String, JsonNode>> iterator = node.fields();
    while (iterator.hasNext()) {
      Entry<String, JsonNode> entry = iterator.next();
      object.put(entry.getKey(), toJson(entry.getValue()));
    }
    return object;
  }


  private static ValueNode createNumberNode(JsonNodeCreator nodeCreator, JsonNumber value) {
    if (value instanceof CJNumber) {
      CJNumber number = (CJNumber) value;
      switch (number.getNumberType()) {
        case CJNumber.TYPE_INT:
          return nodeCreator.numberNode(number.intValue());
        case CJNumber.TYPE_LONG:
          return nodeCreator.numberNode(number.longValue());
        case CJNumber.TYPE_BIG_INT:
          return nodeCreator.numberNode(number.bigIntegerValue());
        case CJNumber.TYPE_DECIMAL:
          return nodeCreator.numberNode(number.bigDecimalValue());
        default:
          break;
      }
    }

    return value.isIntegral() ? nodeCreator.numberNode(value.bigIntegerValue()) : nodeCreator.numberNode(value.bigDecimalValue());
  }


  private static ObjectNode createObjectNode(JsonNodeCreator nodeCreator, JsonObject jsonObject) {
    ObjectNode objectNode = nodeCreator.objectNode();
    for (Entry<String, JsonValue> entry : jsonObject.entrySet()) {
      objectNode.set(entry.getKey(), toJackson(nodeCreator, entry.getValue()));
    }
    return objectNode;
  }


  /**
   * Convert a javax JsonValue to a Jackson JsonNode.
   *
   * @param nodeCreator factory for Jackson nodes
   * @param value       the value to convert
   *
   * @return the Jackson equivalent
   */
  public static JsonNode toJackson(JsonNodeCreator nodeCreator, JsonValue value) {
    if (value == null) {
      return null;
    }
    switch (value.getValueType()) {
      case OBJECT:
        return createObjectNode(nodeCreator, (JsonObject) value);
      case ARRAY:
        return createArrayNode(nodeCreator, (JsonArray) value);
      case STRING:
        return nodeCreator.textNode(((JsonString) value).getString());
      case NUMBER:
        return createNumberNode(nodeCreator, (JsonNumber) value);
      case NULL:
        return nodeCreator.nullNode();
      case TRUE:
        return nodeCreator.booleanNode(true);
      case FALSE:
        return nodeCreator.booleanNode(false);
      default:
        // should be unreachable
        throw new JsonException("Unknown value type: " + value.getValueType());
    }
  }


  /**
   * Convert a javax JsonValue to a Jackson JsonNode.
   *
   * @param value the value to convert
   *
   * @return the Jackson equivalent
   */
  public static JsonNode toJackson(JsonValue value) {
    return toJackson(JsonNodeFactory.withExactBigDecimals(false), value);
  }


  /**
   * Convert a javax JsonArray to a Jackson ArrayNode.
   *
   * @param value the value to convert
   *
   * @return the Jackson equivalent
   */
  public static ArrayNode toJackson(JsonArray value) {
    return (ArrayNode) toJackson(JsonNodeFactory.withExactBigDecimals(false), value);
  }


  /**
   * Convert a javax JsonObject to a Jackson ObjectNode.
   *
   * @param value the value to convert
   *
   * @return the Jackson equivalent
   */
  public static ObjectNode toJackson(JsonObject value) {
    return (ObjectNode) toJackson(JsonNodeFactory.withExactBigDecimals(false), value);
  }


  /**
   * Convert a javax JsonStructure to a Jackson ContainerNode.
   *
   * @param value the value to convert
   * @param <T>   the Jackson structural node type
   *
   * @return the Jackson equivalent
   */
  @SuppressWarnings("unchecked")
  public static <T extends ContainerNode<T>> ContainerNode<T> toJackson(JsonStructure value) {
    return (ContainerNode<T>) toJackson(JsonNodeFactory.withExactBigDecimals(false), value);
  }


  /**
   * Convert a Jackson container to a javax JsonStructure.
   *
   * @param node the node to convert
   *
   * @return the equivalent JsonStructure
   */
  public static JsonStructure toJson(ContainerNode<?> node) {
    return (JsonStructure) toJson((JsonNode) node);
  }


  /**
   * Convert a Jackson ObjectNode to a javax JsonObject.
   *
   * @param node the node to convert
   *
   * @return the equivalent JsonStructure
   */
  public static JsonObject toJson(ObjectNode node) {
    return (JsonObject) toJson((JsonNode) node);
  }


  /**
   * Convert a Jackson ArrayNode to a javax JsonArray.
   *
   * @param node the node to convert
   *
   * @return the equivalent JsonStructure
   */
  public static JsonArray toJson(ArrayNode node) {
    return (JsonArray) toJson((JsonNode) node);
  }


  /**
   * Convert a Jackson JsonNode to a javax JsonValue.
   *
   * @param node the node to convert
   *
   * @return the equivalent JsonValue
   */
  public static JsonValue toJson(JsonNode node) {
    Function<JsonNode, JsonValue> converter = CONVERTERS.get(node.getNodeType());
    if (converter != null) {
      return converter.apply(node);
    }

    // should be unreachable
    throw new JsonException("Unknown Jackson node type: " + node.getNodeType());
  }


  static {
    // Map node types to the functions that convert the Jackson node to a javax value
    Map<JsonNodeType, Function<JsonNode, JsonValue>> map = new EnumMap<>(JsonNodeType.class);
    map.put(JsonNodeType.OBJECT, n -> createJsonObject((ObjectNode) n));
    map.put(JsonNodeType.ARRAY, n -> createJsonArray((ArrayNode) n));
    map.put(JsonNodeType.STRING, n -> CJString.create(n.textValue()));
    map.put(JsonNodeType.BOOLEAN, n -> n.booleanValue() ? CJTrue.TRUE : CJFalse.FALSE);
    map.put(JsonNodeType.NULL, n -> CJNull.NULL);
    map.put(JsonNodeType.NUMBER, n -> CJNumber.cast(n.numberValue()));

    map.put(
        JsonNodeType.BINARY, n -> {
          try {
            return CJString.create(Base64.getEncoder().encodeToString(n.binaryValue()));
          } catch (IOException ioe) {
            throw new JsonException("Jackson failure", ioe);
          }
        }
    );

    // Unsupported types
    map.put(
        JsonNodeType.POJO, n -> {
          throw new JsonException("Jackson POJO nodes are not supported");
        }
    );
    map.put(
        JsonNodeType.MISSING, n -> {
          throw new JsonException("Jackson MISSING nodes are not supported");
        }
    );

    CONVERTERS = Collections.unmodifiableMap(map);
  }


  private Convert() {
    // do nothing
  }

}
