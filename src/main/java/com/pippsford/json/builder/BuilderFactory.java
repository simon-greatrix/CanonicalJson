package com.pippsford.json.builder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import com.pippsford.json.Canonical;

/**
 * Factory for creating array and object builders.
 *
 * @author Simon Greatrix on 10/01/2020.
 */
public class BuilderFactory implements JsonBuilderFactory {

  /** New instance. */
  public BuilderFactory() {
    // nothing to do
  }


  @Override
  public ArrayBuilder createArrayBuilder() {
    return new ArrayBuilder();
  }


  @Override
  public ArrayBuilder createArrayBuilder(JsonArray array) {
    ArrayBuilder builder = new ArrayBuilder();
    array.forEach(o -> builder.add(Canonical.cast(o).copy()));
    return builder;
  }


  @Override
  public ArrayBuilder createArrayBuilder(Collection<?> collection) {
    ArrayBuilder builder = new ArrayBuilder();
    collection.forEach(o -> builder.add(Canonical.create(o).copy()));
    return builder;
  }


  @Override
  public ObjectBuilder createObjectBuilder() {
    return new ObjectBuilder();
  }


  @Override
  public ObjectBuilder createObjectBuilder(JsonObject object) {
    ObjectBuilder builder = new ObjectBuilder();
    object.forEach((k, v) -> builder.add(k, Canonical.cast(v).copy()));
    return builder;
  }


  @Override
  public ObjectBuilder createObjectBuilder(Map<String, Object> object) {
    ObjectBuilder builder = new ObjectBuilder();
    object.forEach((k, v) -> builder.add(k, Canonical.create(v).copy()));
    return builder;
  }


  @Override
  public Map<String, ?> getConfigInUse() {
    return Collections.emptyMap();
  }

}
