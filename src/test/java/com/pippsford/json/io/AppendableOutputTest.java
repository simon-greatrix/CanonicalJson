package com.pippsford.json.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Map;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerationException;
import jakarta.json.stream.JsonGenerator;
import org.junit.jupiter.api.Test;

import com.pippsford.json.CJArray;
import com.pippsford.json.builder.ArrayBuilder;
import com.pippsford.json.builder.ObjectBuilder;
import com.pippsford.json.exception.JsonIOException;
import com.pippsford.json.io.PrettyOutput.Special;

class AppendableOutputTest {

  CJArray testArray = new ArrayBuilder()
      .add(1)
      .add(JsonValue.EMPTY_JSON_ARRAY)
      .add(JsonValue.EMPTY_JSON_OBJECT)
      .add(new ObjectBuilder()
          .add("a", 1)
          .add("b", 2))
      .add(new ObjectBuilder()
          .add("a", 1))
      .add(new ArrayBuilder()
          .add(1)
          .add(2))
      .add(new ArrayBuilder()
          .add(1))
      .add(new ArrayBuilder()
          .add(JsonValue.EMPTY_JSON_ARRAY))
      .build();


  @Test
  void append() throws IOException {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 5);
    PrettyOutput a2 = a1.append("abc");
    assertSame(a1, a2);
    assertEquals("abc", sb.toString());
  }


  @Test
  void append2() throws IOException {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 5);
    PrettyOutput a2 = a1.append("abc", 0, 2);
    assertSame(a1, a2);
    assertEquals("ab", sb.toString());
  }


  @Test
  void append2ShouldHandleIOException() throws IOException {
    IOException ioException = new IOException("Test");
    Appendable appendable = mock(Appendable.class);
    when(appendable.append(any(CharSequence.class), anyInt(), anyInt())).thenThrow(ioException);
    AppendableOutput appendableOutput = new AppendableOutput(appendable, 5);

    JsonIOException e = assertThrows(JsonIOException.class, () -> appendableOutput.append("abc", 0, 3));
    assertEquals(ioException, e.getCause());
  }


  @Test
  void appendChar() throws IOException {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 5);
    PrettyOutput a2 = a1.append('z');
    assertSame(a1, a2);
    assertEquals("z", sb.toString());
  }


  @Test
  void appendCharShouldHandleIOException() throws IOException {
    IOException ioException = new IOException("Test");
    Appendable appendable = mock(Appendable.class);
    when(appendable.append(anyChar())).thenThrow(ioException);
    AppendableOutput appendableOutput = new AppendableOutput(appendable, 5);

    JsonIOException e = assertThrows(JsonIOException.class, () -> appendableOutput.append('a'));
    assertEquals(ioException, e.getCause());
  }


  @Test
  void appendShouldHandleIOException() throws IOException {
    IOException ioException = new IOException("Test");
    Appendable appendable = mock(Appendable.class);
    when(appendable.append(any(CharSequence.class))).thenThrow(ioException);
    AppendableOutput appendableOutput = new AppendableOutput(appendable, 5);

    JsonIOException e = assertThrows(JsonIOException.class, () -> appendableOutput.append("abc"));
    assertEquals(ioException, e.getCause());
  }


  @Test
  void close() throws IOException {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 5);
    a1.close();
    assertEquals("", sb.toString());

    IOException ioException = new IOException("Test");
    Appendable cl = mock(Appendable.class, withSettings().extraInterfaces(Closeable.class));
    doThrow(ioException).when((Closeable) cl).close();
    AppendableOutput a2 = new AppendableOutput(cl, 5);
    JsonIOException e = assertThrows(JsonIOException.class, () -> a2.close());
    assertEquals(ioException, e.cause());
  }


  @Test
  void closeInsideEmptyStructure() {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 0);
    assertThrows(JsonGenerationException.class, () -> a1.append(Special.START_ARRAY).close());
  }


  @Test
  void closeInsideSmallStructure() {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 6);
    assertThrows(JsonGenerationException.class, () -> a1.append(Special.START_ARRAY).close());
  }


  @Test
  void emptyStructureConvertsOk() {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 2);
    a1.append(Special.START_ARRAY)
        .append("123")
        .append(Special.END_ARRAY)
        .append(Special.SEPARATOR)
        .append(Special.START_ARRAY)
        .append("456", 0, 3)
        .append(Special.END_ARRAY)
        .append(Special.SEPARATOR)
        .append(Special.START_ARRAY)
        .append(new char[]{'7', '8', '9'}, 0, 3)
        .append(Special.END_ARRAY);
    assertEquals("[\n  123\n],\n[\n  456\n],\n[\n  789\n]", sb.toString());
  }


  @Test
  void emptyStructureStaysEmpty() {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 2);
    a1.append(Special.START_ARRAY)
        .append("")
        .append("123", 1, 1)
        .append(new char[]{'4', '5', '6'}, 2, 2)
        .append(Special.END_ARRAY);
    assertEquals("[]", sb.toString());
  }


  @Test
  void flush() throws IOException {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 5);
    PrettyOutput p = a1.flush();
    assertEquals("", sb.toString());
    assertSame(p, a1);

    IOException ioException = new IOException("Test");
    Appendable cl = mock(Appendable.class, withSettings().extraInterfaces(Flushable.class));
    doThrow(ioException).when((Flushable) cl).flush();
    AppendableOutput a2 = new AppendableOutput(cl, 5);
    JsonIOException e = assertThrows(JsonIOException.class, () -> a2.flush());
    assertEquals(ioException, e.cause());
  }


  @Test
  void flushInsideEmptyStructure() {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 0);
    a1.append(Special.START_ARRAY).flush().append(Special.END_ARRAY);
    // Not small because of flush
    assertEquals("[\n  \n]", sb.toString());
  }


  @Test
  void flushInsideSmallStructure() {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 20);
    a1.append(Special.START_ARRAY).append("1").flush().append(Special.END_ARRAY);
    // Not small because of flush
    assertEquals("[\n  1\n]", sb.toString());
  }


  private String format(int size, JsonValue value) {
    Map<String, Object> map = Map.of(
        GeneratorFactory.SMALL_STRUCTURE_LIMIT, size,
        JsonGenerator.PRETTY_PRINTING, true
    );
    GeneratorFactory factory = new GeneratorFactory(map);
    Generator generator = new StringGenerator(factory);
    generator.write(value);
    generator.close();
    return generator.toString();
  }


  @Test
  void mismatchedSymbol() throws IOException {
    AppendableOutput a1 = new AppendableOutput(new StringBuilder(), 20);
    assertThrows(IllegalStateException.class, () -> a1.append(Special.START_ARRAY).append(Special.END_OBJECT));

    AppendableOutput a2 = new AppendableOutput(new StringBuilder(), 20);
    assertThrows(IllegalStateException.class, () -> a2.append(Special.START_OBJECT).append(Special.END_ARRAY));
  }


  @Test
  void size0IsBig() throws IOException {
    String json = format(0, testArray);
    assertEquals(
        "[\n"
            + "  1,\n"
            + "  [\n"
            + "  ],\n"
            + "  {\n"
            + "  },\n"
            + "  {\n"
            + "    \"a\": 1,\n"
            + "    \"b\": 2\n"
            + "  },\n"
            + "  {\n"
            + "    \"a\": 1\n"
            + "  },\n"
            + "  [\n"
            + "    1,\n"
            + "    2\n"
            + "  ],\n"
            + "  [\n"
            + "    1\n"
            + "  ],\n"
            + "  [\n"
            + "    [\n"
            + "    ]\n"
            + "  ]\n"
            + "]", json
    );
  }


  @Test
  void size100IsFullyCompactStructures() throws IOException {
    String json = format(100, testArray);
    assertEquals(
        "[ 1, [], {}, { \"a\": 1, \"b\": 2 }, { \"a\": 1 }, [ 1, 2 ], [ 1 ], [ [] ] ]", json
    );
  }


  @Test
  void size10HasSomeCompactStructures() throws IOException {
    String json = format(10, testArray);
    assertEquals(
        "[\n"
            + "  1,\n"
            + "  [],\n"
            + "  {},\n"
            + "  {\n"
            + "    \"a\": 1,\n"
            + "    \"b\": 2\n"
            + "  },\n"
            + "  { \"a\": 1 },\n"
            + "  [ 1, 2 ],\n"
            + "  [ 1 ],\n"
            + "  [ [] ]\n"
            + "]", json
    );
  }


  @Test
  void size20HasAllCompactStructures() throws IOException {
    String json = format(20, testArray);
    assertEquals(
        "[\n"
            + "  1,\n"
            + "  [],\n"
            + "  {},\n"
            + "  { \"a\": 1, \"b\": 2 },\n"
            + "  { \"a\": 1 },\n"
            + "  [ 1, 2 ],\n"
            + "  [ 1 ],\n"
            + "  [ [] ]\n"
            + "]", json
    );
  }


  @Test
  void size4HasCompactEmptyStructures() throws IOException {
    String json = format(4, testArray);
    assertEquals(
        "[\n"
            + "  1,\n"
            + "  [],\n"
            + "  {},\n"
            + "  {\n"
            + "    \"a\": 1,\n"
            + "    \"b\": 2\n"
            + "  },\n"
            + "  {\n"
            + "    \"a\": 1\n"
            + "  },\n"
            + "  [\n"
            + "    1,\n"
            + "    2\n"
            + "  ],\n"
            + "  [\n"
            + "    1\n"
            + "  ],\n"
            + "  [\n"
            + "    []\n"
            + "  ]\n"
            + "]", json
    );
  }


  @Test
  void size5HasCompactNonEmptyStructures() throws IOException {
    String json = format(5, testArray);
    assertEquals(
        "[\n"
            + "  1,\n"
            + "  [],\n"
            + "  {},\n"
            + "  {\n"
            + "    \"a\": 1,\n"
            + "    \"b\": 2\n"
            + "  },\n"
            + "  {\n"
            + "    \"a\": 1\n"
            + "  },\n"
            + "  [\n"
            + "    1,\n"
            + "    2\n"
            + "  ],\n"
            + "  [ 1 ],\n"
            + "  [\n"
            + "    []\n"
            + "  ]\n"
            + "]", json
    );
  }


  @Test
  void smallStructureOverflows() {
    StringBuilder sb = new StringBuilder();
    AppendableOutput a1 = new AppendableOutput(sb, 6);
    a1.append(Special.START_ARRAY).append("1234567890").append(Special.END_ARRAY);
    assertEquals("[\n  1234567890\n]", sb.toString());
  }

}
