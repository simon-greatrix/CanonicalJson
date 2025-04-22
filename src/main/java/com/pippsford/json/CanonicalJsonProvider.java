package com.pippsford.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import com.pippsford.json.builder.ArrayBuilder;
import com.pippsford.json.builder.BuilderFactory;
import com.pippsford.json.builder.ObjectBuilder;
import com.pippsford.json.io.CJReader;
import com.pippsford.json.io.CJWriter;
import com.pippsford.json.io.Generator;
import com.pippsford.json.io.GeneratorFactory;
import com.pippsford.json.io.ReaderFactory;
import com.pippsford.json.io.WriterFactory;
import com.pippsford.json.merge.Merge;
import com.pippsford.json.merge.MergeDiff;
import com.pippsford.json.parser.CJParser;
import com.pippsford.json.parser.Parser;
import com.pippsford.json.parser.ParserFactory;
import com.pippsford.json.patch.Patch;
import com.pippsford.json.patch.PatchBuilder;
import com.pippsford.json.patch.PatchFactory;
import com.pippsford.json.pointer.JsonExtendedPointer;
import com.pippsford.json.pointer.PointerFactory;
import com.pippsford.json.primitive.CJString;
import com.pippsford.json.primitive.numbers.CJNumber;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;

/**
 * The provider.
 *
 * @author Simon Greatrix on 15/01/2020.
 */
public class CanonicalJsonProvider extends JsonProvider {

  static final GeneratorFactory CANONICAL_GENERATOR_FACTORY = new GeneratorFactory(Map.of(
      GeneratorFactory.TRUST_KEY_ORDER, true
  ));

  static final GeneratorFactory PRETTY_GENERATOR_FACTORY = new GeneratorFactory(Map.of(
      JsonGenerator.PRETTY_PRINTING, true,
      GeneratorFactory.TRUST_KEY_ORDER, true,
      GeneratorFactory.SMALL_STRUCTURE_LIMIT, 20
  ));

  static boolean isToStringPretty = false;


  /**
   * Do "toString()" methods generate canonical or pretty JSON?.
   *
   * @return true if the output will be pretty.
   */
  public static boolean isToStringPretty() {
    return isToStringPretty;
  }


  /**
   * Set the "toString()" method to generate pretty JSON.
   *
   * @param isToStringPretty true if the output should be pretty.
   */
  public static void setIsToStringPretty(boolean isToStringPretty) {
    CanonicalJsonProvider.isToStringPretty = isToStringPretty;
  }


  /** New instance. */
  public CanonicalJsonProvider() {
    // default constructor
  }


  @Override
  public ArrayBuilder createArrayBuilder(JsonArray array) {
    return createBuilderFactory(null).createArrayBuilder(array);
  }


  @Override
  public ArrayBuilder createArrayBuilder(Collection<?> collection) {
    return createBuilderFactory(null).createArrayBuilder(collection);
  }


  @Override
  public ArrayBuilder createArrayBuilder() {
    return new BuilderFactory().createArrayBuilder();
  }


  @Override
  public BuilderFactory createBuilderFactory(Map<String, ?> config) {
    // Our ArrayBuilder and ObjectBuilder do not take any configuration, so we discard what was specified.
    return new BuilderFactory();
  }


  @Override
  public Patch createDiff(JsonStructure source, JsonStructure target) {
    return PatchFactory.create(source, target);
  }


  @Override
  public Generator<?> createGenerator(Writer writer) {
    return createGeneratorFactory(null).createGenerator(writer);
  }


  @Override
  public Generator<?> createGenerator(OutputStream out) {
    return createGeneratorFactory(null).createGenerator(out);
  }


  @Override
  public GeneratorFactory createGeneratorFactory(Map<String, ?> config) {
    return new GeneratorFactory(config);
  }


  @Override
  public Merge createMergeDiff(JsonValue source, JsonValue target) {
    return MergeDiff.create(source, target);
  }


  @Override
  public Merge createMergePatch(JsonValue patch) {
    return new Merge(patch);
  }


  @Override
  public ObjectBuilder createObjectBuilder(JsonObject object) {
    return createBuilderFactory(null).createObjectBuilder(object);
  }


  @Override
  public ObjectBuilder createObjectBuilder(Map<String, ?> map) {
    @SuppressWarnings("unchecked")
    Map<String, Object> map2 = (Map<String, Object>) map;
    return createBuilderFactory(null).createObjectBuilder(map2);
  }


  @Override
  public ObjectBuilder createObjectBuilder() {
    return new BuilderFactory().createObjectBuilder();
  }


  @Override
  public CJParser createParser(Reader reader) {
    return new Parser(reader);
  }


  @Override
  public CJParser createParser(InputStream in) {
    return createParserFactory(null).createParser(in);
  }


  @Override
  public ParserFactory createParserFactory(Map<String, ?> config) {
    return new ParserFactory(config);
  }


  @Override
  public Patch createPatch(JsonArray array) {
    return new Patch(array);
  }


  @Override
  public PatchBuilder createPatchBuilder() {
    return createPatchBuilder(null);
  }


  @Override
  public PatchBuilder createPatchBuilder(JsonArray array) {
    return new PatchBuilder(array);
  }


  @Override
  public JsonExtendedPointer createPointer(String jsonPointer) {
    return PointerFactory.create(jsonPointer);
  }


  @Override
  public CJReader createReader(Reader reader) {
    return createReaderFactory(null).createReader(reader);
  }


  @Override
  public CJReader createReader(InputStream in) {
    return createReaderFactory(null).createReader(in);
  }


  @Override
  public ReaderFactory createReaderFactory(Map<String, ?> config) {
    return new ReaderFactory(config);
  }


  @Override
  public CJString createValue(String value) {
    return CJString.create(value);
  }


  @Override
  public CJNumber createValue(int value) {
    return CJNumber.create(value);
  }


  @Override
  public CJNumber createValue(long value) {
    return CJNumber.create(value);
  }


  @Override
  public CJNumber createValue(double value) {
    return CJNumber.cast(value);
  }


  @Override
  public CJNumber createValue(BigDecimal value) {
    return CJNumber.cast(value);
  }


  @Override
  public CJNumber createValue(BigInteger value) {
    return CJNumber.cast(value);
  }


  @Override
  public CJWriter createWriter(Writer writer) {
    return createWriterFactory(null).createWriter(writer);
  }


  @Override
  public CJWriter createWriter(OutputStream out) {
    return createWriterFactory(null).createWriter(out);
  }


  @Override
  public WriterFactory createWriterFactory(Map<String, ?> config) {
    return new WriterFactory(createGeneratorFactory(config));
  }

}
