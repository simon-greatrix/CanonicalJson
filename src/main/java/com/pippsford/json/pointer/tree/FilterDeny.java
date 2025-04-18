package com.pippsford.json.pointer.tree;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import com.pippsford.json.pointer.PathElement;

/**
 * Filter that denies access to all descendants.
 *
 * @author Simon Greatrix on 17/02/2020.
 */
class FilterDeny implements Filter {

  /** The singleton instance. */
  public static final Filter DENY = new FilterDeny();


  private FilterDeny() {
    // this is a singleton
  }


  @Override
  public void add(PathElement element) {
    throw new UnsupportedOperationException("The DENY filter is immutable.");
  }


  @Override
  public boolean allowValue() {
    return false;
  }


  @Override
  public boolean containsAll(JsonObject jsonObject) {
    return false;
  }


  @Override
  public boolean containsAll(JsonArray jsonArray) {
    return false;
  }

}
