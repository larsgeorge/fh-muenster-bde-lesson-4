package org.fhmuenster.bde.http;

/**
 * Encapsulates the request parameters for a search.
 */
public class SearchParameters {
  private String searchText = null;
  private int startNum = -1;
  private int docsPerPage = -1;

  public SearchParameters() {
  }

  public SearchParameters(String searchText, int startNum, int docsPerPage) {
    this.searchText = searchText;
    this.startNum = startNum;
    this.docsPerPage = docsPerPage;
  }

  public String getSearchText() {
    return searchText;
  }

  public void setSearchText(String searchText) {
    this.searchText = searchText;
  }

  public int getStartNum() {
    return startNum;
  }

  public void setStartNum(int startNum) {
    this.startNum = startNum;
  }

  public int getDocsPerPage() {
    return docsPerPage;
  }

  public void setDocsPerPage(int docsPerPage) {
    this.docsPerPage = docsPerPage;
  }
}
