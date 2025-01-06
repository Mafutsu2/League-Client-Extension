package org.mafutsu.network;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.Map;

public class WSClient extends WebSocketClient {
  private IWSClient callback;

  //Subscribing to event = 5
  //Unsubscribing to event = 6
  //Receiving event = 8
  public WSClient(URI serverUri, Draft draft) {
    super(serverUri, draft);
  }

  public WSClient(URI serverURI) {
    super(serverURI);
  }

  public WSClient(URI serverUri, Map<String, String> httpHeaders) {
    super(serverUri, httpHeaders);
  }

  public WSClient(URI serverUri, Map<String, String> httpHeaders, IWSClient callback) {
    this(serverUri, httpHeaders);
    this.callback = callback;
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    System.out.println("opened connection");
    callback.onOpenConnection();
  }

  @Override
  public void onMessage(String message) {
    if(message != null && !message.isEmpty()) {
      JSONArray arr = new JSONArray(message);
      JSONObject data = (JSONObject) arr.get(2);
      //if(data.get("uri").equals("/lol-challenges/v1/summary-player-data/player/e22eafa3-97ea-5038-a874-282fd1daac1c"))
      System.out.println(message);
    }

    if(!message.isEmpty())
      callback.onReceiveMessage(message);
  }


  @Override
  public void onClose(int code, String reason, boolean remote) {
    // The close codes are documented in class org.java_websocket.framing.CloseFrame
    System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    callback.onCloseConnection();
  }

  @Override
  public void onError(Exception ex) {
    ex.printStackTrace();
    // if the error is fatal then onClose will be called additionally
  }

}
