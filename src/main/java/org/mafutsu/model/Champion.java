package org.mafutsu.model;

import org.json.JSONObject;

import java.text.MessageFormat;

public class Champion {
  private int id;
  private String name;

  public Champion(JSONObject jsonObject) {
    this(jsonObject.getInt("id"), jsonObject.getString("name"));
  }

  public Champion(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public JSONObject toJSON() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", id);
    jsonObject.put("name", name);
    return jsonObject;
  }

  @Override
  public String toString() {
    return MessageFormat.format("'{'{0}, {1}'}'", id, name);
  }
}
