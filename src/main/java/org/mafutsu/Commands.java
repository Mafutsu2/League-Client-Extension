package org.mafutsu;

import org.mafutsu.model.Champion;
import org.mafutsu.model.Participant;
import org.mafutsu.model.Website;
import org.mafutsu.network.Credentials;
import org.mafutsu.network.IWSClient;
import org.mafutsu.network.RequestManager;
import org.mafutsu.network.WSClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mafutsu.util.ClientType.RIOT;
import static org.mafutsu.util.Constants.*;

public class Commands implements IWSClient {

  private final Credentials credentials;
  private final ICommands callback;
  private final RequestManager requestManager;
  private final ArrayList<String> awaitingSubEvents, currentSubEvents;
  private ArrayList<Participant> summoners;
  private WSClient wsClient;
  private boolean isWaitAccept, hasBanned, hasHovered, hasPicked, isConnectingToWS, isAutoAccept, isAutoAcceptSwap, isAutoChampSelect, isInNeedOfHover, isWaitDelay;
  private int localPlayerCellId, champSelectPhaseId, delay;
  private long timestampWithDelay;
  private String primaryRole, secondaryRole;
  private ArrayList<Champion> banChampionsPrimary, hoverChampionsPrimary, pickChampionsPrimary, banChampionsSecondary, hoverChampionsSecondary, pickChampionsSecondary;
  private ScheduledExecutorService scheduler;

  public Commands(ICommands callback) {
    this.callback = callback;
    credentials = new Credentials();
    requestManager = new RequestManager(credentials);
    isWaitAccept = false;
    hasBanned = false;
    hasHovered = false;
    hasPicked = false;
    isConnectingToWS = false;
    isAutoAccept = false;
    isAutoAcceptSwap = false;
    isAutoChampSelect = false;
    isInNeedOfHover = false;
    isWaitDelay = false;
    localPlayerCellId = -2;
    champSelectPhaseId = -2;
    delay = 500;
    banChampionsPrimary = new ArrayList<>();
    hoverChampionsPrimary = new ArrayList<>();
    pickChampionsPrimary = new ArrayList<>();
    banChampionsSecondary = new ArrayList<>();
    hoverChampionsSecondary = new ArrayList<>();
    pickChampionsSecondary = new ArrayList<>();
    awaitingSubEvents = new ArrayList<>();
    currentSubEvents = new ArrayList<>();
  }

  public void setBanChampionsPrimary(ArrayList<Champion> banChampionsPrimary) {
    this.banChampionsPrimary = banChampionsPrimary;
  }

  public void setHoverChampionsPrimary(ArrayList<Champion> hoverChampionsPrimary) {
    this.hoverChampionsPrimary = hoverChampionsPrimary;
  }

  public void setPickChampionsPrimary(ArrayList<Champion> pickChampionsPrimary) {
    this.pickChampionsPrimary = pickChampionsPrimary;
  }

  public void setBanChampionsSecondary(ArrayList<Champion> banChampionsSecondary) {
    this.banChampionsSecondary = banChampionsSecondary;
  }

  public void setHoverChampionsSecondary(ArrayList<Champion> hoverChampionsSecondary) {
    this.hoverChampionsSecondary = hoverChampionsSecondary;
  }

  public void setPickChampionsSecondary(ArrayList<Champion> pickChampionsSecondary) {
    this.pickChampionsSecondary = pickChampionsSecondary;
  }

  public void setPrimaryRole(String mainRole) {
    this.primaryRole = mainRole;
  }

  public void setSecondaryRole(String secondaryRole) {
    this.secondaryRole = secondaryRole;
  }

  public void setDelay(int delay) {
    this.delay = delay;
  }

  public ArrayList<Champion> getChampionsList() {
    ArrayList<Champion> champions = new ArrayList<>();
    String versionsJSON = requestManager.sendOtherRequest("https://ddragon.leagueoflegends.com/api/versions.json");
    JSONArray versionsArray = new JSONArray(versionsJSON);
    String lastestVersion = versionsArray.getString(0);
    String championsJSON = requestManager.sendOtherRequest("https://ddragon.leagueoflegends.com/cdn/" + lastestVersion + "/data/en_US/champion.json");
    JSONObject championsObject = new JSONObject(championsJSON);
    JSONObject championsList = championsObject.getJSONObject("data");
    JSONArray keys = championsList.names();
    for(int i = 0; i < keys.length(); i++) {
      JSONObject champion = championsList.getJSONObject(keys.getString(i));
      champions.add(new Champion(champion.getInt("key"), champion.getString("name")));
    }
    return champions;
  }

  public boolean connect() {
    return credentials.init();
  }

  public void connectWebsocket() {
    try {
      Map<String, String> map = new HashMap<>();
      map.put("Authorization", "Basic " + credentials.getLcuAuth());
      wsClient = new WSClient(new URI("wss://127.0.0.1:" + credentials.getLcuPort()), map, this);

      SSLContext sslContext = RequestManager.bypassCertificate();
      SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();
      wsClient.setSocketFactory(factory);

      wsClient.connect();
    } catch(URISyntaxException e) {
      e.printStackTrace();
    }
  }

  public void disconnectWebsocket() {
    if(wsClient != null && !wsClient.isClosed()) {
      wsClient.close();
      wsClient = null;
    }
  }

  @Override
  public void onOpenConnection() {
    for(String awaitingSubEvent : awaitingSubEvents)
      wsClient.send("[5, \"" + awaitingSubEvent + "\"]");
    awaitingSubEvents.clear();
  }

  @Override
  public void onCloseConnection() {
    wsClient = null;
    callback.onCloseConnection();
  }

  @Override
  public void onReceiveMessage(String message) {
    onReceiveMessage(message, true);
  }

  public void onReceiveMessage(String message, boolean canRedo) {
    JSONArray arr = new JSONArray(message);
    if(isAutoAccept && arr.get(1).equals("OnJsonApiEvent_lol-matchmaking_v1_search")) {
      JSONObject data = (JSONObject) arr.get(2);
      if(data.opt("data") instanceof JSONObject dataObj) {
        if(dataObj.opt("readyCheck") instanceof JSONObject readyCheck) {
          if(readyCheck.get("state").equals("InProgress") && readyCheck.get("playerResponse").equals("None")) {
            if(!isWaitDelay) {
              isWaitDelay = true;
              scheduler = Executors.newScheduledThreadPool(1);
              Runnable task = () -> {
                if(isWaitDelay) {
                  acceptGame();
                  isWaitDelay = false;
                }
              };
              scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
              scheduler.shutdown();
            }
          } else if(readyCheck.get("state").equals("InProgress")) {
            scheduler.shutdownNow();
            isWaitDelay = false;
          }
        }
      }
    } else if(arr.get(1).equals("OnJsonApiEvent_lol-champ-select_v1_session")) {
      JSONObject data = ((JSONObject) arr.get(2)).getJSONObject("data");
      localPlayerCellId = data.getInt("localPlayerCellId");
      if(isAutoAcceptSwap && !data.getJSONArray("pickOrderSwaps").isEmpty()) {
        JSONArray pickOrderSwaps = data.getJSONArray("pickOrderSwaps");
        for(int i = 0; i < pickOrderSwaps.length(); i++) {
          if(pickOrderSwaps.getJSONObject(i).getString("state").equals("RECEIVED")) {
            isInNeedOfHover = true;
            System.out.println("swapped");
            swapPickOrder(pickOrderSwaps.getJSONObject(i).getInt("id"));
            return;
          }
        }
      }
      if(isAutoChampSelect && !data.getJSONArray("actions").isEmpty()) {
        int isRoleMatching = -1;
        JSONArray myTeam = data.getJSONArray("myTeam");
        for(int i = 0; i < myTeam.length(); i++) {
          JSONObject teamPlayer = myTeam.getJSONObject(i);
          if(teamPlayer.getInt("cellId") == localPlayerCellId) {
            String roleAssigned = teamPlayer.getString("assignedPosition");
            if(roleAssigned.equalsIgnoreCase(primaryRole) || primaryRole.equalsIgnoreCase(POSITION_FILL)) {
              isRoleMatching = 0;
            } else if(roleAssigned.equalsIgnoreCase(secondaryRole) || secondaryRole.equalsIgnoreCase(POSITION_FILL)) {
              isRoleMatching = 1;
            }
            break;
          }
        }
        if(isRoleMatching == -1)
          return;
        JSONArray actions = data.getJSONArray("actions");
        for(int i = 0; i < actions.length(); i++) {
          for(int j = 0; j < actions.getJSONArray(i).length(); j++) {
            JSONObject currentAction = actions.getJSONArray(i).getJSONObject(j);
            if(currentAction.getInt("actorCellId") == localPlayerCellId) {
              try {
                if(currentAction.getString("type").equals("pick") && !currentAction.getBoolean("completed") && currentAction.getBoolean("isInProgress") && data.getJSONObject("timer").getString("phase").equals("BAN_PICK"))
                  parsePick(data, currentAction, isRoleMatching == 0 ? pickChampionsPrimary : pickChampionsSecondary, currentAction.getInt("championId"));
                else if(isInNeedOfHover || (currentAction.getString("type").equals("pick") && data.getJSONObject("timer").getString("phase").equals("PLANNING")))
                  parseHover(currentAction, canRedo, isRoleMatching == 0 ? hoverChampionsPrimary : hoverChampionsSecondary);
                else if(currentAction.getString("type").equals("ban") && !currentAction.getBoolean("completed") && currentAction.getBoolean("isInProgress") && data.getJSONObject("timer").getString("phase").equals("BAN_PICK"))
                  parseBan(data, currentAction, isRoleMatching == 0 ? banChampionsPrimary : banChampionsSecondary, currentAction.getInt("championId"));
              } catch(Exception e) {
                e.printStackTrace();
                if(canRedo)
                  onReceiveMessage(message, false);
              }
            }
          }
        }
      } else {
        localPlayerCellId = -2;
        champSelectPhaseId = -2;
        hasBanned = false;
        hasHovered = false;
        hasPicked = false;
      }
    }
  }

  public void parseHover(JSONObject currentAction, boolean canRedo, ArrayList<Champion> hoverChampions) throws Exception {
    if((!isInNeedOfHover && hasHovered) || hoverChampions.isEmpty())
      return;
    hasHovered = true;
    if(isInNeedOfHover)
      isInNeedOfHover = false;
    else if(canRedo)
      Thread.sleep(8000);
    System.out.println("hovered");
    pickChampion(hoverChampions.getFirst().getId(), currentAction.getInt("id"), true);
  }

  public void parsePick(JSONObject data, JSONObject currentAction, ArrayList<Champion> pickChampions, int hoveredChampionId) {
    if(hasPicked || pickChampions.isEmpty())
      return;
    for(Champion pickChampion : pickChampions) {
      if(!getBans(data).contains(pickChampion.getId()) && !getPicks(data, localPlayerCellId, false).contains(pickChampion.getId())) {
        boolean isHover = hoveredChampionId != pickChampion.getId();
        if(!isHover)
          hasPicked = true;
        System.out.println("picked");
        pickChampion(pickChampion.getId(), currentAction.getInt("id"), false);
        break;
      }
    }
  }

  public void parseBan(JSONObject data, JSONObject currentAction, ArrayList<Champion> banChampions, int hoveredChampionId) {
    if(hasBanned || banChampions.isEmpty())
      return;
    for(Champion banChampion : banChampions) {
      if(!getBans(data).contains(banChampion.getId()) && !getPicks(data, localPlayerCellId, true).contains(banChampion.getId())) {
        boolean isHover = hoveredChampionId != banChampion.getId();
        if(!isHover)
          hasBanned = true;
        System.out.println("hover or banned");
        pickChampion(banChampion.getId(), currentAction.getInt("id"), isHover);
        break;
      }
    }
  }

  private ArrayList<Integer> getBans(JSONObject data) {
    ArrayList<Integer> bannedChampionIds = new ArrayList<>();
    JSONObject bans = data.getJSONObject("bans");
    JSONArray teamBans = bans.getJSONArray("myTeamBans");
    getTeamBans(bannedChampionIds, teamBans);
    JSONArray enemyBans = bans.getJSONArray("theirTeamBans");
    getTeamBans(bannedChampionIds, enemyBans);
    return bannedChampionIds;
  }

  private void getTeamBans(ArrayList<Integer> bannedChampionIds, JSONArray teamBans) {
    for(int i = 0; i < teamBans.length(); i++) {
      bannedChampionIds.add(teamBans.getInt(i));
    }
  }

  private ArrayList<Integer> getPicks(JSONObject data, int localPlayerCellId, boolean includesHover) {
    ArrayList<Integer> pickedChampionIds = new ArrayList<>();
    JSONArray team = data.getJSONArray("myTeam");
    getTeamPicks(localPlayerCellId, pickedChampionIds, team, includesHover);
    JSONArray enemyteam = data.getJSONArray("theirTeam");
    getTeamPicks(localPlayerCellId, pickedChampionIds, enemyteam, false);
    return pickedChampionIds;
  }

  private void getTeamPicks(int localPlayerCellId, ArrayList<Integer> pickedChampionIds, JSONArray team, boolean includesHover) {
    for(int i = 0; i < team.length(); i++) {
      JSONObject player = team.getJSONObject(i);
      if(player.getInt("cellId") != localPlayerCellId) {
        pickedChampionIds.add(player.getInt("championId"));
        if(includesHover)
          pickedChampionIds.add(player.getInt("championPickIntent"));
      }
    }
  }

  public void acceptGame() {
    System.out.println("isWaitAccept " + isWaitAccept);
    if(!isWaitAccept) {
      isWaitAccept = true;
      requestManager.sendRequest(POST, "/lol-matchmaking/v1/ready-check/accept");
      isWaitAccept = false;
    }
  }

  public void dodge() {
    String[] args = {"", "teambuilder-draft", "quitV2", ""};
    JSONArray jsonArgs = new JSONArray(Arrays.asList(args));
    requestManager.sendRequest(POST, "/lol-login/v1/session/invoke?destination=lcdsServiceProxy&method=call&args=" + URLEncoder.encode(jsonArgs.toString(), StandardCharsets.UTF_8));
    requestManager.sendRequest(POST, "/lol-login/v1/session/invoke?destination=lcdsServiceProxy&method=call&args=" + URLEncoder.encode(jsonArgs.toString(), StandardCharsets.UTF_8));
    requestManager.sendRequest(POST, "/lol-login/v1/session/invoke?destination=lcdsServiceProxy&method=call&args=" + URLEncoder.encode(jsonArgs.toString(), StandardCharsets.UTF_8));
    requestManager.sendRequest(POST, "/lol-login/v1/session/invoke?destination=lcdsServiceProxy&method=call&args=" + URLEncoder.encode(jsonArgs.toString(), StandardCharsets.UTF_8));
    //requestManager.sendRequest(POST, "/process-control/v1/process/quit");
  }

  public void createGame(String primaryRole, String secondaryRole) {
    JSONObject obj = new JSONObject();
    obj.put("queueId", "420");
    requestManager.sendRequest(POST, "/lol-lobby/v2/lobby", obj.toString());
    JSONObject obj2 = new JSONObject();
    obj2.put("firstPreference", primaryRole);
    obj2.put("secondPreference", secondaryRole);
    requestManager.sendRequest(PUT, "/lol-lobby/v1/lobby/members/localMember/position-preferences", obj2.toString());
  }

  public void pickRandomSkin() {
    String result = requestManager.sendRequest(GET, "/lol-champ-select/v1/skin-carousel-skins");
    JSONArray arr = new JSONArray(result);
    ArrayList<ArrayList<Integer>> skinList = new ArrayList<>();
    ArrayList<String> nameList = new ArrayList<>();
    for(int i = 0; i < arr.length(); i++) {
      JSONObject skin = arr.getJSONObject(i);
      if(skin.getBoolean("unlocked")) {
        ArrayList<Integer> idList = new ArrayList<Integer>();
        idList.add(skin.getInt("id"));
        JSONArray chromas = skin.getJSONArray("childSkins");
        for(int j = 0; j < chromas.length(); j++) {
          JSONObject chroma = chromas.getJSONObject(j);
          if(chroma.getBoolean("unlocked"))
            idList.add(chroma.getInt("id"));
        }
        nameList.add(skin.getString("name"));
        skinList.add(idList);
      }
    }
    Random rand = new Random();
    int rSkin = rand.nextInt(skinList.size());
    int rChroma = rand.nextInt(skinList.get(rSkin).size());
    System.out.println(nameList.get(rSkin) + " " + skinList.get(rChroma));

    JSONObject obj = new JSONObject();
    obj.put("selectedSkinId", skinList.get(rSkin).get(rChroma));
    requestManager.sendRequest(PATCH, "/lol-champ-select/v1/session/my-selection", obj.toString());
  }

  public ArrayList<Participant> getParticipants() {
    String result = requestManager.sendRequest(RIOT, GET, "/chat/v5/participants");
    JSONObject obj = new JSONObject(result);
    JSONArray arr = obj.getJSONArray("participants");
    ArrayList<Participant> summonersTemp = new ArrayList<>();
    for(int i = 0; i < arr.length(); i++) {
      JSONObject summoner = arr.getJSONObject(i);
      if(!summoner.isNull("activePlatform")) {
        summonersTemp.add(new Participant(summoner.getString("game_name"), summoner.getString("game_tag")));
      }
    }

    if(summonersTemp.size() >= 5)
      summoners = new ArrayList<>(summonersTemp.subList(summonersTemp.size() - 5, summonersTemp.size()));
    else
      summoners = summonersTemp;
    return summoners;
  }

  public void pickChampion(int championId, int actionId, boolean isHover) {
    JSONObject payload = new JSONObject();
    payload.put("championId", championId);
    if(!isHover)
      payload.put("completed", true);
    requestManager.sendRequest(PATCH, "/lol-champ-select/v1/session/actions/" + actionId, payload.toString());
  }

  public void setAutoAccept(boolean isSelected) {
    autoSub(isSelected, "OnJsonApiEvent_lol-matchmaking_v1_search", "autoAccept");
    isAutoAccept = isSelected;
  }

  public void setAutoAcceptSwap(boolean isSelected) {
    autoSub(isSelected, "OnJsonApiEvent_lol-champ-select_v1_session", "autoAcceptSwap");
    isAutoAcceptSwap = isSelected;
  }

  public void setAutoChampSelect(boolean isSelected) {
    autoSub(isSelected, "OnJsonApiEvent_lol-champ-select_v1_session", "autoChampSelect");
    isAutoChampSelect = isSelected;
  }

  private void subToAll() {
    String event = "OnJsonApiEvent";
    if(wsClient == null) {
      if(!isConnectingToWS) {
        awaitingSubEvents.clear();
        connectWebsocket();
      }
      if(!awaitingSubEvents.contains(event))
        awaitingSubEvents.add(event);
    } else {
      wsClient.send("[5, \"" + event + "\"]");
    }
  }

  private void autoSub(boolean isSelected, String event, String type) {
    if(isSelected) {
      if(wsClient == null) {
        if(!isConnectingToWS) {
          awaitingSubEvents.clear();
          connectWebsocket();
        }
        if(!awaitingSubEvents.contains(event))
          awaitingSubEvents.add(event);
      } else {
        wsClient.send("[5, \"" + event + "\"]");
      }
    } else if(wsClient != null) {
      if(!isAutoAccept && !isAutoAcceptSwap && !isAutoChampSelect)
        disconnectWebsocket();
      else if((type.equals("autoAcceptSwap") && !isAutoChampSelect) || (type.equals("autoChampSelect") && !isAutoAcceptSwap))
        wsClient.send("[6, \"" + event + "\"]");
    }
  }

  public void swapPickOrder(int actionId) {
    requestManager.sendRequest(POST, "/lol-champ-select/v1/session/pick-order-swaps/" + actionId + "/accept");
  }

  public void openInBrowser(String id) {
    if(summoners == null)
      return;
    Website website = Websites.getWebsite(id);
    String url = MessageFormat.format(website.getUrl(), credentials.getRegion().toLowerCase()) + URLEncoder.encode(getURLNames(website.needDash()), StandardCharsets.UTF_8);

    if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop().browse(new URI(url));
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  private String getURLNames(boolean needDash) {
    String summonersStr = "";
    for(int i = 0; i < summoners.size(); i++) {
      summonersStr += needDash ? summoners.get(i).getCompleteNameDash() : summoners.get(i).getCompleteNameHash();
      if(i < summoners.size() - 1)
        summonersStr += ",";
    }
    return summonersStr;
  }

  private static class Websites {
    private static final Website[] websites = {
      new Website("OPGG", "https://www.op.gg/multisearch/{0}?summoners=", false),
      new Website("Poro", "https://porofessor.gg/pregame/{0}/", true),
      new Website("UGG", "https://u.gg/multisearch?region={0}1&summoners=", true),
      new Website("Deeplol", "https://www.deeplol.gg/multi/{0}/", false),
    };

    public static Website getWebsite(String id) {
      for(Website w : websites) {
        if(w.getId().equals(id))
          return w;
      }
      return null;
    }
  }

}
