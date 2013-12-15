package org.fhmuenster.bde.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helper to wrap configuration.
 */
public class PortalConfiguration {

  private HashMap<String, String> config = null;

  public PortalConfiguration() {
    config = new HashMap<String, String>();
    load("portal.properties");
  }

  public void load(String filename) {
    InputStream in = ClassLoader.getSystemResourceAsStream(filename);
    if (in == null) {
      File file = new File(filename);
      if (file.exists()) {
        try {
          in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
          System.err.println("WARN: Could not load configuration!");
        }
      }
    }
    load(in);
  }

  public void load(InputStream in) {
    try {
      Properties properties = new Properties();
      properties.load(in);
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        config.put(entry.getKey().toString(), entry.getValue().toString());
      }
    } catch (Exception e) {
      System.err.println("WARN: Could not load configuration!");
    }
  }

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writeValueAsString(new TreeMap<String, String>(config));
    } catch (JsonProcessingException e) {
      return "An error occurred: " + e.getMessage();
    }
  }

  public String get(String key) {
    return config.get(key);
  }

  public String get(String key, String defaultValue) {
    return config.containsKey(key) ? config.get(key) : defaultValue;
  }

  public int getInt(String key) {
    return Integer.parseInt(config.get(key));
  }

  public int getInt(String key, int defaultValue) {
    return config.containsKey(key) ? Integer.parseInt(config.get(key)) : defaultValue;
  }

  public void set(String key, String value) {
    config.put(key, value);
  }
}
