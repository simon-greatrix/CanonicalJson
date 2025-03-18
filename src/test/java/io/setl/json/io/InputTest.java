package io.setl.json.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import jakarta.json.stream.JsonParsingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InputTest {

  @Test
  void close() throws IOException {
    Reader reader = mock(Reader.class);
    Input input = new Input(reader);
    input.close();
    verify(reader).close();
  }


  @Test
  void closeFails() throws IOException {
    Reader reader = mock(Reader.class);
    doThrow(new IOException()).when(reader).close();
    Input input = new Input(reader);
    assertThrows(JsonParsingException.class, () -> input.close());
    verify(reader).close();
  }


  @ParameterizedTest
  @CsvSource({
      "'',1,0,0",
      "'a',1,1,1",
      "'a\nb',2,1,3",
      "'\n\n\tb',3,10,4",
  })
  void getLocation(String text, int line, int column, long offset) throws IOException {
    Reader reader = new StringReader(text);
    Input input = new Input(reader);
    while (input.read() != -1) {
      // do nothing
    }
    assertEquals(line, input.getLocation().getLineNumber());
    assertEquals(column, input.getLocation().getColumnNumber());
    assertEquals(offset, input.getLocation().getStreamOffset());
  }

  @Test
  void readAndUnread() {
    Reader reader = new StringReader("abc");
    Input input = new Input(reader);
    assertEquals('a', input.read());
    assertEquals('b', input.read());
    input.unread('b');
    assertEquals('b', input.read());
    input.unread('z');
    assertEquals('z', input.read());
    input.unread(-1);
    assertEquals(-1, input.read());
    assertEquals('c', input.read());
    assertEquals(-1, input.read());
    assertEquals(-1, input.read());
    input.unread('z');
    assertEquals('z', input.read());
    assertEquals(-1, input.read());
  }

  @Test
  void readFails() throws IOException {
    Reader reader = mock(Reader.class);
    Input input = new Input(reader);
    doThrow(new IOException()).when(reader).read();
    assertThrows(JsonParsingException.class, input::read);
  }
}
