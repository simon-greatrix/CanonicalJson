package com.pippsford.json.jackson.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simon Greatrix on 2020-01-07.
 */
public class Fleet {

  private List<Vehicle> vehicles = new ArrayList<>();


  public void add(Vehicle v) {
    vehicles.add(v);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Fleet)) {
      return false;
    }

    Fleet fleet = (Fleet) o;

    return vehicles.equals(fleet.vehicles);
  }


  public List<Vehicle> getVehicles() {
    return vehicles;
  }


  @Override
  public int hashCode() {
    return vehicles.hashCode();
  }


  public void setVehicles(List<Vehicle> vehicles) {
    this.vehicles = vehicles;
  }

}
