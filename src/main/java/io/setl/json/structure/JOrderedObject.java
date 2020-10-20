package io.setl.json.structure;

import java.util.Collection;
import java.util.Set;
import javax.json.JsonValue;

import io.setl.json.JObject;

/**
 * A view upon an JObject which has insertion order iterations instead of key order iterations.
 *
 * @author Simon Greatrix on 20/10/2020.
 */
public class JOrderedObject extends JObject {

  @Override
  public Set<Entry<String, JsonValue>> entrySet() {
    // TODO : Implement me! simon 20/10/2020
    return super.entrySet();
  }


  @Override
  public Set<String> keySet() {
    // TODO : Implement me! simon 20/10/2020
    return super.keySet();
  }


  @Override
  public Collection<JsonValue> values() {
    // TODO : Implement me! simon 20/10/2020
    return super.values();
  }

}
