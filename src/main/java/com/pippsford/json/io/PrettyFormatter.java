package com.pippsford.json.io;

import java.io.IOException;

import com.pippsford.json.exception.JsonIOException;
import com.pippsford.json.io.PrettyOutput.Special;
import com.pippsford.json.primitive.CJBase;
import com.pippsford.json.primitive.CJString;

/**
 * A JSON formatter which does pretty printing.
 *
 * @author Simon Greatrix on 18/11/2020.
 */
class PrettyFormatter implements Formatter, Appendable {

  /** Pretty output destination. */
  private PrettyOutput prettyOutput;


  /**
   * Create a new instance.
   *
   * @param appendable          the appendable to write to
   * @param smallStructureLimit the limit at which small structures are expanded
   */
  public PrettyFormatter(Appendable appendable, int smallStructureLimit) {
    prettyOutput = new AppendableOutput(appendable, smallStructureLimit);
  }


  @Override
  public PrettyFormatter append(CharSequence csq) {
    prettyOutput = prettyOutput.append(csq);
    return this;
  }


  @Override
  public PrettyFormatter append(CharSequence csq, int start, int end) {
    prettyOutput = prettyOutput.append(csq, start, end);
    return this;
  }


  @Override
  public PrettyFormatter append(char c) {
    prettyOutput = prettyOutput.append(c);
    return this;
  }


  @Override
  public void close() {
    prettyOutput.close();
  }


  @Override
  public void flush() {
    prettyOutput = prettyOutput.flush();
  }


  @Override
  public void write(CJBase value) {
    try {
      value.writeTo(this);
    } catch (IOException exception) {
      throw new JsonIOException(exception);
    }
  }


  @Override
  public void writeArrayEnd() {
    prettyOutput = prettyOutput.append(Special.END_ARRAY);
  }


  @Override
  public void writeArrayStart() {
    prettyOutput = prettyOutput.append(Special.START_ARRAY);
  }


  @Override
  public void writeColon() {
    prettyOutput = prettyOutput.append(": ");
  }


  @Override
  public void writeComma() {
    prettyOutput = prettyOutput.append(Special.SEPARATOR);
  }


  @Override
  public void writeKey(String key) {
    try {
      CJString.format(this, key);
    } catch (IOException exception) {
      // As 'this' does not throw any IOExceptions, this should never happen
      throw new InternalError("Impossible I/O Exception", exception);
    }
  }


  @Override
  public void writeObjectEnd() {
    prettyOutput = prettyOutput.append(Special.END_OBJECT);
  }


  @Override
  public void writeObjectStart() {
    prettyOutput = prettyOutput.append(Special.START_OBJECT);
  }

}
