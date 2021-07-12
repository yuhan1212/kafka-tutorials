package io.confluent.developer;

import java.util.ArrayList;
import java.util.List;
import io.confluent.developer.serdes.JSONSerdeCompatible;

public class AggregateObject extends JSONSerdeCompatible {

  public List<String> values;

  public AggregateObject() {
    values = new ArrayList<>();
  }

  public AggregateObject add(String v) {
    values.add(v);
    return this;
  }

  public List<String> getValues() {
    return values;
  }

  @Override
  public String toString() {
    return "AggregateObject [values=" + values + "]";
  }


}
