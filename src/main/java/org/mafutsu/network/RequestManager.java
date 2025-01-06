package org.mafutsu.network;

import org.mafutsu.util.ClientType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;

import static org.mafutsu.util.ClientType.LCU;
import static org.mafutsu.util.ClientType.RIOT;
import static org.mafutsu.util.Constants.*;

public class RequestManager {
  private final String HOST = "https://127.0.0.1:";
  private Credentials credentials;

  public RequestManager(Credentials credentials) {
    this.credentials = credentials;
  }

  public static SSLContext bypassCertificate() {
    try {
      TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          public void checkClientTrusted(X509Certificate[] certs, String authType) {
          }

          public void checkServerTrusted(X509Certificate[] certs, String authType) {
          }
        }
      };

      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      return sslContext;
    } catch(Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String sendRequest(String type, String endpoint) {
    return sendRequest(LCU, type, endpoint, null);
  }

  public String sendRequest(ClientType client, String type, String endpoint) {
    return sendRequest(client, type, endpoint, null);
  }

  public String sendRequest(String type, String endpoint, String payload) {
    return sendRequest(LCU, type, endpoint, payload);
  }

  public String sendRequest(ClientType client, String type, String endpoint, String payload) {
    if(credentials == null)
      return null;
    System.out.println(type + " " + endpoint + " " + payload);
    String url = HOST + credentials.getLcuPort() + endpoint;
    String auth = credentials.getLcuAuth();
    if(client == RIOT) {
      url = HOST + credentials.getRiotPort() + endpoint;
      auth = credentials.getRiotAuth();
    }

    HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
    if(type.equals(POST) || type.equals(PUT) || type.equals(PATCH) || type.equals(DELETE)) {
      if(payload != null && !payload.isEmpty()) {
        bodyPublisher = HttpRequest.BodyPublishers.ofString(payload);
      }
    }

    try {
      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        //.header("User-Agent", "LeagueOfLegendsClient/")
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .header("Authorization", "Basic " + auth)
        .method(type, bodyPublisher)
        .build();
      HttpClient httpClient = HttpClient.newBuilder().sslContext(bypassCertificate()).build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println(response.body());
      return response.body();
    } catch(Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String sendOtherRequest(String url) {
    HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();

    try {
      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .method(GET, bodyPublisher)
        .build();
      HttpClient httpClient = HttpClient.newBuilder().sslContext(bypassCertificate()).build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return response.body();
    } catch(Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
