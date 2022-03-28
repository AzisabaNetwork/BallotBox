package net.azisaba.ballotbox.receiver.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.ballotbox.receiver.BallotBoxReceiver;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter
@RequiredArgsConstructor
public class BallotBoxReceiverConfig {

  private final BallotBoxReceiver plugin;

  private SQLConnectionInfo sqlConnectionInfo;
  private VoteFetchOption voteFetchOption;

  private double fetchVoteIntervalSeconds;

  public BallotBoxReceiverConfig load() {
    try {
      saveDefaultConfig();
    } catch (IOException e) {
      e.printStackTrace();
    }

    File file = new File(plugin.getBallotBoxDataFolder(), "config.yml");
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    String sqlHostName = config.getString("sql.hostname");
    int sqlPort = config.getInt("sql.port");
    String sqlDatabase = config.getString("sql.database");
    String sqlUsername = config.getString("sql.username");
    String sqlPassword = config.getString("sql.password");

    if (sqlHostName == null || sqlPort <= 0 || sqlDatabase == null || sqlUsername == null
        || sqlPassword == null) {
      plugin.getLogger().severe("Invalid SQL configuration.");
      return this;
    }

    if (sqlUsername.equals("username") && sqlPassword.equals("password")) {
      plugin.getLogger()
          .warning("SQL username and password are not set. Disabling SQL connection.");
      return this;
    }

    sqlConnectionInfo = new SQLConnectionInfo(sqlHostName, sqlPort, sqlUsername, sqlPassword,
        sqlDatabase);

    int voteExpireDays = config.getInt("vote-expire-days", 7);
    int fetchMaxVotesPerInterval = config.getInt("fetch-max-votes-per-interval", 100);
    voteFetchOption = new VoteFetchOption(voteExpireDays, fetchMaxVotesPerInterval);

    fetchVoteIntervalSeconds = config.getDouble("fetch-vote-interval-seconds", 2);
    if (fetchVoteIntervalSeconds < 0.05) {
      fetchVoteIntervalSeconds = 0.05;
      plugin.getLogger().warning("fetch-vote-interval-seconds is too small. Setting to 0.05.");
    }

    return this;
  }

  public void saveDefaultConfig() throws IOException {
    File file = new File(plugin.getBallotBoxDataFolder(), "config.yml");
    if (file.exists()) {
      return;
    }

    InputStream is = getClass().getClassLoader().getResourceAsStream(file.getName());
    if (is == null) {
      throw new IllegalStateException("Failed to load config.yml from resource.");
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    Files.createDirectories(file.getParentFile().toPath());

    byte[] byteStr =
        reader
            .lines()
            .collect(Collectors.joining(System.lineSeparator()))
            .getBytes(StandardCharsets.UTF_8);

    Files.write(file.toPath(), byteStr);
  }
}
