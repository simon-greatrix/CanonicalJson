package com.pippsford.json.io;

import jakarta.json.stream.JsonGenerationException;

import com.pippsford.json.Canonical;
import com.pippsford.json.builder.ArrayBuilder;
import com.pippsford.json.builder.ObjectBuilder;
import com.pippsford.json.primitive.CJNull;

/**
 * The in memory generator assembles the structure in memory.
 *
 * @author Simon Greatrix on 27/01/2020.
 */
public abstract class InMemoryGenerator<GeneratorType extends InMemoryGenerator<GeneratorType>> implements Generator<GeneratorType> {

  /** The current JSON generation context. */
  protected interface Context {

    /**
     * Get the parent context.
     *
     * @return the parent context (null for the root)
     */
    Context getParent();


    /**
     * Write the specified value.
     *
     * @param canonical the value to write
     */
    void write(Canonical canonical);


    /**
     * Write the end of the current structure.
     *
     * @return the parent context.
     */
    Context writeEnd();


    /**
     * Write the key for the current value. Must be in an object.
     *
     * @param key the key
     */
    void writeKey(String key);

  }



  /** Context for building an array. */
  protected static class ArrayContext implements Context {

    private final ArrayBuilder builder = new ArrayBuilder();

    private final Context parent;


    ArrayContext(Context parent) {
      this.parent = parent;
    }


    public Context getParent() {
      return parent;
    }


    @Override
    public void write(Canonical canonical) {
      builder.add(canonical);
    }


    @Override
    public Context writeEnd() {
      parent.write(builder.build());
      return parent;
    }


    @Override
    public void writeKey(String key) {
      throw new JsonGenerationException("Cannot write key in array context");
    }

  }



  /**
   * Context for building an object.
   */
  protected static class ObjectContext implements Context {

    private final ObjectBuilder builder = new ObjectBuilder();

    private final Context parent;

    private String key;


    ObjectContext(Context parent) {
      this.parent = parent;
    }


    @Override
    public Context getParent() {
      return parent;
    }


    @Override
    public void write(Canonical canonical) {
      if (key == null) {
        throw new JsonGenerationException("Cannot write value in object context without key");
      }
      builder.add(key, canonical);
      key = null;
    }


    @Override
    public Context writeEnd() {
      if (key != null) {
        throw new JsonGenerationException("Cannot end object when a key has an unwritten value");
      }
      parent.write(builder.build());
      return parent;
    }


    @Override
    public void writeKey(String key) {
      if (this.key == null) {
        this.key = key;
        return;
      }
      throw new JsonGenerationException("Cannot write key twice in object context");
    }

  }



  /**
   * Context for the root, outside any structure.
   */
  protected static class RootContext implements Context {

    /** The output. */
    protected Canonical output = null;


    /** New instance. */
    protected RootContext() {
      // do nothing
    }


    @Override
    public Context getParent() {
      return null;
    }


    @Override
    public void write(Canonical canonical) {
      if (output != null) {
        throw new JsonGenerationException("Cannot write multiple values to root context");
      }
      output = canonical;
    }


    public Context writeEnd() {
      throw new JsonGenerationException("Cannot write end in root context");
    }


    @Override
    public void writeKey(String key) {
      throw new JsonGenerationException("Cannot write key in root context");
    }

  }



  /**
   * The root context.
   */
  protected final RootContext root = new RootContext();

  /** The current context. */
  protected Context context = root;


  /** New instance. */
  public InMemoryGenerator() {
    // do nothing
  }


  @Override
  public void close() {
    if (!(context instanceof RootContext)) {
      throw new JsonGenerationException("Close attempted with unfinished structures");
    }
    closeWith(((RootContext) context).output);
    ((RootContext) context).output = null;
  }


  /**
   * Close the generator, which has generated the specified value.
   *
   * @param canonical the value generated
   */
  protected abstract void closeWith(Canonical canonical);


  /**
   * Test if the current context is the root.
   *
   * @return true if in the root context
   */
  protected boolean isInRoot() {
    return context.getParent() == null;
  }


  /**
   * Get this instance as the correct type.
   *
   * @return this as the correct type
   */
  @SuppressWarnings("unchecked")
  protected GeneratorType me() {
    return (GeneratorType) this;
  }


  @Override
  public GeneratorType write(Canonical value) {
    if (value == null) {
      value = CJNull.NULL;
    }
    context.write(value);
    return me();
  }


  @Override
  public GeneratorType writeEnd() {
    context = context.writeEnd();
    return me();
  }


  @Override
  public GeneratorType writeKey(String name) {
    context.writeKey(name);
    return me();
  }


  @Override
  public GeneratorType writeStartArray() {
    context = new ArrayContext(context);
    return me();
  }


  @Override
  public GeneratorType writeStartObject() {
    context = new ObjectContext(context);
    return me();
  }

}
