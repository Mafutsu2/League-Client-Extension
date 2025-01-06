package org.mafutsu.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Credentials {
  private String lcuPort, riotPort, lcuAuth, riotAuth, region;

  public Credentials() {}

  public boolean init() {
    String command = getCommand();
    if(command == null)
      return false;

    String[] tokens = getToken(command);

    if(tokens == null)
      return false;

    String username = "riot";
    lcuAuth = Base64.getEncoder().encodeToString((username + ":" + tokens[3]).getBytes());
    riotAuth = Base64.getEncoder().encodeToString((username + ":" + tokens[0]).getBytes());
    region = tokens[2];
    lcuPort = tokens[4];
    riotPort = tokens[1];
    return true;
  }

  public String getLcuPort() {
    return lcuPort;
  }

  public String getRiotPort() {
    return riotPort;
  }

  public String getLcuAuth() {
    return lcuAuth;
  }

  public String getRiotAuth() {
    return riotAuth;
  }

  public String getRegion() {
    return region;
  }

  private String getCommand() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("cmd.exe", "/c", "wmic PROCESS WHERE name='LeagueClientUx.exe' GET commandline");

    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      ArrayList<String> list = new ArrayList<>();
      String line;
      while((line = reader.readLine()) != null) {
        list.add(line);
      }

      int exitCode = process.waitFor();
      System.out.println("\nExited with error code : " + exitCode);
      for(String l : list) {
        if(l.length() > 0 && l.charAt(0) == '"') {
          return l;
        }
      }
    } catch(IOException e) {
      e.printStackTrace();
    } catch(InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String[] getToken(String command) {
    String[] result = new String[5];
    System.out.println(command);
    Pattern pattern = Pattern.compile("--riotclient-auth-token=([\\w-]*).*--riotclient-app-port=([0-9]*).*--region=([\\w-]*).*--remoting-auth-token=([\\w-]*).*--app-port=([0-9]*)");
    Matcher matcher = pattern.matcher(command);
    if(!matcher.find())
      return null;
    result[0] = matcher.group(1);
    result[1] = matcher.group(2);
    result[2] = matcher.group(3);
    result[3] = matcher.group(4);
    result[4] = matcher.group(5);
    return result;
  }
}
