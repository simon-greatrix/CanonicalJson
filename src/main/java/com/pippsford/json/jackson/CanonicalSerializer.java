package com.pippsford.json.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.pippsford.json.Canonical;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.util.Map;

/**
 * A serializer of JSON values.
 *
 * @author Simon Greatrix on 2020-01-07.
 */
public class CanonicalSerializer<T extends Canonical> extends JsonSerializer<T> {

  /** New instance. */
  public CanonicalSerializer() {
    // default constructor
  }


  @Override
  public void serialize(Canonical value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
    if (gen instanceof CanonicalGenerator) {
      ((CanonicalGenerator) gen).writeRawCanonicalValue(value);
      return;
    }

    JacksonGenerator jacksonGenerator = new JacksonGenerator(gen);
    jacksonGenerator.generate(value);
  }


  @Override
  public void serializeWithType(Canonical object, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
    gen.assignCurrentValue(object);

    JsonToken token = switch (object.getValueType()) {
      case ARRAY -> JsonToken.START_ARRAY;
      case OBJECT -> JsonToken.START_OBJECT;
      default -> // We use this when the type is neither an array nor an object.
          JsonToken.VALUE_EMBEDDED_OBJECT;
    };

    WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(object, token));

    if (gen instanceof CanonicalGenerator) {
      ((CanonicalGenerator) gen).writeRawCanonicalType(object, token != JsonToken.VALUE_EMBEDDED_OBJECT);
    } else if (token == JsonToken.START_OBJECT) {
      // writeRawValue cannot be used in object context (generator expects field name after writeTypePrefix opens '{').
      JacksonGenerator jg = new JacksonGenerator(gen);
      for (Map.Entry<String, JsonValue> entry : ((JsonObject) object).entrySet()) {
        gen.writeFieldName(entry.getKey());
        jg.generate(entry.getValue());
      }
    } else {
      String text = Canonical.toCanonicalString(object);
      if (token != JsonToken.VALUE_EMBEDDED_OBJECT) {
        // Remove the start and end markers
        text = text.substring(1, text.length() - 1);
      }
      gen.writeRawValue(text);
    }

    typeSer.writeTypeSuffix(gen, typeIdDef);
  }

}
