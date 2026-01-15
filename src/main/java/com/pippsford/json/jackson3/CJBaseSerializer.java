package com.pippsford.json.jackson3;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import tools.jackson.databind.type.TypeFactory;
import com.pippsford.json.primitive.CJBase;

/**
 * A writer of JSON values.
 *
 * @author Simon Greatrix on 06/01/2020.
 */
public class CJBaseSerializer extends CanonicalSerializer<CJBase> {

  static final CJBaseSerializer INSTANCE = new CJBaseSerializer();

  static final JavaType TYPE = TypeFactory.createDefaultInstance().constructType(CJBase.class);


  /** New instance. */
  public CJBaseSerializer() {
    // nothing to do
  }


  @Override
  public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType type) {
    // Cannot narrow the format down as a json value could be anything.
    visitor.expectAnyFormat(type);
  }


  @Override
  public Class<CJBase> handledType() {
    return CJBase.class;
  }


}
