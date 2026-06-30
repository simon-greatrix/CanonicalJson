package com.pippsford.json.primitive;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

/**
 * A comparator for object keys which sorts strings into code point order.
 */
public class CodePointOrder implements Comparator<String>, Serializable {

  /** A singleton instance of this comparator. */
  public static final CodePointOrder INSTANCE = new CodePointOrder();


  @Override
  public int compare(String s1, String s2) {
    int len1 = s1.length();
    int len2 = s2.length();
    int lim = Math.min(len1, len2);
    for (int i = 0; i < lim; i++) {
      int cp1 = s1.codePointAt(i);
      int cp2 = s2.codePointAt(i);
      if (cp1 != cp2) {
        return cp1 - cp2;
      }
      if (cp1 > 0xffff) {
        i++;
      }
    }
    return len1 - len2;
  }


  /**
   * Ensure the singleton is used on deserialization.
   *
   * @return the singleton
   */
  @Serial
  public Object readResolve() {
    return INSTANCE;
  }

}
