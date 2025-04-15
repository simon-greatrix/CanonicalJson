package com.pippsford.json.io;

import jakarta.json.stream.JsonGenerationException;

/** Handle the special formatter required for empty structures. */
class PrettyEmptyStructure implements PrettyOutput {

  private final Special endsWith;

  private final boolean isSmall;

  private final PrettyOutput parent;

  private final char startsWith;


  PrettyEmptyStructure(PrettyOutput parent, boolean isSmall, char startsWith, Special endsWith) {
    this.parent = parent;
    this.isSmall = isSmall;
    this.startsWith = startsWith;
    this.endsWith = endsWith;
  }


  @Override
  public PrettyOutput append(CharSequence csq) {
    if (csq.length() == 0) {
      return this;
    }

    // Not an empty structure.
    return parent.append(startsWith).append('\u0002').append(csq);
  }


  @Override
  public PrettyOutput append(char[] csq, int start, int end) {
    if (start == end) {
      return this;
    }

    // Not an empty structure.
    return parent.append(startsWith).append('\u0002').append(csq, start, end);
  }


  @Override
  public PrettyOutput append(CharSequence csq, int start, int end) {
    if (start == end) {
      return this;
    }

    // Not an empty structure.
    return parent.append(startsWith).append('\u0002').append(csq, start, end);
  }


  @Override
  public PrettyOutput append(char c) {
    // Not an empty structure.
    return parent.append(startsWith).append('\u0002').append(c);
  }


  @Override
  public PrettyOutput append(Special special) {
    if (special != endsWith) {
      // Not an empty structure.
      return parent.append(startsWith).append('\u0002').append(special);
    }

    if (isSmall) {
      // Compact small structure.
      return parent.append(startsWith).append(special.symbol());
    }

    return parent.append(startsWith).append('\u0001').append(special.symbol());
  }


  @Override
  public void close() {
    throw new JsonGenerationException("Generation terminated within structure");
  }


  @Override
  public PrettyOutput flush() {
    // Abandon small structure.
    return parent.append(startsWith).append('\u0002').flush();
  }

}
