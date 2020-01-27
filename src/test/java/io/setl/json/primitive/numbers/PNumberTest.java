package io.setl.json.primitive.numbers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

/**
 * @author Simon Greatrix on 26/01/2020.
 */
public class PNumberTest {

  @Test
  public void simplifyBigDecimal() {
    Number n = PNumber.simplify(BigDecimal.TEN);
    assertTrue( n instanceof Integer);
    assertEquals(10,n.intValue());

    BigDecimal bd1 = new BigDecimal("0.100");
    BigDecimal bd2 = new BigDecimal("0.1");
    n = PNumber.simplify(bd1);
    assertEquals(bd2,n);

    bd1 = new BigDecimal("1e+6");
    n = PNumber.simplify(bd1);
    assertEquals(Integer.valueOf(1000000),n);

    bd1 = BigDecimal.valueOf(Integer.MAX_VALUE);
    n = PNumber.simplify(bd1);
    assertTrue(n instanceof Integer);
    assertEquals(Integer.MAX_VALUE,n);

    bd1 = BigDecimal.valueOf(Integer.MIN_VALUE);
    n = PNumber.simplify(bd1);
    assertTrue(n instanceof Integer);
    assertEquals(Integer.MIN_VALUE,n);

    bd1 = new BigDecimal("1e+12");
    n = PNumber.simplify(bd1);
    assertEquals(Long.valueOf(1000000000000L),n);

    bd1 = BigDecimal.valueOf(Long.MAX_VALUE);
    n = PNumber.simplify(bd1);
    assertTrue(n instanceof Long);
    assertEquals(Long.MAX_VALUE,n);

    bd1 = bd1.add(BigDecimal.ONE);
    n = PNumber.simplify(bd1);
    assertTrue(n instanceof BigInteger);
    assertEquals(bd1.toBigIntegerExact(),n);

    bd1 = BigDecimal.valueOf(Long.MIN_VALUE);
    n = PNumber.simplify(bd1);
    assertTrue(n instanceof Long);
    assertEquals(Long.MIN_VALUE,n);

    bd1 = bd1.subtract(BigDecimal.ONE);
    n = PNumber.simplify(bd1);
    assertTrue(n instanceof BigInteger);
    assertEquals(bd1.toBigIntegerExact(),n);

    bd1 = new BigDecimal("1e+30");
    n = PNumber.simplify(bd1);
    assertEquals(bd1.toBigIntegerExact(),n);

    bd1 = new BigDecimal("1e+31");
    n = PNumber.simplify(bd1);
    assertEquals(bd1,n);
  }


  @Test
  public void simplifyBigInteger() {
    BigInteger bi1 = BigInteger.TEN;
    Number n = PNumber.simplify(bi1,true);
    assertEquals(Integer.valueOf(10),n);

    bi1 = BigInteger.valueOf(Integer.MIN_VALUE);
    n = PNumber.simplify(bi1,true);
    assertEquals(Integer.valueOf(Integer.MIN_VALUE),n);


    bi1 = BigInteger.valueOf(Long.MIN_VALUE);
    n = PNumber.simplify(bi1,true);
    assertEquals(Long.valueOf(Long.MIN_VALUE),n);

    BigDecimal bd1 = new BigDecimal("1e+30");
    n = PNumber.simplify(bd1.toBigIntegerExact(),true);
    assertEquals(bd1.toBigIntegerExact(),n);

    bd1 = new BigDecimal("1e+31");
    n = PNumber.simplify(bd1.toBigIntegerExact(),true);
    assertEquals(bd1,n);
  }


  @Test
  public void simplifyDouble() {
    assertEquals(Integer.valueOf(100), PNumber.simplify(100.0));

    assertEquals(Long.valueOf(Long.MIN_VALUE), PNumber.simplify((double) Long.MIN_VALUE));
    assertEquals(Long.valueOf(Long.MAX_VALUE), PNumber.simplify((double) Long.MAX_VALUE));

    long l = Long.MAX_VALUE - (long) Math.ulp((double) Long.MAX_VALUE);
    assertEquals(Long.valueOf((long) ((double) l)), PNumber.simplify((double) l));

    BigDecimal bd = new BigDecimal("1e+30");
    Number n = PNumber.simplify(1e+30d);
    assertEquals(bd.toBigIntegerExact(),n);

    bd = new BigDecimal("1e+31");
    n = PNumber.simplify(1e+31d);
    assertEquals(bd,n);
  }


  @Test
  public void simplifyFloat() {
    assertEquals(Integer.valueOf(100), PNumber.simplify(100.0f));

    long l = Long.MAX_VALUE - (long) Math.ulp((float) Long.MAX_VALUE);
    assertEquals(Long.valueOf((long) ((float) l)), PNumber.simplify((float) l));

    BigDecimal bd = new BigDecimal("1e+30");
    Number n = PNumber.simplify(1e+30f);
    assertEquals(bd.toBigIntegerExact(),n);

    bd = new BigDecimal("1e+31");
    n = PNumber.simplify(1e+31f);
    assertEquals(bd,n);
  }

  @Test
  public void equals() {
    PNumber pn = PNumber.create(1);
    PNumber pc = PNumber.create(1);
    assertTrue(pn.equals(pc));

    pc = PNumber.create(12345678901234L);
    assertFalse(pn.equals(pc));

    pc = PNumber.create(1234.5678);
    assertFalse(pn.equals(pc));


    pc = PNumber.create(BigInteger.ONE.shiftLeft(64));
    assertFalse(pn.equals(pc));

  }
}