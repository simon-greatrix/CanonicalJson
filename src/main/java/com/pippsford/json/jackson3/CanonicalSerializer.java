package com.pippsford.json.jackson3;

import com.pippsford.json.Canonical;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.WritableTypeId;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;

/**
 * A serializer of JSON values.
 *
 * @author Simon Greatrix on 2020-01-07.
 */
public class CanonicalSerializer<T extends Canonical> extends ValueSerializer<T> {

  /** New instance. */
  public CanonicalSerializer() {
    // default constructor
  }


  @Override
  public void serialize(T value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    if (gen instanceof CanonicalGenerator cg) {
      cg.writeRawCanonicalValue(value);
      return;
    }

    JacksonGenerator jacksonGenerator = new JacksonGenerator(gen);
    jacksonGenerator.generate(value);
  }


  @Override
  public void serializeWithType(Canonical object, JsonGenerator gen, SerializationContext context, TypeSerializer typeSer) {
    gen.assignCurrentValue(object);

    JsonToken token = switch (object.getValueType()) {
      case ARRAY -> JsonToken.START_ARRAY;
      case OBJECT -> JsonToken.START_OBJECT;
      default -> JsonToken.VALUE_EMBEDDED_OBJECT;
    };

    WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, context, typeSer.typeId(object, token));

    if (gen instanceof CanonicalGenerator cg) {
      cg.writeRawCanonicalType(object, token != JsonToken.VALUE_EMBEDDED_OBJECT);
    } else {
      String text = Canonical.toCanonicalString(object);
      if (token != JsonToken.VALUE_EMBEDDED_OBJECT) {
        // Remove the start and end markers
        text = text.substring(1, text.length() - 1);
      }
      gen.writeRawValue(text);
    }

    typeSer.writeTypeSuffix(gen, context, typeIdDef);
  }

}
