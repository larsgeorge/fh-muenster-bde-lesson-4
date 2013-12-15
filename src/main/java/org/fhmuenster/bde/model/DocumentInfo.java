package org.fhmuenster.bde.model;

/**
 * Holds info on an indexed book.
 */
public class DocumentInfo {
  private String shortName;
  private String title;
  private String author;
  private String releaseDate;

  public DocumentInfo() {
  }

  public DocumentInfo(String shortName, String title, String author, String releaseDate) {
    this.shortName = shortName;
    this.title = title;
    this.author = author;
    this.releaseDate = releaseDate;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(String releaseDate) {
    this.releaseDate = releaseDate;
  }
}
