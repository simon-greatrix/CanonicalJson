package io.setl.json.jackson;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper.Base;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.setl.json.JsonArray;
import org.junit.Test;

/**
 * @author Simon Greatrix on 2020-01-07.
 */
public class JsonArraySerializerTest {

  @Test
  public void acceptVisitor() throws JsonMappingException {
    JsonArraySerializer instance = new JsonArraySerializer();
    instance.acceptJsonFormatVisitor(new Base(), TypeFactory.defaultInstance().constructType(JsonArray.class));
  }


  @Test
  public void getType() {
    JsonArraySerializer instance = new JsonArraySerializer();
    assertEquals(JsonArray.class, instance.handledType());
  }

}