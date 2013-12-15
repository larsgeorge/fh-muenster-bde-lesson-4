package org.fhmuenster.bde.http;

/**
 * Represents a request parameter.
 */
public class RequestParameter {
  private String name = null;
  private String value = null;

  public RequestParameter(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof RequestParameter &&
      ((RequestParameter) obj).getName().equals(name) &&
      ((RequestParameter) obj).getValue().equals(value);
  }
}
