package com.google.sps.data;

public class Cases {
  private String state;
  private Integer cases;
  private Integer deaths;

  public Cases(String state, Integer cases, Integer deaths) {
    this.state = state;
    this.cases = cases;
    this.deaths = deaths;
  }
}
