package com.pippsford.json.patch.ops;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;

import jakarta.annotation.Nonnull;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import com.pippsford.json.builder.ObjectBuilder;
import com.pippsford.json.exception.IncorrectDigestException;
import com.pippsford.json.pointer.JsonExtendedPointer;

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

  private final JsonObject digest;

  private final byte[] expected;


  TestDigest(String path, String algorithm, String value) {
    super(path);
    this.algorithm = Objects.requireNonNull(algorithm, "Algorithm must be specified");
    expected = Base64.getUrlDecoder().decode(Objects.requireNonNull(value, "Value must be specified"));
    digest = new ObjectBuilder().add("algorithm", algorithm).add("value", value).build();
  }


  TestDigest(String path, String algorithm, byte[] value) {
    super(path);
    this.algorithm = Objects.requireNonNull(algorithm, "Algorithm must be specified");
    expected = Objects.requireNonNull(value, "Value must be specified");
    digest = new ObjectBuilder().add("algorithm", algorithm).add("value", Base64.getUrlEncoder().encodeToString(value)).build();
  }


  TestDigest(JsonExtendedPointer pointer, String algorithm, byte[] value) {
    super(pointer);
    this.algorithm = Objects.requireNonNull(algorithm, "Algorithm must be specified");
    expected = Objects.requireNonNull(value, "Value must be specified");
    digest = new ObjectBuilder().add("algorithm", algorithm).add("value", Base64.getUrlEncoder().encodeToString(value)).build();

  }


  @Override
  public <T extends JsonStructure> T apply(T target) {
    JsonValue jsonValue = pointer.getValue(target);
    byte[] actual = digest(algorithm, jsonValue);
    if (!MessageDigest.isEqual(expected, actual)) {
      throw new IncorrectDigestException("Test failed. Digest for \"" + getPath() + "\" is \"" + Base64.getUrlEncoder().encodeToString(actual) + "\".");
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
  public JsonValue getDigest() {
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
