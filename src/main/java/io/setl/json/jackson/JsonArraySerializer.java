package io.setl.json.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import io.setl.json.JsonArray;

/**
 * @author Simon Greatrix on 06/01/2020.
 */
public class JsonArraySerializer extends JsonValueSerializer<JsonArray> {

  @Override
  public void acceptJsonFormatVisitor(
      JsonFormatVisitorWrapper visitor, JavaType type
  ) throws JsonMappingException {
    JsonArrayFormatVisitor v2 = visitor.expectArrayFormat(type);
    if (v2 != null) {
      v2.itemsFormat(PrimitiveSerializer.INSTANCE, PrimitiveSerializer.TYPE);
    }
  }


  @Override
  public Class<JsonArray> handledType() {
    return JsonArray.class;
  }
}
