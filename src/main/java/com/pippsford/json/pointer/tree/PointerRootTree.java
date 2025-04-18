package com.pippsford.json.pointer.tree;

import java.util.Collections;
import java.util.List;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import com.pippsford.json.Canonical;
import com.pippsford.json.pointer.EmptyPointer;
import com.pippsford.json.pointer.JsonExtendedPointer;

/**
 * A pointer tree that contains the empty pointer and hence has access to everything.
 *
 * @author Simon Greatrix on 17/02/2020.
 */
public class PointerRootTree implements PointerTree {

  /** The root tree. */
  public static final PointerTree INSTANCE = new PointerRootTree();


  private PointerRootTree() {
    // this is a singleton
  }


  @Override
  public boolean containsAll(@Nonnull JsonValue value) {
    return true;
  }


  @Nullable
  @Override
  @SuppressWarnings("unchecked")
  public <T extends JsonStructure> T copy(@Nonnull T value) {
    // everything is copied
    return (T) Canonical.cast(value).copy();
  }


  @Override
  public List<JsonExtendedPointer> getPointers() {
    return Collections.singletonList(EmptyPointer.INSTANCE);
  }


  @Override
  public boolean isParentOf(JsonExtendedPointer pointer) {
    return true;
  }


  @Nullable
  @Override
  public <T extends JsonStructure> T remove(T value) {
    // Everything is removed.
    return null;
  }

}
