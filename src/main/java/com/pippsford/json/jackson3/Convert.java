package com.pippsford.json.jackson3;

import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import com.pippsford.json.primitive.CJFalse;
import com.pippsford.json.primitive.CJNull;
import com.pippsford.json.primitive.CJString;
import com.pippsford.json.primitive.CJTrue;
import com.pippsford.json.primitive.numbers.CJNumber;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ContainerNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.JsonNodeType;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ValueNode;

/**
 * A utility class to convert between Jackson's JsonNode and jakarta's JsonValue.
 *
 * @author Simon Greatrix on 26/02/2020.
 */
public class Convert {

  private static final Map<JsonNodeType, Function<JsonNode, JsonValue>> CONVERTERS;


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
    for (Entry<String, JsonNode> entry : node.properties()) {
      object.put(entry.getKey(), toJson(entry.getValue()));
    }
    return object;
  }


  private static ValueNode createNumberNode(JsonNumber value) {
    if (value instanceof CJNumber number) {
      switch (number.getNumberType()) {
        case CJNumber.TYPE_INT:
          return JsonNodeFactory.instance.numberNode(number.intValue());
        case CJNumber.TYPE_LONG:
          return JsonNodeFactory.instance.numberNode(number.longValue());
        case CJNumber.TYPE_BIG_INT:
          return JsonNodeFactory.instance.numberNode(number.bigIntegerValue());
        case CJNumber.TYPE_DECIMAL:
          return JsonNodeFactory.instance.numberNode(number.bigDecimalValue().stripTrailingZeros());
        default:
          break;
      }
    }

    return value.isIntegral()
        ? JsonNodeFactory.instance.numberNode(value.bigIntegerValue())
        : JsonNodeFactory.instance.numberNode(value.bigDecimalValue().stripTrailingZeros());
  }


  /**
   * Convert a jakarta JsonValue to a Jackson JsonNode.
   *
   * @param value the value to convert
   *
   * @return the Jackson equivalent
   */
  public static JsonNode toJackson(JsonValue value) {
    if (value == null) {
      return null;
    }
    return switch (value.getValueType()) {
      case OBJECT -> toJackson((JsonObject) value);
      case ARRAY -> toJackson((JsonArray) value);
      case STRING -> JsonNodeFactory.instance.stringNode(((JsonString) value).getString());
      case NUMBER -> createNumberNode((JsonNumber) value);
      case NULL -> JsonNodeFactory.instance.nullNode();
      case TRUE -> JsonNodeFactory.instance.booleanNode(true);
      case FALSE -> JsonNodeFactory.instance.booleanNode(false);
    };
  }


  /**
   * Convert a jakarta JsonArray to a Jackson ArrayNode.
   *
   * @param value the value to convert
   *
   * @return the Jackson equivalent
   */
  public static ArrayNode toJackson(JsonArray value) {
    ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode(value.size());
    for (JsonValue v : value) {
      arrayNode.add(toJackson(v));
    }
    return arrayNode;
  }


  /**
   * Convert a jakarta JsonObject to a Jackson ObjectNode.
   *
   * @param value the value to convert
   *
   * @return the Jackson equivalent
   */
  public static ObjectNode toJackson(JsonObject value) {
    ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
    for (Entry<String, JsonValue> entry : value.entrySet()) {
      objectNode.set(entry.getKey(), toJackson(entry.getValue()));
    }
    return objectNode;
  }


  /**
   * Convert a jakarta JsonStructure to a Jackson ContainerNode.
   *
   * @param value the value to convert
   * @param <T>   the Jackson structural node type
   *
   * @return the Jackson equivalent
   */
  @SuppressWarnings("unchecked")
  public static <T extends ContainerNode<T>> ContainerNode<T> toJackson(JsonStructure value) {
    return (ContainerNode<T>) toJackson((JsonValue) value);
  }


  /**
   * Convert a Jackson container to a jakarta JsonStructure.
   *
   * @param node the node to convert
   *
   * @return the equivalent JsonStructure
   */
  public static JsonStructure toJson(ContainerNode<?> node) {
    return (JsonStructure) toJson((JsonNode) node);
  }


  /**
   * Convert a Jackson ObjectNode to a jakarta JsonObject.
   *
   * @param node the node to convert
   *
   * @return the equivalent JsonStructure
   */
  public static JsonObject toJson(ObjectNode node) {
    return (JsonObject) toJson((JsonNode) node);
  }


  /**
   * Convert a Jackson ArrayNode to a jakarta JsonArray.
   *
   * @param node the node to convert
   *
   * @return the equivalent JsonStructure
   */
  public static JsonArray toJson(ArrayNode node) {
    return (JsonArray) toJson((JsonNode) node);
  }


  /**
   * Convert a Jackson JsonNode to a jakarta JsonValue.
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
    // Map node types to the functions that convert the Jackson node to a jakarta value
    Map<JsonNodeType, Function<JsonNode, JsonValue>> map = new EnumMap<>(JsonNodeType.class);
    map.put(JsonNodeType.OBJECT, n -> createJsonObject((ObjectNode) n));
    map.put(JsonNodeType.ARRAY, n -> createJsonArray((ArrayNode) n));
    map.put(JsonNodeType.STRING, n -> CJString.create(n.stringValue()));
    map.put(JsonNodeType.BOOLEAN, n -> n.booleanValue() ? CJTrue.TRUE : CJFalse.FALSE);
    map.put(JsonNodeType.NULL, n -> CJNull.NULL);
    map.put(JsonNodeType.NUMBER, n -> CJNumber.cast(n.numberValue()));
    map.put(JsonNodeType.BINARY, n -> CJString.create(Base64.getEncoder().encodeToString(n.binaryValue())));

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
