package org.mafutsu.model;

public class Website {
  private String id, url;
  private boolean needDash;

  public Website(String id, String url, boolean needDash) {
    this.id = id;
    this.url = url;
    this.needDash = needDash;
  }

  public String getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public boolean needDash() {
    return needDash;
  }
}
