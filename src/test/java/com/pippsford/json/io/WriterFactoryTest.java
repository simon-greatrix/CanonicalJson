package com.pippsford.json.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.json.JsonException;
import jakarta.json.JsonWriter;
import jakarta.json.stream.JsonGeneratorFactory;
import org.junit.jupiter.api.Test;

import com.pippsford.json.primitive.CJString;

class WriterFactoryTest {

  JsonGeneratorFactory generator = new GeneratorFactory(null);

  JsonGeneratorFactory spy = spy(generator);

  WriterFactory writerFactory = new WriterFactory(spy);


  @Test
  void createWriter() {
    StringWriter writer = new StringWriter();
    assertNotNull(writerFactory.createWriter(writer));
    verify(spy).createGenerator(writer);
  }


  @Test
  void createWriterFromOutputStream() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    JsonWriter writer = writerFactory.createWriter(stream);
    assertNotNull(writer);
    verify(spy).createGenerator(any(Writer.class));

    writer.write(CJString.create("€"));
    writer.close();
    assertArrayEquals("\"€\"".getBytes(StandardCharsets.UTF_8), stream.toByteArray());
  }


  @Test
  void getConfigInUse() {
    Map<String, ?> map = writerFactory.getConfigInUse();
    verify(spy).getConfigInUse();
    assertEquals(map, generator.getConfigInUse());
  }


  @Test
  void testWriterFromOutputStreamAndCharset() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    JsonWriter writer = writerFactory.createWriter(stream, StandardCharsets.UTF_8);
    assertNotNull(writer);
    verify(spy).createGenerator(any(Writer.class));

    writer.write(CJString.create("€"));
    writer.close();
    assertArrayEquals("\"€\"".getBytes(StandardCharsets.UTF_8), stream.toByteArray());

    assertThrows(JsonException.class, () -> writerFactory.createWriter(stream, StandardCharsets.UTF_16));
  }

}
