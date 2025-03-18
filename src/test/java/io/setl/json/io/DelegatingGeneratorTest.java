package io.setl.json.io;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.json.stream.JsonGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.setl.json.primitive.CJString;

class DelegatingGeneratorTest {

  JsonGenerator delegate;

  DelegatingGenerator generator;


  @Test
  void close() {
    generator.close();
    verify(delegate).close();
  }


  @Test
  void flush() {
    generator.flush();
    verify(delegate).flush();
  }


  @Test
  void me() {
    assertSame(generator, generator.me());
  }


  @BeforeEach
  void setUp() {
    delegate = mock(JsonGenerator.class);
    generator = new DelegatingGenerator(delegate);
  }


  @Test
  void write() {
    CJString string = CJString.create("foo");
    generator.write(string);
    verify(delegate).write(string);
  }


  @Test
  void writeEnd() {
    generator.writeEnd();
    verify(delegate).writeEnd();
  }


  @Test
  void writeKey() {
    generator.writeKey("foo");
    verify(delegate).writeKey("foo");
  }


  @Test
  void writeStartArray() {
    generator.writeStartArray();
    verify(delegate).writeStartArray();
  }


  @Test
  void writeStartObject() {
    generator.writeStartObject();
    verify(delegate).writeStartObject();
  }

}
