package com.pippsford.json.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import com.pippsford.json.CJArray;
import com.pippsford.json.CJObject;
import com.pippsford.json.Canonical;
import com.pippsford.json.jackson.objects.Car;
import com.pippsford.json.jackson.objects.Fleet;
import com.pippsford.json.jackson.objects.Truck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Simon Greatrix on 2020-01-07.
 */
public class CanonicalSerializerTest {

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
    JsonMapper mapper = JsonMapper.builder(new CanonicalFactory()).addModule(new CanonicalJsonModule()).build();

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

}
