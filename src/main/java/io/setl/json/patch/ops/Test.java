package io.setl.json.patch.ops;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch.Operation;
import jakarta.json.JsonValue;

import io.setl.json.CJObject;
import io.setl.json.Canonical;
import io.setl.json.builder.ObjectBuilder;
import io.setl.json.patch.PatchOperation;
import io.setl.json.pointer.JsonExtendedPointer;
import io.setl.json.pointer.JsonExtendedPointer.ResultOfAdd;

/**
 * A "test" operation. The standard test is extended to allow verification of cryptographic digests.
 *
 * @author Simon Greatrix on 06/02/2020.
 */
@JsonInclude(Include.NON_NULL)
public abstract class Test extends PatchOperation {

  /** The default digest algorithm used to calculate digests of canonical JSON (SHA-512/256). */
  public static final String DEFAULT_DIGEST = "SHA-512/256";



  /** The type of test. */
  public enum Type {
    /** The test compares a value. */
    VALUE,
    /** The test compares a digest. */
    DIGEST,
    /** The test compares the result of an add operation. */
    RESULT
  }


  /**
   * New instance. Exactly one of <code>value</code>, <code>digest</code>, or <code>resultOfAdd</code> must be specified.
   *
   * @param path        the path to test
   * @param value       the value to check against
   * @param digest      the digest to check against
   * @param resultOfAdd the required result of an add operation to this
   *
   * @return the new test
   */
  @JsonCreator
  public static Test create(
      @JsonProperty("path") String path,
      @JsonProperty("value") JsonValue value,
      @JsonProperty("digest") String digest,
      @JsonProperty("resultOfAdd") ResultOfAdd resultOfAdd
  ) {
    int flags = 0;
    if (value != null) {
      flags |= 1;
    }
    if (digest != null) {
      flags |= 2;
    }
    if (resultOfAdd != null) {
      flags |= 4;
    }
    switch (flags) {
      case 1:
        return new TestValue(path, value);
      case 2:
        return new TestDigest(path, digest);
      case 4:
        return new TestResult(path, resultOfAdd);
      default:
        throw new IllegalArgumentException("Test case must specify exactly one of 'value', 'digest', or 'resultOfAdd'");
    }
  }


  /**
   * Create a new instance from its JSON representation.
   *
   * @param object representation of the test
   *
   * @return the new test
   */
  public static Test create(CJObject object) {
    String resultName = object.optString("resultOfAdd");
    ResultOfAdd result = (resultName == null) ? null : ResultOfAdd.valueOf(resultName);

    return create(
        object.getString("path"),
        object.optJsonValue("value"),
        object.optString("digest"),
        result
    );
  }


  /**
   * Calculate the digest of the canonical representation of a JsonValue, using the specified algorithm.
   *
   * @param algorithm the algorithm. If null or empty, the default algorithm is used.
   * @param jsonValue the value
   *
   * @return the digest
   *
   * @throws JsonException if the algorithm is invalid
   */
  public static byte[] digest(String algorithm, JsonValue jsonValue) {
    if (algorithm == null || algorithm.isEmpty()) {
      algorithm = DEFAULT_DIGEST;
    }
    MessageDigest hash;
    try {
      hash = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new JsonException("Invalid digest algorithm: \"" + algorithm + "\"", e);
    }

    String canonical = Canonical.toCanonicalString(Canonical.cast(jsonValue));
    return hash.digest(canonical.getBytes(UTF_8));
  }


  /**
   * Create a test that verifies the digest of the JSON structure indicated by the path. The digest is specified as
   *
   * @param path   the path
   * @param digest the digest
   *
   * @return the test
   */
  public static Test testDigest(String path, String digest) {
    return new TestDigest(path, digest);
  }


  /**
   * Create a test that verifies the digest of the JSON structure indicated by the pointer.
   *
   * @param pointer the pointer
   * @param digest  the digest
   *
   * @return the test
   */
  public static Test testDigest(JsonExtendedPointer pointer, String digest) {
    return new TestDigest(pointer, digest);
  }


  /**
   * Create a test that verifies result of an 'add' operation.
   *
   * @param path   the path
   * @param result the result
   *
   * @return the test
   */
  public static Test testResult(String path, ResultOfAdd result) {
    return new TestResult(path, result);
  }


  /**
   * Create a test that verifies result of an 'add' operation.
   *
   * @param pointer the pointer
   * @param result  the result
   *
   * @return the test
   */
  public static Test testResult(JsonExtendedPointer pointer, ResultOfAdd result) {
    return new TestResult(pointer, result);
  }


  /**
   * Create a test that verifies the value of the JSON structure indicated by the path. The digest is specified as
   *
   * @param path  the path
   * @param value the value
   *
   * @return the test
   */
  public static Test testValue(String path, JsonValue value) {
    return new TestValue(path, value);
  }


  /**
   * Create a test that verifies the value of the JSON structure indicated by the pointer.
   *
   * @param pointer the pointer
   * @param value   the value
   *
   * @return the test
   */
  public static Test testValue(JsonExtendedPointer pointer, JsonValue value) {
    return new TestValue(pointer, value);
  }


  protected Test(@Nonnull JsonExtendedPointer pointer) {
    super(pointer);
  }


  protected Test(@Nonnull String path) {
    super(path);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Test)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    Test test = (Test) o;
    return Objects.equals(getType(), test.getType()) && Objects.equals(getCriteria(), test.getCriteria());
  }


  /**
   * Get the criteria object for this test. The criteria is the value, digest, or resultOfAdd that the test compares to.
   *
   * @return the criteria object.
   */
  protected abstract Object getCriteria();


  /**
   * Get the expected digest of the value at the test path.
   *
   * @return the expected digest
   */
  public String getDigest() {
    return null;
  }


  @Override
  public Operation getOperation() {
    return Operation.TEST;
  }


  /**
   * Get the expected result of an "add" operation on the test path.
   *
   * @return the expected result
   */
  public ResultOfAdd getResultOfAdd() {
    return null;
  }


  /**
   * Get the type of this test.
   *
   * @return the test's type
   */
  @JsonIgnore
  @Nonnull
  public abstract Type getType();


  /**
   * Get the value to test against.
   *
   * @return the value
   */
  public JsonValue getValue() {
    return null;
  }


  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hash(getValue(), getDigest(), getResultOfAdd());
    return result;
  }


  protected abstract void toJsonObject(ObjectBuilder builder);


  @Override
  public CJObject toJsonObject() {
    ObjectBuilder builder = new ObjectBuilder()
        .add("op", getOperation().operationName())
        .add("path", getPath());

    toJsonObject(builder);

    return builder.build();
  }

}
