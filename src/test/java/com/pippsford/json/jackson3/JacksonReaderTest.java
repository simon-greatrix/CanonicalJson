package com.pippsford.json.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.pippsford.json.CJObject;
import com.pippsford.json.builder.ArrayBuilder;
import com.pippsford.json.builder.ObjectBuilder;
import com.pippsford.json.exception.JsonIOException;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.smile.SmileFactory;

/**
 * @author Simon Greatrix on 31/01/2020.
 */
public class JacksonReaderTest {

  @Test
  public void test1() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SmileFactory smileFactory = new SmileFactory();
    JsonGenerator smileGenerator = smileFactory.createGenerator(outputStream);
    JacksonGenerator jacksonGenerator = new JacksonGenerator(smileGenerator);

    JsonObject object = new ObjectBuilder()
        .add("a", 1)
        .add("b", new ArrayBuilder().add("x").add("y").add("z"))
        .addNull("c")
        .add("d", new ObjectBuilder().add("e", true).add("f", false))
        .add("g", 1000000000000L)
        .add("h", BigInteger.ONE.shiftLeft(70))
        .add("i", new BigDecimal("1.234"))
        .build();

    jacksonGenerator.generate(object);
    jacksonGenerator.close();

    JsonParser smileParser = smileFactory.createParser(outputStream.toByteArray());
    JacksonReader jacksonReader = new JacksonReader(smileParser);
    JsonValue output = jacksonReader.read();
    jacksonReader.close();

    assertEquals(object.toString(), output.toString());
  }


  @Test
  public void test10() throws IOException {
    JsonParser mock = Mockito.mock(JsonParser.class);
    Mockito.doThrow(JacksonIOException.construct(new IOException("test"))).when(mock).nextToken();
    JacksonReader parser = new JacksonReader(mock);

    JsonIOException e = assertThrows(JsonIOException.class, parser::read);
    assertEquals("test", e.getCause().getMessage());
  }


  @Test
  public void test11() throws IOException {
    JsonParser mock = Mockito.mock(JsonParser.class);
    Mockito.doThrow(JacksonIOException.construct(new IOException("test"))).when(mock).nextToken();
    JacksonReader parser = new JacksonReader(mock);

    JsonIOException e = assertThrows(JsonIOException.class, parser::readArray);
    assertEquals("test", e.getCause().getMessage());
  }


  @Test
  public void test12() throws IOException {
    JsonParser mock = Mockito.mock(JsonParser.class);
    Mockito.doThrow(JacksonIOException.construct(new IOException("test"))).when(mock).nextToken();
    JacksonReader parser = new JacksonReader(mock);

    JsonIOException e = assertThrows(JsonIOException.class, () -> parser.readObject());
    assertEquals("test", e.getCause().getMessage());
  }


  @Test
  public void test14() throws IOException {
    SmileFactory smileFactory = new SmileFactory();
    JsonParser smileParser = smileFactory.createParser(new byte[0]);
    JacksonReader jacksonReader = new JacksonReader(smileParser);

    JsonParsingException e = assertThrows(JsonParsingException.class, () -> jacksonReader.readValue());
    assertEquals("Value not found", e.getMessage());
  }


  @Test
  public void test15() throws IOException {
    JsonMapper mapper = JsonMapper.builder()
        .addModule(new CanonicalJsonModule())
        .build();
    CJObject object = mapper.readValue("{\"a\":1}", CJObject.class);
  }


  @Test
  public void test2() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SmileFactory smileFactory = new SmileFactory();
    JsonGenerator smileGenerator = smileFactory.createGenerator(outputStream);
    JacksonGenerator jacksonGenerator = new JacksonGenerator(smileGenerator);
    jacksonGenerator.generate(null);
    jacksonGenerator.generate(JsonValue.TRUE);
    jacksonGenerator.close();

    JsonParser smileParser = smileFactory.createParser(outputStream.toByteArray());
    JacksonReader jacksonReader = new JacksonReader(smileParser);
    JsonValue output = jacksonReader.readValue();
    assertEquals(JsonValue.NULL, output);

    try {
      // should not be able to read two values
      jacksonReader.readValue();
      fail();
    } catch (IllegalStateException e) {
      // correct
    }
  }


  @Test
  public void test3() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SmileFactory smileFactory = new SmileFactory();
    JsonGenerator smileGenerator = smileFactory.createGenerator(outputStream);
    JacksonGenerator jacksonGenerator = new JacksonGenerator(smileGenerator);

    JsonObject object = new ObjectBuilder()
        .add("a", 1)
        .add("b", new ArrayBuilder().add("x").add("y").add("z"))
        .addNull("c")
        .add("d", new ObjectBuilder().add("e", true).add("f", false))
        .add("g", 1000000000000L)
        .add("h", BigInteger.ONE.shiftLeft(70))
        .add("i", new BigDecimal("1.234"))
        .build();

    jacksonGenerator.generate(object);
    jacksonGenerator.close();

    JsonParser smileParser = smileFactory.createParser(outputStream.toByteArray());
    JacksonReader jacksonReader = new JacksonReader(smileParser);
    JsonObject output = jacksonReader.readObject();
    jacksonReader.close();

    assertEquals(object.toString(), output.toString());
  }


  @Test
  public void test4() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SmileFactory smileFactory = new SmileFactory();
    JsonGenerator smileGenerator = smileFactory.createGenerator(outputStream);
    JacksonGenerator jacksonGenerator = new JacksonGenerator(smileGenerator);

    jacksonGenerator.generate(JsonValue.TRUE);
    jacksonGenerator.close();

    JsonParser smileParser = smileFactory.createParser(outputStream.toByteArray());
    JacksonReader jacksonReader = new JacksonReader(smileParser);

    JsonParsingException e = assertThrows(JsonParsingException.class, () -> jacksonReader.readObject());
    assertEquals("Cannot create object when next item in JSON stream is VALUE_TRUE", e.getMessage());
  }


  @Test
  public void test5() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SmileFactory smileFactory = new SmileFactory();
    JsonGenerator smileGenerator = smileFactory.createGenerator(outputStream);
    JacksonGenerator jacksonGenerator = new JacksonGenerator(smileGenerator);

    jacksonGenerator.generate(JsonValue.TRUE);
    jacksonGenerator.close();

    JsonParser smileParser = smileFactory.createParser(outputStream.toByteArray());
    JacksonReader jacksonReader = new JacksonReader(smileParser);

    JsonParsingException e = assertThrows(JsonParsingException.class, () -> jacksonReader.readArray());
    assertEquals("Cannot create array when next item in JSON stream is VALUE_TRUE", e.getMessage());
  }


  @Test
  public void test6() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SmileFactory smileFactory = new SmileFactory();
    JsonGenerator smileGenerator = smileFactory.createGenerator(outputStream);
    JacksonGenerator jacksonGenerator = new JacksonGenerator(smileGenerator);

    jacksonGenerator.generate(JsonValue.TRUE);
    jacksonGenerator.close();

    JsonParser smileParser = smileFactory.createParser(outputStream.toByteArray());
    JacksonReader jacksonReader = new JacksonReader(smileParser);

    JsonParsingException e = assertThrows(JsonParsingException.class, () -> jacksonReader.read());
    assertEquals("Cannot create structure when next item in JSON stream is VALUE_TRUE", e.getMessage());
  }


  @Test
  public void test7() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SmileFactory smileFactory = new SmileFactory();
    JsonGenerator smileGenerator = smileFactory.createGenerator(outputStream);
    JacksonGenerator jacksonGenerator = new JacksonGenerator(smileGenerator);

    JsonArray array = new ArrayBuilder().add("x").add("y").add("z")
        .add(new ObjectBuilder().add("e", true).add("f", false))
        .add(new ArrayBuilder().add(1).add(1.234).add(true))
        .build();

    jacksonGenerator.generate(array);
    jacksonGenerator.close();

    JsonParser smileParser = smileFactory.createParser(outputStream.toByteArray());
    JacksonReader jacksonReader = new JacksonReader(smileParser);
    JsonStructure output = jacksonReader.read();
    jacksonReader.close();

    assertEquals(array.toString(), output.toString());
  }


  @Test
  public void test8() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SmileFactory smileFactory = new SmileFactory();
    JsonGenerator smileGenerator = smileFactory.createGenerator(outputStream);
    JacksonGenerator jacksonGenerator = new JacksonGenerator(smileGenerator);

    JsonArray array = new ArrayBuilder().add("x").add("y").add("z")
        .add(new ObjectBuilder().add("e", true).add("f", false))
        .add(new ArrayBuilder().add(1).add(1.234).add(true))
        .build();

    jacksonGenerator.generate(array);
    jacksonGenerator.close();

    JsonParser smileParser = smileFactory.createParser(outputStream.toByteArray());
    JacksonReader jacksonReader = new JacksonReader(smileParser);
    JsonStructure output = jacksonReader.readArray();
    jacksonReader.close();

    assertEquals(array.toString(), output.toString());
  }


  @Test
  public void test9() throws IOException {
    JsonParser mock = Mockito.mock(JsonParser.class);
    Mockito.doThrow(JacksonIOException.construct(new IOException("test"))).when(mock).nextToken();
    JacksonReader parser = new JacksonReader(mock);
    JsonIOException e = assertThrows(JsonIOException.class, () -> parser.readValue());
    assertEquals("test", e.getCause().getMessage());
  }

}
