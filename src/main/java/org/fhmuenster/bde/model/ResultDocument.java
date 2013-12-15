package org.fhmuenster.bde.model;

public class ResultDocument {
  private double score = 0.0;
  private String name = null;

  public ResultDocument(double score, String name) {
    this.score = score;
    this.name = name;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
