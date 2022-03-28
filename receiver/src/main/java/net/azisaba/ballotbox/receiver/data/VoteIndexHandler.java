package net.azisaba.ballotbox.receiver.data;

import java.io.File;
import java.io.IOException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.ballotbox.receiver.BallotBoxReceiver;
import org.bukkit.configuration.file.YamlConfiguration;

@RequiredArgsConstructor
public class VoteIndexHandler {

  public static final String FILE_NAME = "vote-index.yml";

  private final BallotBoxReceiver plugin;

  @Getter
  private long index;

  public long load() {
    File file = new File(plugin.getBallotBoxDataFolder(), FILE_NAME);
    YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

    index = conf.getLong("index", -1);
    return index;
  }

  public void save(long newIndex) {
    File file = new File(plugin.getBallotBoxDataFolder(), FILE_NAME);
    YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

    conf.set("index", newIndex);

    try {
      conf.save(file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
