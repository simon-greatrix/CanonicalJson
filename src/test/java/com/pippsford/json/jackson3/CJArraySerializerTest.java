package com.pippsford.json.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.pippsford.json.CJArray;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import tools.jackson.databind.type.TypeFactory;

/**
 * @author Simon Greatrix on 2020-01-07.
 */
public class CJArraySerializerTest {

  @Test
  public void acceptVisitor() {
    JsonArraySerializer instance = new JsonArraySerializer();
    JsonFormatVisitorWrapper visitorWrapper = Mockito.mock(JsonFormatVisitorWrapper.class);
    instance.acceptJsonFormatVisitor(visitorWrapper, TypeFactory.createDefaultInstance().constructType(CJArray.class));
    Mockito.verify(visitorWrapper).expectArrayFormat(any());
  }


  @Test
  public void getType() {
    JsonArraySerializer instance = new JsonArraySerializer();
    assertEquals(CJArray.class, instance.handledType());
  }

}
