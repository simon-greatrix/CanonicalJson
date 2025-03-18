package io.setl.json.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

import io.setl.json.exception.JsonIOException;

/**
 * An equivalent to a regular Appendable which throws JsonIOException when an IOException would occur.
 *
 * @author Simon Greatrix on 20/11/2020.
 */
class AppendableOutput implements PrettyOutput {

  /** Output. */
  private final Appendable appendable;

  /**
   * Maximum size of a small structure, not including the end marker (which is two characters: whitespace and symbol).
   * There are two special values:
   * <ul>
   *   <li>0: Do not prettify small structures.</li>
   *   <li>1: Prettify empty structures only.</li>
   *   <li>2: Not used.</li>
   *   <li>3+: Prettify structures up to this size. For example, "[ 1 ]" has three character before the end whitespace and symbol.</li>
   * </ul>
   */
  private final int smallStructureLimit;

  /** Current level of indentation. */
  private int indent = 0;


  /**
   * Create a pretty output writing to the specified Appendable.
   *
   * @param appendable          the output destination
   * @param smallStructureLimit the maximum size for a small structure
   */
  public AppendableOutput(Appendable appendable, int smallStructureLimit) {
    this.appendable = appendable;
    // The smallest possible structure is "[]", which is 2 characters.
    // The smallest possible non-empty structure is "[ 1 ]", which is 5 characters.
    // Therefore, between 2 and 5 characters we are only prettifying empty structures.
    if (smallStructureLimit < 2) {
      // Cannot prettify anything
      this.smallStructureLimit = 0;
    } else if (smallStructureLimit < 5) {
      // Only empty structures are prettified.
      this.smallStructureLimit = 1;
    } else {
      // Track small structures.
      this.smallStructureLimit = smallStructureLimit - 2;
    }
  }


  @Override
  public PrettyOutput append(CharSequence csq) {
    try {
      appendable.append(csq);
    } catch (IOException exception) {
      throw new JsonIOException(exception);
    }
    return this;
  }


  @Override
  public PrettyOutput append(char[] csq, int start, int end) {
    for (int i = start; i < end; i++) {
      append(csq[i]);
    }
    return this;
  }


  @Override
  public PrettyOutput append(CharSequence csq, int start, int end) {
    try {
      appendable.append(csq, start, end);
    } catch (IOException exception) {
      throw new JsonIOException(exception);
    }
    return this;
  }


  @Override
  public PrettyOutput append(char c) {
    try {
      if (c < 3) {
        indent += (c - 1);
        appendable.append('\n');
        for (int i = 0; i < indent; i++) {
          appendable.append(' ').append(' ');
        }
      } else {
        appendable.append(c);
      }
      return this;
    } catch (IOException exception) {
      throw new JsonIOException(exception);
    }
  }


  @Override
  public PrettyOutput append(Special special) {
    PrettyBuffer buffer;
    switch (special) {
      case END_ARRAY:
        return append('\u0000').append(']');
      case END_OBJECT:
        return append('\u0000').append('}');
      case START_ARRAY:
        if (smallStructureLimit > 1) {
          buffer = new PrettyBuffer(this, smallStructureLimit, Special.END_ARRAY);
          return buffer.append('[').append('\u0002');
        } else {
          return new PrettyEmptyStructure(this, (smallStructureLimit == 1), '[', Special.END_ARRAY);
        }
      case START_OBJECT:
        if (smallStructureLimit > 1) {
          buffer = new PrettyBuffer(this, smallStructureLimit, Special.END_OBJECT);
          return buffer.append('{').append('\u0002');
        } else {
          return new PrettyEmptyStructure(this, (smallStructureLimit == 1), '{', Special.END_OBJECT);
        }
      default:
        // Must be a separator
        return append(',').append('\u0001');
    }
  }


  @Override
  public void close() {
    if (appendable instanceof Closeable) {
      try {
        ((Closeable) appendable).close();
      } catch (IOException exception) {
        throw new JsonIOException(exception);
      }
    }
  }


  @Override
  public PrettyOutput flush() {
    if (appendable instanceof Flushable) {
      try {
        ((Flushable) appendable).flush();
      } catch (IOException exception) {
        throw new JsonIOException(exception);
      }
    }
    return this;
  }

}
