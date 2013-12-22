package org.fhmuenster.bde.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.fhmuenster.bde.config.PortalConfiguration;
import org.fhmuenster.bde.model.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines the main API exported by this server.
 */
public class PortalApi {

  protected final static Logger LOG = LoggerFactory.getLogger(PortalApi.class);

  private @Inject PortalConfiguration portalConfiguration;
  private Map<String, TreeMap<Double, String>> index;
  private Map<String, DocumentInfo> documentInfoMap;
  private ObjectMapper objectMapper = new ObjectMapper();
  private Pattern pattern = Pattern.compile(
    "(\\w+)@(\\w+)\\.\\w+\\s+\\[\\d+/\\d+ , \\d+/\\d+ , (\\S+)\\]");

  // angles@pg30601.txt      [8/20 , 3/40770 , 0.00002928]
  // angles@pg4300.txt       [8/20 , 3/174698 , 0.00000683]
  // ..

  /**
   * Constructor.
   */
  public PortalApi() {
  }

  /**
   * Loads the search index.
   *
   * @throws IOException When the data to be loaded is faulty or missing.
   */
  private void loadIndex() throws IOException {
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    String fn = portalConfiguration.get("input");
    Path in = new Path(fn);
    FSDataInputStream dis = fs.open(in);
    BufferedReader br = new BufferedReader(new InputStreamReader(dis));
    index = new HashMap<String, TreeMap<Double, String>>();
    String line = br.readLine();
    while (line != null) {
      Matcher m = pattern.matcher(line);
      if (m.matches()) {
        String word = m.group(1);
        String fileName = m.group(2);
        Double score = Double.parseDouble(m.group(3));
        TreeMap<Double, String> wordIndex = index.get(word);
        if (wordIndex == null) {
          wordIndex = new TreeMap<Double, String>(new Comparator<Double>() {
            public int compare(Double o1, Double o2) {
              return o2.compareTo(o1);
            }
          });
          index.put(word, wordIndex);
        }
        wordIndex.put(score, fileName);
      } else {
        LOG.debug("Failed to match line \"{}\"", line);
      }
      line = br.readLine();
    }
  }

  /**
   * Loads the mapping between short name and full info.
   *
   * @throws IOException When the mapping file is missing.
   */
  private void loadMappings() throws IOException {
    documentInfoMap = new HashMap<String, DocumentInfo>();
    InputStream is = this.getClass().getResourceAsStream("/bookmappings.properties");
    Properties props = new Properties();
    props.load(is);
    HashSet<String> docIds = new HashSet<String>(props.size());
    for (String prop : props.stringPropertyNames()) {
      String[] parts = prop.split("\\.");
      docIds.add(parts[0]);
    }
    for (String docId : docIds) {
      DocumentInfo info = new DocumentInfo();
      info.setShortName(docId);
      String s = props.getProperty(docId + ".title");
      if (s != null && s.length() >0 ) info.setTitle(s);
      s = props.getProperty(docId + ".author");
      if (s != null && s.length() > 0) info.setAuthor(s);
      s = props.getProperty(docId + ".reldate");
      if (s != null && s.length() > 0) info.setReleaseDate(s);
      documentInfoMap.put(docId, info);
    }
  }

  /**
   * Provides a REST mapped implementation of the search funktion.
   *
   * @param searchText The text to search for in the index.
   * @return The results or an error message.
   * @throws IOException When the search fails to access the index.
   */
  public String search(String searchText) throws IOException {
    if (index == null) loadIndex();
    TreeMap<Double, String> wordIndex = index.get(searchText);
    try {
      return wordIndex != null ? objectMapper.writeValueAsString(wordIndex) : "[]";
    } catch (JsonProcessingException e) {
      return "[ \"error\": \"" + e.getMessage() + "\"]";
    }
  }

  /**
   * Lookup for a document info record based on a short name.
   *
   * @param shortName The short name of the document.
   * @return The info records or <code>null</code>.
   */
  public DocumentInfo getDocumentInfo(String shortName) throws IOException {
    if (documentInfoMap == null) loadMappings();
    return documentInfoMap.get(shortName);
  }

  /**
   * Called from REST API. Purely for testing.
   */
  public void noop() {

  }
}
