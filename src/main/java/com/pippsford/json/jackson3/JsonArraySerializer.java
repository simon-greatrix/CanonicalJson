package com.pippsford.json.jackson3;

import com.pippsford.json.CJArray;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;

/**
 * Serializer for JSON arrays.
 *
 * @author Simon Greatrix on 06/01/2020.
 */
public class JsonArraySerializer extends CanonicalSerializer<CJArray> {

  /** New instance. */
  public JsonArraySerializer() {
    // nothing to do
  }


  @Override
  public void acceptJsonFormatVisitor(
      JsonFormatVisitorWrapper visitor, JavaType type
  ) {
    JsonArrayFormatVisitor v2 = visitor.expectArrayFormat(type);
    if (v2 != null) {
      v2.itemsFormat(CJBaseSerializer.INSTANCE, CJBaseSerializer.TYPE);
    }
  }


  @Override
  public Class<CJArray> handledType() {
    return CJArray.class;
  }

}
