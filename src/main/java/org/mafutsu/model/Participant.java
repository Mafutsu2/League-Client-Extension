package org.mafutsu.model;

public class Participant {
  private String name, tag;

  public Participant(String name, String tag) {
    this.name = name;
    this.tag = tag;
  }

  public String getName() {
    return name;
  }

  public String getTag() {
    return tag;
  }

  public String getCompleteNameHash() {
    return name + "#" + tag;
  }

  public String getCompleteNameDash() {
    return (name + "-" + tag).replaceAll("\\s+", "");
  }
}
