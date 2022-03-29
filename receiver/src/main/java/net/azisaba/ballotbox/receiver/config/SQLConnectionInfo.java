package net.azisaba.ballotbox.receiver.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SQLConnectionInfo {

  private String hostname;
  private int port;
  private String username;
  private String password;
  private String database;

}
