package io.setl.json.structure;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.setl.json.JObject;
import io.setl.json.jackson.JsonObjectSerializer;

/**
 * View of an object in JSON represented as a Navigable Map. Any changes to this will also be applied to the original JSON object.
 *
 * @author Simon Greatrix
 */

@JsonSerialize(using = JsonObjectSerializer.class)
public class JNavigableObject extends JObject implements NavigableMap<String, JsonValue> {


  private static Entry<String, JsonValue> nv(Entry<String, JObjectEntry> entry) {
    return entry != null ? entry.getValue() : null;
  }


  /**
   * Create a JSON view on the supplied map. Note that instances of this are views upon another object and hence this constructor does *not* create a deep copy.
   *
   * @param map the map to encapsulate
   */
  public JNavigableObject(NavigableMap<String, JObjectEntry> map) {
    super(map, false);
  }


  @Override
  public Entry<String, JsonValue> ceilingEntry(String key) {
    return nv(myMap.ceilingEntry(key));
  }


  @Override
  public String ceilingKey(String key) {
    return myMap.ceilingKey(key);
  }


  @Override
  public Comparator<? super String> comparator() {
    return myMap.comparator();
  }


  /**
   * {@inheritDoc}
   *
   * <p>This copy will not be linked to the original JSON object and changes to the copy will not affect any other pre-existing object.</p>
   */
  @Override
  public JNavigableObject copy() {
    return new JNavigableObject(myMap);
  }


  @Override
  public NavigableSet<String> descendingKeySet() {
    return myMap.descendingKeySet();
  }


  @Override
  public NavigableMap<String, JsonValue> descendingMap() {
    return new JNavigableObject(myMap.descendingMap());
  }


  @Override
  public Entry<String, JsonValue> firstEntry() {
    return nv(myMap.firstEntry());
  }


  @Override
  public String firstKey() {
    return myMap.firstKey();
  }


  @Override
  public Entry<String, JsonValue> floorEntry(String key) {
    return nv(myMap.floorEntry(key));
  }


  @Override
  public String floorKey(String key) {
    return myMap.floorKey(key);
  }


  @Override
  public NavigableMap<String, JsonValue> headMap(String toKey, boolean inclusive) {
    return new JNavigableObject(myMap.headMap(toKey, inclusive));
  }


  @Override
  @Nonnull
  public SortedMap<String, JsonValue> headMap(String toKey) {
    return new JNavigableObject(myMap.headMap(toKey, false));
  }


  @Override
  public Entry<String, JsonValue> higherEntry(String key) {
    return nv(myMap.higherEntry(key));
  }


  @Override
  public String higherKey(String key) {
    return myMap.higherKey(key);
  }


  @Override
  public Entry<String, JsonValue> lastEntry() {
    return nv(myMap.lastEntry());
  }


  @Override
  public String lastKey() {
    return myMap.lastKey();
  }


  @Override
  public Entry<String, JsonValue> lowerEntry(String key) {
    return nv(myMap.lowerEntry(key));
  }


  @Override
  public String lowerKey(String key) {
    return myMap.lowerKey(key);
  }


  @Override
  public NavigableSet<String> navigableKeySet() {
    return myMap.navigableKeySet();
  }


  @Override
  public JNavigableObject navigableView() {
    return this;
  }


  @Override
  public Entry<String, JsonValue> pollFirstEntry() {
    return nv(myMap.pollFirstEntry());
  }


  @Override
  public Entry<String, JsonValue> pollLastEntry() {
    return nv(myMap.pollLastEntry());
  }


  @Override
  public NavigableMap<String, JsonValue> subMap(String fromKey, boolean fromInclusive, String toKey, boolean toInclusive) {
    return new JNavigableObject(myMap.subMap(fromKey, fromInclusive, toKey, toInclusive));
  }


  @Override
  @Nonnull
  public SortedMap<String, JsonValue> subMap(String fromKey, String toKey) {
    return new JNavigableObject(myMap.subMap(fromKey, true, toKey, false));
  }


  @Override
  public NavigableMap<String, JsonValue> tailMap(String fromKey, boolean inclusive) {
    return new JNavigableObject(myMap.tailMap(fromKey, inclusive));
  }


  @Override
  @Nonnull
  public SortedMap<String, JsonValue> tailMap(String fromKey) {
    return new JNavigableObject(myMap.tailMap(fromKey, true));
  }


  @Override
  public JObject withInsertOrder() {
    throw new UnsupportedOperationException("A navigable view on a JSON Object cannot be viewed in insert order.");
  }

}
