package org.fhmuenster.bde.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.fhmuenster.bde.config.PortalConfiguration;
import org.fhmuenster.bde.model.ResultDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides help to the JSP (as opposed to code it all there).
 */
public class PortalJspHelper {

  protected final static Logger LOG = LoggerFactory.getLogger(PortalJspHelper.class);

  private static final String PARAM_START = "s";
  private static final String PARAM_SEARCHTEXT = "q";
  private static final String PARAM_DOCSPERPAGE = "dc";
  private static final String PARAM_SHOWERROR = "SHOWERROR";

  private HttpServletRequest request;
  private HttpServletResponse response;
  private ServletContext context;
  private PortalConfiguration config;

  private ObjectMapper objectMapper = new ObjectMapper();

  public PortalJspHelper(HttpServletRequest request, HttpServletResponse response,
    ServletContext context, PortalConfiguration config) {
    this.request = request;
    this.response = response;
    this.context = context;
    this.config = config;
  }

  public String shorten(String text, int maxLength) {
    if (text.length() > maxLength) {
      return text.substring(0, maxLength) + "...";
    }
    return text;
  }

  /**
   * Wraps all search relevant parameters.
   *
   * @return THe wrapper instance.
   */
  public SearchParameters getSearchParameters() {
    SearchParameters params = new SearchParameters();
    params.setSearchText(request.getParameter(PARAM_SEARCHTEXT));
    String sn = request.getParameter(PARAM_START);
    if (sn != null) {
      params.setStartNum(Integer.parseInt(sn));
    }
    sn = request.getParameter(PARAM_DOCSPERPAGE);
    if (sn != null) {
      params.setDocsPerPage(Integer.parseInt(sn));
    }
    return params;
  }

  /**
   * Builds a list with all possible current request parameters.
   *
   * @param params The list of given parameters and their value.
   * @return Returns an itemized
   */
  private ArrayList<RequestParameter> getRequestParameter(SearchParameters params) {
    ArrayList<RequestParameter> rp = new ArrayList<RequestParameter>();
    // add query details
    if(params.getSearchText() != null && params.getSearchText().length() > 0) {
      rp.add(new RequestParameter(PARAM_SEARCHTEXT, params.getSearchText()));
    }
    if(params.getStartNum() > 0) {
      rp.add(new RequestParameter(PARAM_START, Integer.toString(params.getStartNum())));
    }
    if(params.getDocsPerPage() > 0) {
      rp.add(new RequestParameter(PARAM_DOCSPERPAGE, Integer.toString(params.getDocsPerPage())));
    }
    return rp;
  }

  /**
   * Internal helper to convert the list into a string.
   *
   * @param params The given parameter list.
   * @return The same as a query string.
   */
  private String convertToString(ArrayList<RequestParameter> params) {
    StringBuilder sb = new StringBuilder();
    if (params.size() > 0) sb.append("?");
    boolean first = true;
    for (RequestParameter rp : params) {
      if (!first) sb.append("&"); else first = false;
      sb.append(rp.getName()).append("=").append(rp.getValue());
    }
    return sb.toString();
  }

  /**
   * Helper to check if a parameter already exists.
   *
   * @param params The current parameters.
   * @param name The name to check for.
   * @param value The given value to check for (optional).
   * @return True, in case the parameter exists in the list.
   */
  private boolean hasParameter(ArrayList<RequestParameter> params, String name, String value) {
    for (RequestParameter rp : params) {
      if (rp.getName().equalsIgnoreCase(name)) {
        if (value == null || (value != null && rp.getValue().equalsIgnoreCase(value))) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Helper to replace elements in the parameter list. If the parameter does not exist it is
   * inserted instead (at the end of the list).
   *
   * @param params The current parameter list.
   * @param name The name of the parameter to replace.
   * @param param The new parameter to replace with.
   * @param all Flag if all matching parameters should be replaced or just the first.
   */
  private void replaceParameter(ArrayList<RequestParameter> params, String name,
    RequestParameter param, boolean all) {
    boolean found = false;
    for (RequestParameter rp : params) {
      if (rp.getName().equalsIgnoreCase(name)) {
        rp.setName(param.getName());
        rp.setValue(param.getValue());
        found = true;
        if (!all) break;
      }
    }
    // fallback in case there is no pre-existing parameter that matched
    if (!found) {
      params.add(param);
    }
  }

  /**
   * Helper to remove elements from the parameter list.
   *
   * @param params The current parameter list.
   * @param name The name of the parameter to remove.
   * @param value The (optional) value to check as well for a match.
   * @param all Flag if all matching parameters should be removed or just the first.
   */
  private void removeParameter(ArrayList<RequestParameter> params, String name, String value,
    boolean all) {
    ListIterator<RequestParameter> iter = params.listIterator(params.size());
    while (iter.hasPrevious()) {
      RequestParameter rp = iter.previous();
      if (rp.getName().equalsIgnoreCase(name)) {
        if (value == null || (value != null && rp.getValue().equalsIgnoreCase(value))) {
          iter.remove();
          if (!all) break;
        }
      }
    }
  }

  /**
   * Helper to convert objects into JSON.
   *
   * @param object The object to convert.
   * @return The JSON version of the object.
   * @throws JsonProcessingException When the conversion fails.
   */
  public String convertToJson(Object object) throws JsonProcessingException {
    return object != null ? objectMapper.writeValueAsString(object) : "";
  }

  /**
   * Converts the JSON from the REST server to an object instance.
   *
   * @param json The JSON to parse.
   * @return The list with the results. Will be empty on exceptions.
   */
  public ArrayList<ResultDocument> convertResultDocuments(String json) {
    ArrayList<ResultDocument> docs = new ArrayList<ResultDocument>();
    try {
      Map<String, String> data = objectMapper.readValue(json, Map.class);
      for (Map.Entry<String, String> entry : data.entrySet()) {
        ResultDocument doc = new ResultDocument(
          Double.parseDouble(entry.getKey()), entry.getValue());
        docs.add(doc);
      }
    } catch (IOException e) {
      LOG.error("An error occurred.", e);
    }
    return docs;
  }

  /**
   * Helper to parse document ID out of name.
   *
   * @param name The short name of a document.
   * @return The ID or <code>null</code>.
   */
  public Integer getDocumentId(String name) {
    return name != null ? Integer.parseInt(name.replaceAll("[a-zA-Z]", "")) : null;
  }
}
