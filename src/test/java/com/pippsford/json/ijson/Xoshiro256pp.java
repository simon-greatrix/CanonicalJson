package com.pippsford.json.ijson;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Xoshiro256pp {

  // Example usage:
  public static void main(String[] args) {
    Xoshiro256pp rng = new Xoshiro256pp(new long[]{1L, 2L, 3L, 4L});
    for (int i = 0; i < 20; i++) {
      System.out.println(Long.toHexString(rng.next()));
    }
  }


  private static long rol64(long x, int k) {
    return (x << k) | (x >>> (64 - k));
  }


  private final long[] s = new long[4];

  private int jump = 0;


  public Xoshiro256pp(long[] seed) {
    if (seed.length != 4) {
      throw new IllegalArgumentException("Seed must have length 4");
    }
    System.arraycopy(seed, 0, s, 0, 4);
  }


  public long next() {
    jump--;
    if (jump < 0) {
      jump = 1000;
      byte[] buffer = new byte[32];
      LongBuffer longBuffer = ByteBuffer.wrap(buffer).asLongBuffer();
      longBuffer.put(s);
      MessageDigest digest = null;
      try {
        digest = MessageDigest.getInstance("SHA256");
      } catch (NoSuchAlgorithmException e) {
        throw new UnsupportedOperationException(e);
      }
      byte[] out = digest.digest(buffer);
      longBuffer = ByteBuffer.wrap(out).asLongBuffer();
      longBuffer.get(s);
    }

    long result = rol64(s[0] + s[3], 23) + s[0];
    long t = s[1] << 17;

    s[2] ^= s[0];
    s[3] ^= s[1];
    s[1] ^= s[2];
    s[0] ^= s[3];

    s[2] ^= t;
    s[3] = rol64(s[3], 45);

    return result;
  }

}
