package com.pippsford.json.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import com.pippsford.json.Canonical;
import com.pippsford.json.jackson.objects.Car;
import com.pippsford.json.jackson.objects.Fleet;
import com.pippsford.json.jackson.objects.Truck;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Greatrix on 2020-01-07.
 */
public class CanonicalSerializerTest {

  /** Holder whose field carries @JsonTypeInfo so Jackson calls serializeWithType(). */
  static class CanonicalHolder {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
    public Canonical value;

    CanonicalHolder() {}

    CanonicalHolder(Canonical v) {
      value = v;
    }


    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CanonicalHolder that = (CanonicalHolder) o;
      return Objects.equals(value, that.value);
    }


    @Override
    public int hashCode() {
      return Objects.hashCode(value);
    }
  }



  /** Holder whose field carries @JsonTypeInfo so Jackson calls serializeWithType(). */
  static class CanonicalHolder2 {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.WRAPPER_OBJECT)
    public Canonical value;

    CanonicalHolder2() {}

    CanonicalHolder2(Canonical v) {
      value = v;
    }


    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CanonicalHolder2 that = (CanonicalHolder2) o;
      return Objects.equals(value, that.value);
    }


    @Override
    public int hashCode() {
      return Objects.hashCode(value);
    }
  }



  /** Holder whose field carries @JsonTypeInfo so Jackson calls serializeWithType(). */
  static class CanonicalHolder3 {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY)
    public Canonical value;

    CanonicalHolder3() {}

    CanonicalHolder3(Canonical v) {
      value = v;
    }


    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CanonicalHolder3 that = (CanonicalHolder3) o;
      return Objects.equals(value, that.value);
    }


    @Override
    public int hashCode() {
      return Objects.hashCode(value);
    }
  }



  Fleet fleet = new Fleet();


  @BeforeEach
  public void before() {
    Car car1 = new Car("Ford", "Mondeo", 5, 120);
    car1.setMetadata(Canonical.create("META"));
    Car car2 = new Car("Mercedes-Benz", "S500", 5, 250.0);
    CJObject object = new CJObject();
    object.put("A", 123);
    car2.setMetadata(object);

    Truck truck1 = new Truck("Isuzu", "NQR", 7500.0);
    CJArray array = new CJArray();
    array.add(1);
    array.add("B");
    truck1.setMetadata(array);
    Truck truck2 = new Truck("BMW", "X6", 6000.0);
    truck2.setDocuments(object);
    fleet.add(car1);
    fleet.add(truck1);
    fleet.add(car2);
    fleet.add(truck2);
  }


  @Test
  public void serialize() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new CanonicalFactory());
    mapper.registerModule(new JsonModule());

    String json = mapper.writeValueAsString(fleet);
    // Warning, if you refactor the code, the class names in this will break.
    assertEquals(
        "{\"vehicles\":["
            + "[\"com.pippsford.json.jackson.objects.Car\","
            + "{\"make\":\"Ford\",\"metadata\":\"META\",\"model\":\"Mondeo\",\"seatingCapacity\":5,\"topSpeed\":120}],"
            + "[\"com.pippsford.json.jackson.objects.Truck\","
            + "{\"documents\":null,\"make\":\"Isuzu\",\"metadata\":[1,\"B\"],\"model\":\"NQR\",\"payloadCapacity\":7500}],"
            + "[\"com.pippsford.json.jackson.objects.Car\","
            + "{\"make\":\"Mercedes-Benz\",\"metadata\":{\"A\":123},\"model\":\"S500\",\"seatingCapacity\":5,\"topSpeed\":250}],"
            + "[\"com.pippsford.json.jackson.objects.Truck\","
            + "{\"documents\":{\"A\":123},\"make\":\"BMW\",\"metadata\":null,\"model\":\"X6\",\"payloadCapacity\":6000}]]}",
        json
    );

    Fleet copy = mapper.readValue(json, Fleet.class);
    assertEquals(fleet, copy);
  }


  @Test
  public void serializeViaJacksonGenerator() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());

    CJObject obj = new CJObject();
    obj.put("a", 1);
    obj.put("b", true);
    String objJson = mapper.writeValueAsString(obj);
    assertEquals("{\"a\":1,\"b\":true}", objJson);
    assertEquals(obj.toString(), mapper.readValue(objJson, JsonObject.class).toString());

    CJArray arr = new CJArray(Arrays.asList(1, "x", null));
    String arrJson = mapper.writeValueAsString(arr);
    assertEquals("[1,\"x\",null]", arrJson);
    assertEquals(arr.toString(), mapper.readValue(arrJson, JsonArray.class).toString());
  }


  @Test
  public void serializeWithTypeArray() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());

    CJArray array = new CJArray(Arrays.asList(1, 2));
    CanonicalHolder ch  = new CanonicalHolder(array);
    String json = mapper.writeValueAsString(ch);
    assertEquals("""
        {"value":["com.pippsford.json.CJArray",[1,2]]}""", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder.class));
  }


  @Test
  public void serializeWithTypeArray2() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());

    CJArray array = new CJArray(Arrays.asList(1, 2));
    CanonicalHolder2 ch  = new CanonicalHolder2(array);

    String json = mapper.writeValueAsString(ch);
    assertEquals("""
        {"value":{"com.pippsford.json.CJArray":[1,2]}}""", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder2.class));
  }


  @Test
  public void serializeWithTypeArray3() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());

    CJArray array = new CJArray(Arrays.asList(1, 2));
    CanonicalHolder3 ch  = new CanonicalHolder3(array);
    String json = mapper.writeValueAsString(ch);
    assertEquals("""
        {"value":["com.pippsford.json.CJArray",[1,2]]}""", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder3.class));
  }


  @Test
  public void serializeWithTypeObject() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());

    CJObject obj = new CJObject();
    obj.put("a", 1);
    CanonicalHolder ch  = new CanonicalHolder(obj);
    String json = mapper.writeValueAsString(ch);
    assertEquals("""
        {"value":["com.pippsford.json.CJObject",{"a":1}]}""", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder.class));
  }


  @Test
  public void serializeWithTypeObject2() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());

    CJObject obj = new CJObject();
    obj.put("a", 1);
    CanonicalHolder2 ch  = new CanonicalHolder2(obj);
    String json = mapper.writeValueAsString(ch);
    assertEquals("""
        {"value":{"com.pippsford.json.CJObject":{"a":1}}}""", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder2.class));
  }


  @Test
  public void serializeWithTypeObject3() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());

    CJObject obj = new CJObject();
    obj.put("a", 1);
    CanonicalHolder3 ch  = new CanonicalHolder3(obj);
    String json = mapper.writeValueAsString(ch);
    assertEquals("""
        {"value":{"@class":"com.pippsford.json.CJObject","a":1}}""", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder3.class));
  }


  @Test
  public void serializeWithTypePrimitive() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());

    Canonical str = Canonical.create("hello");
    CanonicalHolder ch  = new CanonicalHolder(str);
    String json = mapper.writeValueAsString(ch);
    assertEquals("""
        {"value":["com.pippsford.json.primitive.CJString","hello"]}""", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder.class));
  }


  @Test
  public void serializeWithTypePrimitive2() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());

    Canonical str = Canonical.create("hello");
    CanonicalHolder2 ch  = new CanonicalHolder2(str);
    String json = mapper.writeValueAsString(ch);
    assertEquals("""
        {"value":{"com.pippsford.json.primitive.CJString":"hello"}}""", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder2.class));
  }


  @Test
  public void serializeWithTypePrimitive3() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonModule());

    Canonical str = Canonical.create("hello");
    CanonicalHolder3 ch  = new CanonicalHolder3(str);
    String json = mapper.writeValueAsString(ch);
    assertEquals("""
        {"value":["com.pippsford.json.primitive.CJString","hello"]}""", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder3.class));
  }


  // --- CanonicalGenerator path (ObjectMapper with CanonicalFactory) ---

  private static ObjectMapper canonicalMapper() {
    ObjectMapper mapper = new ObjectMapper(new CanonicalFactory());
    mapper.registerModule(new JsonModule());
    return mapper;
  }


  @Test
  public void serializeWithTypeArrayCanonical() throws IOException {
    ObjectMapper mapper = canonicalMapper();
    CJArray array = new CJArray(Arrays.asList(1, 2));
    CanonicalHolder ch = new CanonicalHolder(array);
    String json = mapper.writeValueAsString(ch);
    assertEquals("{\"value\":[\"com.pippsford.json.CJArray\",[1,2]]}", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder.class));
  }


  @Test
  public void serializeWithTypeArray2Canonical() throws IOException {
    ObjectMapper mapper = canonicalMapper();
    CJArray array = new CJArray(Arrays.asList(1, 2));
    CanonicalHolder2 ch = new CanonicalHolder2(array);
    String json = mapper.writeValueAsString(ch);
    assertEquals("{\"value\":{\"com.pippsford.json.CJArray\":[1,2]}}", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder2.class));
  }


  @Test
  public void serializeWithTypeArray3Canonical() throws IOException {
    ObjectMapper mapper = canonicalMapper();
    CJArray array = new CJArray(Arrays.asList(1, 2));
    CanonicalHolder3 ch = new CanonicalHolder3(array);
    String json = mapper.writeValueAsString(ch);
    assertEquals("{\"value\":[\"com.pippsford.json.CJArray\",[1,2]]}", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder3.class));
  }


  @Test
  public void serializeWithTypeObjectCanonical() throws IOException {
    ObjectMapper mapper = canonicalMapper();
    CJObject obj = new CJObject();
    obj.put("a", 1);
    CanonicalHolder ch = new CanonicalHolder(obj);
    String json = mapper.writeValueAsString(ch);
    assertEquals("{\"value\":[\"com.pippsford.json.CJObject\",{\"a\":1}]}", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder.class));
  }


  @Test
  public void serializeWithTypeObject2Canonical() throws IOException {
    ObjectMapper mapper = canonicalMapper();
    CJObject obj = new CJObject();
    obj.put("a", 1);
    CanonicalHolder2 ch = new CanonicalHolder2(obj);
    String json = mapper.writeValueAsString(ch);
    assertEquals("{\"value\":{\"com.pippsford.json.CJObject\":{\"a\":1}}}", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder2.class));
  }


  @Test
  public void serializeWithTypeObject3Canonical() throws IOException {
    ObjectMapper mapper = canonicalMapper();
    CJObject obj = new CJObject();
    obj.put("a", 1);
    CanonicalHolder3 ch = new CanonicalHolder3(obj);
    String json = mapper.writeValueAsString(ch);
    assertEquals("{\"value\":{\"@class\":\"com.pippsford.json.CJObject\",\"a\":1}}", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder3.class));
  }


  @Test
  public void serializeWithTypePrimitiveCanonical() throws IOException {
    ObjectMapper mapper = canonicalMapper();
    Canonical str = Canonical.create("hello");
    CanonicalHolder ch = new CanonicalHolder(str);
    String json = mapper.writeValueAsString(ch);
    assertEquals("{\"value\":[\"com.pippsford.json.primitive.CJString\",\"hello\"]}", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder.class));
  }


  @Test
  public void serializeWithTypePrimitive2Canonical() throws IOException {
    ObjectMapper mapper = canonicalMapper();
    Canonical str = Canonical.create("hello");
    CanonicalHolder2 ch = new CanonicalHolder2(str);
    String json = mapper.writeValueAsString(ch);
    assertEquals("{\"value\":{\"com.pippsford.json.primitive.CJString\":\"hello\"}}", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder2.class));
  }


  @Test
  public void serializeWithTypePrimitive3Canonical() throws IOException {
    ObjectMapper mapper = canonicalMapper();
    Canonical str = Canonical.create("hello");
    CanonicalHolder3 ch = new CanonicalHolder3(str);
    String json = mapper.writeValueAsString(ch);
    assertEquals("{\"value\":[\"com.pippsford.json.primitive.CJString\",\"hello\"]}", json);
    assertEquals(ch, mapper.readValue(json, CanonicalHolder3.class));
  }
}
