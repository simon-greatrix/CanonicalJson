package com.pippsford.json.primitive;

import java.io.IOException;
import jakarta.json.JsonValue;

/**
 * Representation of "false".
 *
 * @author Simon Greatrix on 08/01/2020.
 */
public class CJFalse extends CJBase implements CJBoolean {

  /** The singleton instance. */
  public static final CJFalse FALSE = new CJFalse();


  private CJFalse() {
    // do nothing
  }


  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public boolean equals(Object obj) {
    return JsonValue.FALSE.equals(obj);
  }


  @Override
  public Boolean getValue() {
    return Boolean.FALSE;
  }


  @Override
  public ValueType getValueType() {
    return ValueType.FALSE;
  }


  @Override
  public int hashCode() {
    return JsonValue.FALSE.hashCode();
  }


  @Override
  public String toString() {
    return "false";
  }


  @Override
  public void writeTo(Appendable writer) throws IOException {
    writer.append("false");
  }

}
