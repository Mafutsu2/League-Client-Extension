package org.mafutsu.network;

public interface IWSClient {
  void onReceiveMessage(String message);
  void onOpenConnection();
  void onCloseConnection();
}
