package com.windhoverlabs.yamcs;

import org.yamcs.protobuf.Yamcs;

public class RegistryEntry {
  private String path;
  private Yamcs.Value.Type dataType;
  private String stringValue = "";
  private Long longValue = 0L;
  private int intValue = 0;
  private boolean boolValue = false;
  private double dblValue = 0.0;

  public RegistryEntry(String path, Yamcs.Value.Type dataType, String value) {
    this.path = path;
    this.dataType = dataType;
    this.stringValue = value;
  }

  public RegistryEntry(String path, Yamcs.Value.Type dataType, Long value) {
    this.path = path;
    this.dataType = dataType;
    this.longValue = value;
  }

  public RegistryEntry(String path, Yamcs.Value.Type dataType, int value) {
    this.path = path;
    this.dataType = dataType;
    this.intValue = value;
  }

  public RegistryEntry(String path, Yamcs.Value.Type dataType, boolean value) {
    this.path = path;
    this.dataType = dataType;
    this.boolValue = value;
  }

  public RegistryEntry(String path, Yamcs.Value.Type dataType, double value) {
    this.path = path;
    this.dataType = dataType;
    this.dblValue = value;
  }

  public String getPath() {
    return path;
  }

  public Yamcs.Value.Type getDataType() {
    return dataType;
  }

  public String getStringValue() {
    return stringValue;
  }

  public Long getLongValue() {
    return longValue;
  }

  public int getIntValue() {
    return intValue;
  }

  public boolean getBoolValue() {
    return boolValue;
  }

  public double getDblValue() {
    return dblValue;
  }
}
