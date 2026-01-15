package com.pippsford.json.jackson3;

import com.pippsford.json.CJObject;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import tools.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor;
import tools.jackson.databind.ser.jdk.JDKKeySerializers.StringKeySerializer;
import tools.jackson.databind.type.TypeFactory;

/**
 * A serializer for JSON objects.
 *
 * @author Simon Greatrix on 06/01/2020.
 */
public class JsonObjectSerializer extends CanonicalSerializer<CJObject> {

  /** Serialize the string key as strings. Weirdly Jackson defines the String Key Serializer as serializing Objects. */
  private static final ValueSerializer<Object> KEY_SERIALIZER = new StringKeySerializer();

  private static final JavaType KEY_TYPE = TypeFactory.createDefaultInstance().constructType(String.class);


  /** New instance. */
  public JsonObjectSerializer() {
    // nothing to do
  }


  @Override
  public void acceptJsonFormatVisitor(
      JsonFormatVisitorWrapper visitor, JavaType type
  ) {
    JsonMapFormatVisitor v2 = visitor.expectMapFormat(type);
    if (v2 != null) {
      v2.keyFormat(KEY_SERIALIZER, KEY_TYPE);
      v2.valueFormat(CJBaseSerializer.INSTANCE, CJBaseSerializer.TYPE);
    }
  }


  @Override
  public Class<CJObject> handledType() {
    return CJObject.class;
  }

}
