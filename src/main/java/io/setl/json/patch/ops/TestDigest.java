package io.setl.json.patch.ops;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;

import jakarta.annotation.Nonnull;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import io.setl.json.builder.ObjectBuilder;
import io.setl.json.exception.IncorrectDigestException;
import io.setl.json.pointer.JsonExtendedPointer;

class TestDigest extends Test {

  private static Object[] parseDigest(String digest) {
    int p = digest.indexOf('=');
    if (p == -1) {
      return new Object[]{
          DEFAULT_DIGEST,
          Base64.getUrlDecoder().decode(digest)
      };
    }
    return new Object[]{
        digest.substring(0, p),
        Base64.getUrlDecoder().decode(digest.substring(p + 1))
    };
  }


  private final String algorithm;

  private final String digest;

  private final byte[] expected;


  /**
   * New instance for a value comparison.
   *
   * @param pointer the path to test
   * @param digest  the digest the value must have
   */
  TestDigest(@Nonnull JsonExtendedPointer pointer, @Nonnull String digest) {
    super(pointer);
    this.digest = Objects.requireNonNull(digest, "Test digest must not be null");
    Object[] parsed = parseDigest(digest);
    algorithm = (String) parsed[0];
    expected = (byte[]) parsed[1];
  }


  /**
   * New instance for a value comparison.
   *
   * @param path   the path to test
   * @param digest the digest the value must have
   */
  TestDigest(@Nonnull String path, @Nonnull String digest) {
    super(path);
    this.digest = Objects.requireNonNull(digest, "Test digest must not be null");

    Object[] parsed = parseDigest(digest);
    algorithm = (String) parsed[0];
    expected = (byte[]) parsed[1];
  }


  @Override
  public <T extends JsonStructure> T apply(T target) {
    JsonValue jsonValue = pointer.getValue(target);
    byte[] actual = digest(algorithm, jsonValue);
    if (!MessageDigest.isEqual(expected, actual)) {
      throw new IncorrectDigestException("Test failed. Digest for " + getPath() + " is \"" + Base64.getUrlEncoder().encodeToString(actual) + "\".");
    }

    return target;
  }


  @Override
  protected Object getCriteria() {
    return digest;
  }


  /**
   * Get the expected digest of the value at the test path.
   *
   * @return the expected digest
   */
  public String getDigest() {
    return digest;
  }


  @Nonnull
  @Override
  public Type getType() {
    return Type.DIGEST;
  }


  @Override
  protected void toJsonObject(ObjectBuilder builder) {
    builder.add("digest", getDigest());
  }

}
