package com.pippsford.json.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import tools.jackson.databind.type.TypeFactory;
import com.pippsford.json.CJObject;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Greatrix on 2020-01-07.
 */
public class CJObjectSerializerTest {

  @Test
  public void acceptVisitor() {
    JsonObjectSerializer instance = new JsonObjectSerializer();
    JsonFormatVisitorWrapper mock = mock(JsonFormatVisitorWrapper.class);
    instance.acceptJsonFormatVisitor(mock, TypeFactory.createDefaultInstance().constructType(CJObject.class));
    verify(mock).expectMapFormat(any());
  }


  @Test
  public void getType() {
    JsonObjectSerializer instance = new JsonObjectSerializer();
    assertEquals(CJObject.class, instance.handledType());
  }

}
