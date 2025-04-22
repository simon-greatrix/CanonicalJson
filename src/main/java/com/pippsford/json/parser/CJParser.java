package com.pippsford.json.parser;

import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import com.pippsford.json.Canonical;
import jakarta.json.stream.JsonParser;

/** A JsonParser that returns Canonical values. */
public interface CJParser extends JsonParser {

  @Override
  CJArray getArray();

  @Override
  CJObject getObject();

  @Override
  Canonical getValue();

}
