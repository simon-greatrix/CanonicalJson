package io.setl.json.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.setl.json.exception.JsonIOException;
import io.setl.json.primitive.CJTrue;

class NoOpFormatterTest {

  StringBuilder builder = new StringBuilder();

  NoOpFormatter formatter = new NoOpFormatter(builder);


  @Test
  void close() throws IOException {
    // does nothing
    formatter.close();

    Appendable appendable = mock(Appendable.class, withSettings().extraInterfaces(Closeable.class, Flushable.class));
    formatter = new NoOpFormatter(appendable);
    formatter.close();
    verify((Closeable) appendable).close();

    appendable = mock(Appendable.class, withSettings().extraInterfaces(Closeable.class, Flushable.class));
    doThrow(new IOException()).when((Closeable) appendable).close();
    formatter = new NoOpFormatter(appendable);
    assertThrows(JsonIOException.class, () -> formatter.close());
  }


  @Test
  void flush() throws IOException {
    // does nothing
    formatter.close();

    Appendable appendable = mock(Appendable.class, withSettings().extraInterfaces(Closeable.class, Flushable.class));
    formatter = new NoOpFormatter(appendable);
    formatter.flush();
    verify((Flushable) appendable).flush();

    appendable = mock(Appendable.class, withSettings().extraInterfaces(Closeable.class, Flushable.class));
    doThrow(new IOException()).when((Flushable) appendable).flush();
    formatter = new NoOpFormatter(appendable);
    assertThrows(JsonIOException.class, () -> formatter.flush());
  }


  @Test
  void writeArrayEnd() {
    formatter.writeArrayEnd();
    assertEquals("]", builder.toString());
  }


  @Test
  void writeArrayStart() {
    formatter.writeArrayStart();
    assertEquals("[", builder.toString());
  }


  @Test
  void writeColon() {
    formatter.writeColon();
    assertEquals(":", builder.toString());
  }


  @Test
  void writeComma() throws IOException {
    formatter.writeComma();
    assertEquals(",", builder.toString());

    Appendable appendable = mock(Appendable.class);
    doThrow(new IOException()).when(appendable).append(anyChar());
    formatter = new NoOpFormatter(appendable);
    assertThrows(JsonIOException.class, () -> formatter.writeComma());
  }


  @Test
  void writeKey() throws IOException {
    formatter.writeKey("key");
    assertEquals("\"key\"", builder.toString());

    Appendable appendable = mock(Appendable.class);
    doThrow(new IOException()).when(appendable).append(anyChar());
    formatter = new NoOpFormatter(appendable);
    assertThrows(JsonIOException.class, () -> formatter.writeKey("key"));
  }


  @Test
  void writeObjectEnd() {
    formatter.writeObjectEnd();
    assertEquals("}", builder.toString());
  }


  @Test
  void writeObjectStart() {
    formatter.writeObjectStart();
    assertEquals("{", builder.toString());
  }


  @Test
  void writeValue() throws IOException {
    formatter.write(CJTrue.TRUE);
    assertEquals("true", builder.toString());

    Appendable appendable = mock(Appendable.class);
    doThrow(new IOException()).when(appendable).append(any());
    formatter = new NoOpFormatter(appendable);
    assertThrows(JsonIOException.class, () -> formatter.write(CJTrue.TRUE));
  }

}
