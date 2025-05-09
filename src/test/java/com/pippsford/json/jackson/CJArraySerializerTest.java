package com.pippsford.json.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.pippsford.json.CJArray;

/**
 * @author Simon Greatrix on 2020-01-07.
 */
public class CJArraySerializerTest {

  @Test
  public void acceptVisitor() throws JsonMappingException {
    JsonArraySerializer instance = new JsonArraySerializer();
    JsonFormatVisitorWrapper visitorWrapper = Mockito.mock(JsonFormatVisitorWrapper.class);
    instance.acceptJsonFormatVisitor(visitorWrapper, TypeFactory.defaultInstance().constructType(CJArray.class));
    Mockito.verify(visitorWrapper).expectArrayFormat(any());
  }


  @Test
  public void getType() {
    JsonArraySerializer instance = new JsonArraySerializer();
    assertEquals(CJArray.class, instance.handledType());
  }

}
