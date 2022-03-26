package net.azisaba.ballotbox.server.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class Versioning {
  private static final Logger LOGGER = LogManager.getLogger();
  private final String name;
  private final String version;

  public Versioning() {
    String n = "BallotBox";
    String v = "unknown";
    try {
      InputStream in = Versioning.class.getResourceAsStream("/version.properties");
      Properties properties = new Properties();
      properties.load(in);
      n = properties.getProperty("name");
      v = properties.getProperty("version");
      if (n == null || "@NAME@".equals(n)) n = "BallotBox";
      if (v == null || "@VERSION@".equals(v)) v = "unknown";
    } catch (IOException e) {
      LOGGER.warn("Failed to read version.properties", e);
    }
    name = n;
    version = v;
  }

  @NotNull
  public String getName() {
    return Objects.requireNonNull(name);
  }

  @NotNull
  public String getVersion() {
    return Objects.requireNonNull(version);
  }
}
