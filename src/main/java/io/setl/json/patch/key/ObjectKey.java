package io.setl.json.patch.key;

import io.setl.json.pointer.JPointer;

public class ObjectKey extends Key {

  private final String escaped;


  public ObjectKey(Key parent, String key) {
    super(parent);
    escaped = JPointer.escapeKey(key);
  }


  @Override
  protected String getEscapedKey() {
    return escaped;
  }

}
