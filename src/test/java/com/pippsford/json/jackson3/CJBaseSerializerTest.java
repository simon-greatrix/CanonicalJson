package com.pippsford.json.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import tools.jackson.databind.type.TypeFactory;
import com.pippsford.json.primitive.CJBase;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Greatrix on 2020-01-07.
 */
public class CJBaseSerializerTest {

  @Test
  public void acceptVisitor() {
    CJBaseSerializer instance = new CJBaseSerializer();
    JsonFormatVisitorWrapper mock = mock(JsonFormatVisitorWrapper.class);
    instance.acceptJsonFormatVisitor(mock, TypeFactory.createDefaultInstance().constructType(CJBase.class));
    verify(mock).expectAnyFormat(any());
  }


  @Test
  public void getType() {
    CJBaseSerializer instance = new CJBaseSerializer();
    assertEquals(CJBase.class, instance.handledType());
  }

}
