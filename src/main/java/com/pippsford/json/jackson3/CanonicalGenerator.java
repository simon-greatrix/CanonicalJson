package com.pippsford.json.jackson3;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;

import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import com.pippsford.json.Canonical;
import com.pippsford.json.primitive.CJFalse;
import com.pippsford.json.primitive.CJJson;
import com.pippsford.json.primitive.CJNull;
import com.pippsford.json.primitive.CJTrue;
import com.pippsford.json.primitive.numbers.CJNumber;
import jakarta.json.JsonValue.ValueType;
import tools.jackson.core.Base64Variant;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.SerializableString;
import tools.jackson.core.StreamWriteCapability;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.TokenStreamContext;
import tools.jackson.core.Version;
import tools.jackson.core.base.GeneratorBase;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.io.IOContext;
import tools.jackson.core.json.DupDetector;
import tools.jackson.core.json.JsonWriteContext;
import tools.jackson.core.json.JsonWriteFeature;
import tools.jackson.core.util.JacksonFeatureSet;

/**
 * Generator for canonical JSON. Note that as the canonical form requires a specific ordering of object properties, no output is created until the root object
 * is complete.
 *
 * @author Simon Greatrix on 16/09/2019.
 */
public class CanonicalGenerator extends GeneratorBase {

  private static final int DEFAULT_STREAM_FEATURES = StreamWriteFeature.AUTO_CLOSE_TARGET.getMask()
      + StreamWriteFeature.AUTO_CLOSE_CONTENT.getMask()
      + StreamWriteFeature.FLUSH_PASSED_TO_STREAM.getMask();

  private static final int DISALLOWED_FEATURES = JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.getMask()
      + StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN.getMask()
      + JsonWriteFeature.ESCAPE_NON_ASCII.getMask();

  private static final int REQUIRED_FEATURES = JsonWriteFeature.QUOTE_PROPERTY_NAMES.getMask()
      + JsonWriteFeature.WRITE_NAN_AS_STRINGS.getMask();

  private static final int DEFAULT_FEATURE_MASK = StreamWriteFeature.AUTO_CLOSE_TARGET.getMask()
      + StreamWriteFeature.AUTO_CLOSE_CONTENT.getMask()
      + StreamWriteFeature.FLUSH_PASSED_TO_STREAM.getMask()
      + REQUIRED_FEATURES;



  interface Container {

    void add(String key, Canonical value);


    void set(JsonWriteContext parent, Canonical raw);


    /**
     * Write the container.
     *
     * @param writer the writer
     */
    void writeTo(Writer writer) throws IOException;

  }



  static class ArrayContainer implements Container {

    final CJArray array = new CJArray();


    @Override
    public void add(String key, Canonical value) {
      array.add(value);
    }


    @Override
    public void set(JsonWriteContext parent, Canonical raw) {
      array.set(parent.getCurrentIndex(), raw);
    }


    @Override
    public void writeTo(Writer writer) throws IOException {
      array.writeTo(writer);
    }

  }



  static class ObjectContainer implements Container {

    final CJObject object = new CJObject();


    @Override
    public void add(String key, Canonical value) {
      object.put(key, value);
    }


    @Override
    public void set(JsonWriteContext parent, Canonical raw) {
      object.put(parent.currentName(), raw);
    }


    @Override
    public void writeTo(Writer writer) throws IOException {
      object.writeTo(writer);
    }

  }



  static class RawContainer implements Container {

    private final String raw;


    RawContainer(String raw) {
      this.raw = raw;
    }


    @Override
    public void add(String key, Canonical value) {
      throw new UnsupportedOperationException("Raw containers cannot be added to");
    }


    @Override
    public void set(JsonWriteContext parent, Canonical raw) {
      throw new UnsupportedOperationException("Raw containers cannot be reset.");
    }


    @Override
    public void writeTo(Writer writer) throws IOException {
      writer.write(raw);
    }

  }



  private final boolean isResourceManaged;

  private final LinkedList<Container> stack = new LinkedList<>();

  private final Writer writer;

  private JsonWriteContext writeContext;


  /**
   * New instance.
   *
   * @param writeCtxt the writer context
   * @param ioCtxt    the I/O context
   * @param writer    the output's writer
   */
  public CanonicalGenerator(ObjectWriteContext writeCtxt, IOContext ioCtxt, Writer writer) {
    super(writeCtxt, ioCtxt, writeCtxt.getStreamWriteFeatures(DEFAULT_STREAM_FEATURES));
    isResourceManaged = ioCtxt.isResourceManaged();
    this.writer = writer;

    // TODO check features

    DupDetector detector = isEnabled(StreamWriteFeature.STRICT_DUPLICATE_DETECTION)
        ? DupDetector.rootDetector(this) : null;
    writeContext = JsonWriteContext.createRootContext(detector);
  }


  @Override
  protected void _closeInput() {
    if (isEnabled(StreamWriteFeature.AUTO_CLOSE_CONTENT)) {
      while (true) {
        TokenStreamContext context = streamWriteContext();
        if (context.inArray()) {
          writeEndArray();
        } else if (context.inObject()) {
          writeEndObject();
        } else {
          break;
        }
      }
    }

    if (isResourceManaged || isEnabled(StreamWriteFeature.AUTO_CLOSE_TARGET)) {
      try {
        writer.close();
      } catch (IOException ioe) {
        throw JacksonIOException.construct(ioe);
      }
    } else if (isEnabled(StreamWriteFeature.FLUSH_PASSED_TO_STREAM)) {
      try {
        writer.flush();
      } catch (IOException ioe) {
        throw JacksonIOException.construct(ioe);
      }
    }
  }


  @Override
  protected void _releaseBuffers() {
    // No buffers used
  }


  protected void _verifyValueWrite(String typeMsg) {
    final int status = writeContext.writeValue();
    switch (status) {
      case JsonWriteContext.STATUS_OK_AFTER_SPACE: // root-value separator
        try {
          writer.write(' ');
        } catch (IOException ioe) {
          throw JacksonIOException.construct(ioe);
        }
        break;
      case JsonWriteContext.STATUS_EXPECT_NAME:
        _reportError(String.format(
            "Can not %s, expecting field name (context: %s)",
            typeMsg, writeContext.typeDesc()
        ));
        break;
      default:
        break;
    }
  }


  @Override
  public void assignCurrentValue(Object v) {
    streamWriteContext().assignCurrentValue(v);
  }


  @Override
  public Object currentValue() {
    return streamWriteContext().currentValue();
  }


  @Override
  public void flush() {
    if (isEnabled(StreamWriteFeature.FLUSH_PASSED_TO_STREAM)) {
      try {
        writer.flush();
      } catch (IOException ioe) {
        throw JacksonIOException.construct(ioe);
      }
    }
  }


  private UnsupportedOperationException rawNotSupported() {
    return new UnsupportedOperationException("Canonical JSON does not support raw content");
  }


  @Override
  public JacksonFeatureSet<StreamWriteCapability> streamWriteCapabilities() {
    // TODO
    return null;
  }


  @Override
  public TokenStreamContext streamWriteContext() {
    return writeContext;
  }


  @Override
  public int streamWriteOutputBuffered() {
    return -1;
  }


  @Override
  public Object streamWriteOutputTarget() {
    return writer;
  }


  @Override
  public Version version() {
    return JsonModule.LIBRARY_VERSION;
  }


  @Override
  public CanonicalGenerator writeBinary(Base64Variant bv, byte[] data, int offset, int len) {
    writeBinary(bv, new ByteArrayInputStream(data, offset, len), len);
    return this;
  }


  @Override
  // Allow labels, and ignore cognitive complexity. Code is copied from Jackson and refactoring to address these issues is a higher risk than leaving them be.
  @SuppressWarnings({"java:S1119", "java:S3776", "CyclomaticComplexity", "NPathComplexity"})
  public int writeBinary(Base64Variant bv, InputStream data, int dataLength) {
    // Jackson's Base64Variant class forces callers to do most of the encoding themselves. This code is copied from the Jackson implementation.
    int length = 0;
    StringBuilder buffer = new StringBuilder();
    if (dataLength > 0) {
      // Add 3/8 overhead as a convenient estimate of 1/3
      buffer.ensureCapacity(dataLength + (dataLength >>> 2) + (dataLength >>> 3));
    }

    final int chunksBeforeLF = bv.getMaxLineLength() >> 2;
    int chunksLeft = chunksBeforeLF;
    int bits24;
    int extraBytes;

    encodingLoop:
    while (true) {
      // attempt to read 3 bytes
      bits24 = 0;
      for (int i = 2; i >= 0; i--) {
        int r;
        try {
          r = data.read();
        } catch (IOException e) {
          throw JacksonIOException.construct(e);
        }
        if (r == -1) {
          // We have read all the bytes, so we just need to note how much padding we need and break out of the encoding loop.
          extraBytes = 2 - i;
          break encodingLoop;
        }
        length++;
        bits24 |= r << (i << 3);
      }

      if (dataLength >= 0 && length > dataLength) {
        throw _constructWriteException("Data length exceeded");
      }
      bv.encodeBase64Chunk(buffer, bits24);

      chunksLeft--;
      if (chunksLeft <= 0) {
        // This is incorrect, but consistent with Jackson's handling. It is incorrect because (a) the line breaks should be CR+LF, and (b) encodings that
        // should not have line breaks get them if the data exceeds Integer.MAX_VALUE characters.
        buffer.append('\n');
        chunksLeft = chunksBeforeLF;
      }
    }

    if (dataLength >= 0 && length > dataLength) {
      throw _constructWriteException("Data length exceeded");
    }
    if (extraBytes > 0) {
      bv.encodeBase64Partial(buffer, bits24, extraBytes);
    }

    writeCanonical(Canonical.create(buffer.toString()));
    return length;
  }


  @Override
  public CanonicalGenerator writeBoolean(boolean state) {
    return writeCanonical(state ? CJTrue.TRUE : CJFalse.FALSE);
  }


  private CanonicalGenerator writeCanonical(Canonical canonical) {
    ValueType valueType = canonical.getValueType();
    String typeMessage = valueType == null ? "RAW" : valueType.name();
    _verifyValueWrite("write " + typeMessage);
    if (stack.isEmpty()) {
      try {
        canonical.writeTo(writer);
      } catch (IOException e) {
        throw JacksonIOException.construct(e);
      }
      return this;
    }
    Container container = stack.peek();
    container.add(writeContext.currentName(), canonical);
    return this;
  }


  @Override
  public CanonicalGenerator writeEndArray() {
    if (!writeContext.inArray()) {
      _reportError("Current context not Array but " + writeContext.typeDesc());
    }
    writeContext = writeContext.clearAndGetParent();
    Container c = stack.pop();
    if (stack.isEmpty()) {
      try {
        c.writeTo(writer);
      } catch (IOException e) {
        throw JacksonIOException.construct(e);
      }
    }
    return this;
  }


  @Override
  public CanonicalGenerator writeEndObject() {
    if (!writeContext.inObject()) {
      _reportError("Current context not Object but " + writeContext.typeDesc());
    }
    writeContext = writeContext.clearAndGetParent();
    Container c = stack.pop();
    if (stack.isEmpty()) {
      try {
        c.writeTo(writer);
      } catch (IOException e) {
        throw JacksonIOException.construct(e);
      }
    }
    return this;
  }


  @Override
  public CanonicalGenerator writeName(String name) {
    int status = writeContext.writeName(name);
    if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
      _reportError("Can not write a field name, expecting a value");
    }
    return this;
  }


  @Override
  public CanonicalGenerator writeName(SerializableString name) {
    return writeName(name.getValue());
  }


  @Override
  public CanonicalGenerator writeNull() {
    return writeCanonical(CJNull.NULL);
  }


  @Override
  public JsonGenerator writeNumber(short v) throws JacksonException {
    return writeCanonical(CJNumber.create(v));
  }


  @Override
  public CanonicalGenerator writeNumber(int v) {
    return writeCanonical(CJNumber.create(v));
  }


  @Override
  public CanonicalGenerator writeNumber(long v) {
    return writeCanonical(CJNumber.create(v));
  }


  @Override
  public CanonicalGenerator writeNumber(BigInteger v) {
    return writeCanonical(CJNumber.cast(v));
  }


  @Override
  public CanonicalGenerator writeNumber(double v) {
    return writeCanonical(CJNumber.castUnsafe(v));
  }


  @Override
  public CanonicalGenerator writeNumber(float v) {
    return writeCanonical(CJNumber.castUnsafe(v));
  }


  @Override
  public CanonicalGenerator writeNumber(BigDecimal v) {
    return writeCanonical(CJNumber.cast(v));
  }


  @Override
  public CanonicalGenerator writeNumber(String encodedValue) {
    // In keeping with this method's contract, we actually output a String
    return writeCanonical(Canonical.create(encodedValue));
  }


  @Override
  public JsonGenerator writePropertyId(long id) throws JacksonException {
    return null;
  }


  @Override
  public CanonicalGenerator writeRaw(String text) {
    throw rawNotSupported();
  }


  @Override
  public CanonicalGenerator writeRaw(String text, int offset, int len) {
    throw rawNotSupported();
  }


  @Override
  public CanonicalGenerator writeRaw(char[] text, int offset, int len) {
    throw rawNotSupported();
  }


  @Override
  public CanonicalGenerator writeRaw(char c) {
    throw rawNotSupported();
  }


  /**
   * Write a Json Value which is being processed as a type. This means the start and end markers are being written by the Jackson type processor.
   *
   * @param object      the value to write
   * @param isContainer is the value a container? i.e. does it have start and end markers?
   *
   * @throws IOException if the write fails
   */
  public void writeRawCanonicalType(Canonical object, boolean isContainer) {
    String json = Canonical.toCanonicalString(object);
    Canonical raw = new CJJson(json);

    if (isContainer) {
      // The caller has already pushed the start marker, creating the container. We pop the new container off the stack and discard it.
      RawContainer rawContainer = new RawContainer(json);
      stack.pop();
      if (!stack.isEmpty()) {
        // have to replace the link to the new container in the parent with the raw JSON
        Container parent = stack.peek();
        parent.set(writeContext.getParent(), raw);
      }
      stack.push(rawContainer);

      return;
    }

    // Not a container, so no markers to handle
    writeCanonical(raw);
  }


  /**
   * Write a Json Value as a value.
   *
   * @param object the value
   */
  public void writeRawCanonicalValue(Canonical object) {
    writeCanonical(new CJJson(Canonical.toCanonicalString(object)));
  }


  @Override
  public CanonicalGenerator writeRawUTF8String(byte[] text, int offset, int length) {
    throw rawNotSupported();
  }


  @Override
  public CanonicalGenerator writeRawValue(String text) {
    throw rawNotSupported();
  }


  @Override
  public CanonicalGenerator writeRawValue(String text, int offset, int len) {
    throw rawNotSupported();
  }


  @Override
  public CanonicalGenerator writeRawValue(char[] text, int offset, int len) {
    throw rawNotSupported();
  }


  @Override
  public CanonicalGenerator writeStartArray(Object forValue) {
    _verifyValueWrite("start an array");

    ArrayContainer arrayContainer = new ArrayContainer();
    if (!stack.isEmpty()) {
      Container container = stack.peek();
      container.add(writeContext.currentName(), arrayContainer.array);
    }

    writeContext = writeContext.createChildArrayContext(forValue);
    stack.push(arrayContainer);
    return this;
  }


  @Override
  public JsonGenerator writeStartArray() throws JacksonException {
    return writeStartArray(null);
  }


  @Override
  public CanonicalGenerator writeStartObject(Object forValue) {
    _verifyValueWrite("start an object");

    ObjectContainer objectContainer = new ObjectContainer();
    if (!stack.isEmpty()) {
      Container container = stack.peek();
      container.add(writeContext.currentName(), objectContainer.object);
    }

    writeContext = writeContext.createChildObjectContext(forValue);
    stack.push(objectContainer);
    return this;
  }


  @Override
  public JsonGenerator writeStartObject() throws JacksonException {
    return writeStartObject(null);
  }


  @Override
  public CanonicalGenerator writeString(String text) {
    return writeCanonical(Canonical.create(text));
  }


  @Override
  public CanonicalGenerator writeString(char[] text, int offset, int len) {
    return writeCanonical(Canonical.create(new String(text, offset, len)));
  }


  @Override
  public CanonicalGenerator writeUTF8String(byte[] text, int offset, int length) {
    return writeCanonical(Canonical.create(new String(text, offset, length, UTF_8)));
  }

}
