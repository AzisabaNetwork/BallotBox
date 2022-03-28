package net.azisaba.ballotbox.server.util.voteHandlers;

import com.vexsoftware.votifier.model.Vote;
import net.azisaba.ballotbox.server.protocol.VotifierSession;
import net.azisaba.ballotbox.server.util.SQLThrowableConsumer;
import net.azisaba.ballotbox.server.yaml.YamlConfiguration;
import net.azisaba.ballotbox.server.yaml.YamlObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.mariadb.jdbc.Driver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class VoteHandlerMariaDB extends VoteHandlerLog {
  private static final Logger LOGGER = LogManager.getLogger();
  private final Driver driver = new Driver();
  private final Properties properties = new Properties();
  private String url = null;

  public VoteHandlerMariaDB() {
    loadConfig();
    try {
      useConnection(
          connection -> {
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `votes` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT,"
                    + "`username` VARCHAR(32) NOT NULL,"
                    + "`service` VARCHAR(128) NOT NULL,"
                    + "`address` VARCHAR(128) NOT NULL,"
                    + "`timestamp` VARCHAR(256) NOT NULL,"
                    + "`version` INT NOT NULL DEFAULT -1,"
                    + "PRIMARY KEY (`id`)"
                    + ")");
            statement.close();
          });
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void loadConfig() {
    YamlObject config;
    try {
      config = new YamlConfiguration("mariadb.yml").asObject();
    } catch (FileNotFoundException e) {
      LOGGER.info("mariadb.yml not found. Using default config.");
      config = new YamlObject();
      config.set("useSSL", true);
      config.set("verifyServerCertificate", true);
      config.set("host", "localhost");
      config.set("name", "ballot_box");
      config.set("user", "ballot_box");
      config.set("password", "ballot_box");
      try {
        config.save("mariadb.yml");
        LOGGER.info("Saved mariadb.yml. Adjust values if needed.");
      } catch (IOException ex) {
        LOGGER.warn("Failed to save mariadb.yml", ex);
      }
    }
    properties.setProperty("useSSL", config.getString("useSSL", "true"));
    properties.setProperty(
        "verifyServerCertificate", config.getString("verifyServerCertificate", "true"));
    properties.setProperty("user", config.getString("user", "ballot_box"));
    properties.setProperty("password", config.getString("password", "ballot_box"));
    url =
        "jdbc:mariadb://"
            + config.getString("host", "localhost")
            + "/"
            + config.getString("name", "ballot_box");
  }

  @NotNull
  public Connection getConnection() throws SQLException {
    if (url == null) throw new IllegalArgumentException("url is null");
    Connection connection = driver.connect(url, properties);
    if (connection == null) throw new SQLException("connection is null (invalid configuration?)");
    return connection;
  }

  public void useConnection(@NotNull SQLThrowableConsumer<Connection> action) throws SQLException {
    try (Connection connection = getConnection()) {
      action.accept(connection);
    }
  }

  @Override
  public void onVoteReceived(
      @NotNull Vote vote,
      VotifierSession.@NotNull ProtocolVersion version,
      @NotNull String remoteAddress) {
    super.onVoteReceived(vote, version, remoteAddress);
    try {
      useConnection(
          connection -> {
            PreparedStatement statement =
                connection.prepareStatement(
                    "INSERT INTO `votes` (`username`, `service`, `address`, `timestamp`, `version`) VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, vote.getUsername());
            statement.setString(2, vote.getServiceName());
            statement.setString(3, remoteAddress);
            statement.setString(4, vote.getTimeStamp());
            statement.setInt(5, version.id);
            statement.execute();
          });
    } catch (SQLException e) {
      LOGGER.warn("Failed to insert into database", e);
      LOGGER.warn("Vote record: {}", vote);
    }
  }
}
