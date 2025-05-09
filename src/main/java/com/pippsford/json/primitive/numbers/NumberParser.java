package com.pippsford.json.primitive.numbers;

import static com.pippsford.json.parser.Parser.isWhite;
import static com.pippsford.json.parser.Parser.safe;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.json.stream.JsonParsingException;

import com.pippsford.json.io.Input;
import com.pippsford.json.primitive.cache.CacheManager;
import com.pippsford.json.primitive.cache.ICache;

/**
 * Parser for JSON numeric values.
 *
 * @author Simon Greatrix on 13/01/2020.
 */
public class NumberParser {

  /** Integer wrapper types. These classes are final so cannot be sub-classed. */
  private static final Set<Class<?>> INTEGER_TYPES = Set.of(
      Byte.class, Short.class, Integer.class, Long.class);



  private enum Step {
    /** Processing the starting character. */
    START(false) {
      Step apply(int r, NumberParser p) {
        if (r == '-') {
          return LEADING_MINUS;
        }
        if (r == '0') {
          return LEADING_ZERO;
        }
        return INTEGER_PART;
      }
    },

    /** Seen leading minus sign, must be followed by digit. */
    LEADING_MINUS(false) {
      Step apply(int r, NumberParser p) {
        if (r == '0') {
          return LEADING_ZERO;
        }
        if (isDigit(r)) {
          return INTEGER_PART;
        }
        return ERROR;
      }
    },

    /** Parsing step failed. */
    ERROR(true) {
      Step apply(int r, NumberParser p) {
        throw new IllegalStateException("Step invoked after error");
      }
    },

    /**
     * Seen leading zero, must be followed by '.', 'e', or 'E'.
     * Note that JSON does not allow multiple redundant leading zeroes.
     */
    LEADING_ZERO(true) {
      Step apply(int r, NumberParser p) {
        if (r == '.') {
          // start fractional part
          return START_FRACTION;
        } else if (r == 'e' || r == 'E') {
          // start exponent part
          return START_EXPONENT;
        }
        return ERROR;
      }
    },

    /** Seen leading 1 to 9, can be followed by any digit, '.', 'e', or 'E'. */
    INTEGER_PART(true) {
      Step apply(int r, NumberParser p) {
        if (isDigit(r)) {
          return INTEGER_PART;
        }
        if (r == '.') {
          return START_FRACTION;
        }
        if (r == 'e' || r == 'E') {
          return START_EXPONENT;
        }
        return ERROR;
      }
    },

    /** Seen a '.', so read fractional part. */
    START_FRACTION(false) {
      Step apply(int r, NumberParser p) {
        p.needBigDecimal = true;
        if (isDigit(r)) {
          return FRACTION_PART;
        }
        return ERROR;
      }
    },

    /** Seen a '.' and a digit, now reading fractional part. */
    FRACTION_PART(true) {
      Step apply(int r, NumberParser p) {
        if (isDigit(r)) {
          return FRACTION_PART;
        }
        if (r == 'e' || r == 'E') {
          return START_EXPONENT;
        }
        return ERROR;
      }
    },

    /** seen 'e' or 'E', read exponent part. Can be '+', '-' or '0' to '9' */
    START_EXPONENT(false) {
      Step apply(int r, NumberParser p) {
        p.needBigDecimal = true;
        if (r == '+' || r == '-') {
          return SIGNED_EXPONENT;
        }
        if (isDigit(r)) {
          return EXPONENT_PART;
        }
        return ERROR;
      }
    },

    /** Seen "E+" or "E-". A digit is required. */
    SIGNED_EXPONENT(false) {
      Step apply(int r, NumberParser p) {
        return isDigit(r) ? EXPONENT_PART : ERROR;
      }
    },

    EXPONENT_PART(true) {
      Step apply(int r, NumberParser p) {
        return isDigit(r) ? EXPONENT_PART : ERROR;
      }
    };

    private final boolean terminal;


    Step(boolean terminal) {
      this.terminal = terminal;
    }


    /**
     * Apply the character and advance to the next step in parsing.
     *
     * @param r the character
     *
     * @return the next step
     */
    abstract Step apply(int r, NumberParser parser);


    /** Can parsing legally stop in this state?. */
    boolean isFinal() {
      return terminal;
    }
  }


  private static JsonParsingException badNumber(StringBuilder buf, int r, Input input) {
    return new JsonParsingException(
        "Invalid character in JSON number: \"" + buf.toString() + "\" was followed by " + safe(r),
        input.getLocation()
    );
  }


  /**
   * Create a JSON number from a string with an appropriate backing type.
   *
   * @param txt the string
   *
   * @return the JSON number
   */
  protected static CJNumber doCreate(String txt) {
    int length = txt.length();

    // Integer.MAX_VALUE takes 10 characters
    if (length < 10) {
      return CJNumber.create(Integer.parseInt(txt));
    }

    // Integer.MIN_VALUE takes 11 characters, so this one could be a long or an int.
    if (length < 12) {
      return doCreateLargeInt(txt);
    }

    // Long.MAX_VALUE takes 19 characters
    if (length < 19) {
      return new CJLong(Long.parseLong(txt));
    }

    // Long.MIN_VALUE takes 20 characters
    if (length < 21) {
      return doCreateLargeLong(txt);
    }

    // Could be a big integer, unless it has too many trailing zeros.
    int s = 0;
    for (int i = txt.length() - 1; i >= 0; i--) {
      if (txt.charAt(i) == '0') {
        s--;
      } else {
        break;
      }
    }

    if (s < CJBigInteger.MIN_SCALE) {
      return new CJBigDecimal(new BigDecimal(txt));
    }

    // It is a BigInteger after all
    return new CJBigInteger(new BigInteger(txt));
  }


  /**
   * Create a JSON number from a string.
   *
   * @param txt the string
   *
   * @return the JSON number
   */
  protected static CJNumber doCreateBigDecimal(String txt) {
    return CJNumber.cast(new BigDecimal(txt));
  }


  /** Create a JSON number from a string with 10 or 11 characters and hence could be a long or a large int. */
  private static CJNumber doCreateLargeInt(String txt) {
    long l = Long.parseLong(txt);
    if (Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE) {
      return CJNumber.create((int) l);
    }
    return new CJLong(l);
  }


  /** Create a JSON number from a string with 19 to 21 characters and hence could be a long or a BigInteger. */
  private static CJNumber doCreateLargeLong(String txt) {
    BigInteger bi = new BigInteger(txt);
    if (bi.bitLength() <= 63) {
      return new CJLong(bi.longValueExact());
    }
    return new CJBigInteger(bi);
  }


  private static boolean isDigit(int r) {
    return '0' <= r && r <= '9';
  }


  private static boolean isEndChar(int r) {
    return r == ',' || r == ']' || r == '}';
  }


  /**
   * Test if the number's type is something whose value can be safely represented by a long, int, short or byte.
   *
   * @param n the number to test
   *
   * @return true if the number's type is an integer of 64 bits ore less.
   */
  public static boolean isPrimitiveIntegerType(Number n) {
    if (n == null) {
      return false;
    }
    if (INTEGER_TYPES.contains(n.getClass())) {
      return true;
    }
    return n instanceof AtomicInteger || n instanceof AtomicLong;
  }


  final Input input;

  boolean needBigDecimal;


  /**
   * New instance.
   *
   * @param input the input to read from
   */
  public NumberParser(Input input) {
    this.input = input;
  }


  /**
   * Does the character end the number?. The actual character is the first one that is not part of the number and will need to be unread for later processing.
   *
   * @param r the character
   *
   * @return true if no longer in the number
   */
  private boolean isEnd(int r, Step step) {
    if (r == -1 || isWhite(r) || isEndChar(r)) {
      input.unread(r);
      // Check for an invalid final state
      if (step.isFinal()) {
        return true;
      }

      // invalid final state
      throw new JsonParsingException("Incomplete JSON number", input.getLocation());
    }

    // not an ending character
    return false;
  }


  /**
   * Parse a number from the input.
   *
   * @param r the initial character of the number
   *
   * @return the parsed number
   *
   * @throws JsonParsingException if the input is invalid
   */
  public CJNumber parse(int r) {
    StringBuilder buf = new StringBuilder();
    buf.append((char) r);

    // read rest of number
    Step step = Step.START.apply(r, this);
    while (true) {
      r = input.read();
      if (isEnd(r, step)) {
        break;
      }
      buf.append((char) r);
      step = step.apply(r, this);
      if (step == Step.ERROR) {
        throw badNumber(buf, r, input);
      }
    }

    String txt = buf.toString();

    ICache<String, CJNumber> cache = CacheManager.numberCache();
    CJNumber pNumber;
    try {
      if (needBigDecimal) {
        pNumber = cache.get(txt, NumberParser::doCreateBigDecimal);
      } else {
        pNumber = cache.get(txt, NumberParser::doCreate);
      }
    } catch (NumberFormatException | ArithmeticException e) {
      pNumber = new BadNumber(new JsonParsingException("Invalid number", e, input.getLocation()));
    }
    pNumber.check();
    return pNumber;
  }

}
