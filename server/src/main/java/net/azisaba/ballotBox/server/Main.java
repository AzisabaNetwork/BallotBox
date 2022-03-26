package net.azisaba.ballotBox.server;

import net.azisaba.ballotBox.server.util.SignalUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
  private static final Logger LOGGER = LogManager.getLogger();

  public static void main(String[] args) {
    try {
      SignalUtil.registerAll();
      new ServerInstance().start();
    } catch (Throwable throwable) {
      LOGGER.fatal("Failed to start BallotBox Server", throwable);
    }
  }
}
