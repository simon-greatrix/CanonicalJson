package com.pippsford.json.jackson3;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

import com.pippsford.json.io.Utf8Writer;
import tools.jackson.core.JsonEncoding;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.io.IOContext;
import tools.jackson.core.json.JsonFactory;

/**
 * A Jackson JSON output factory that writes canonical JSON.
 */
public class CanonicalFactory extends JsonFactory {

  /** New instance using standard settings. */
  public CanonicalFactory() {
    // as super-class
  }


  /**
   * New instance using the provided code.
   *
   * @param factory the base factory
   */
  public CanonicalFactory(JsonFactory factory) {
    super(factory);
  }


  @Override
  protected JsonGenerator _createGenerator(
      ObjectWriteContext writeCtxt,
      IOContext ioCtxt,
      Writer out
  ) {
    return new CanonicalGenerator(writeCtxt, ioCtxt, out);
  }


  @Override
  protected JsonGenerator _createUTF8Generator(
      ObjectWriteContext writeCtxt,
      IOContext ioCtxt,
      OutputStream out
  ) {
    return new CanonicalGenerator(writeCtxt, ioCtxt, new Utf8Writer(out));
  }


  /**
   * Just like regular JSON, canonical JSON cannot handle binary natively.
   *
   * @return false
   */
  @Override
  public boolean canHandleBinaryNatively() {
    return false;
  }


  @Override
  public JsonGenerator createGenerator(ObjectWriteContext writeCtxt, OutputStream out, JsonEncoding enc) {
    if (enc != JsonEncoding.UTF8) {
      throw new UnsupportedOperationException("Canonical encoding must be UTF-8, not " + enc);
    }

    return super.createGenerator(writeCtxt, out, enc);
  }


  @Override
  public JsonGenerator createGenerator(ObjectWriteContext writeCtxt, File f, JsonEncoding enc) {
    if (enc != JsonEncoding.UTF8) {
      throw new UnsupportedOperationException("Canonical encoding must be UTF-8, not " + enc);
    }
    return super.createGenerator(writeCtxt, f, enc);
  }


  @Override
  public JsonGenerator createGenerator(ObjectWriteContext writeCtxt, Path p, JsonEncoding enc) {
    if (enc != JsonEncoding.UTF8) {
      throw new UnsupportedOperationException("Canonical encoding must be UTF-8, not " + enc);
    }
    return super.createGenerator(writeCtxt, p, enc);
  }


  @Override
  public String getFormatName() {
    return FORMAT_NAME_JSON;
  }


  /**
   * Unlike regular JSON, canonical JSON requires a fixed ordering.
   *
   * @return true
   */
  @Override
  public boolean requiresPropertyOrdering() {
    return true;
  }

}
