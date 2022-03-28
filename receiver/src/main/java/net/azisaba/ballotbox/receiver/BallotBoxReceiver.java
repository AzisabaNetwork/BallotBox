package net.azisaba.ballotbox.receiver;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import java.io.File;
import java.util.Iterator;
import lombok.Getter;
import net.azisaba.ballotbox.receiver.config.BallotBoxReceiverConfig;
import net.azisaba.ballotbox.receiver.data.VoteIndexHandler;
import net.azisaba.ballotbox.receiver.fetcher.VoteFetcher;
import net.azisaba.ballotbox.receiver.fetcher.impl.MySQLVoteFetcher;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class BallotBoxReceiver extends JavaPlugin {

  private BallotBoxReceiverConfig pluginConfig;
  private VoteFetcher voteFetcher;
  private VoteIndexHandler voteIndexHandler;

  @Override
  public void onEnable() {
    pluginConfig = new BallotBoxReceiverConfig(this).load();

    voteIndexHandler = new VoteIndexHandler(this);
    long index = voteIndexHandler.load();

    if (!setVoteFetcher(index)) {
      getLogger().severe("Failed to set vote fetcher. The plugin cannot fetch vote.");
    }

    runRepeatTasks();

    Bukkit.getLogger().info(getName() + " enabled.");
  }

  @Override
  public void onDisable() {
    voteFetcher.onClose();
    Bukkit.getLogger().info(getName() + " disabled.");
  }

  public final File getBallotBoxDataFolder() {
    return new File(getDataFolder().getParentFile(), "BallotBoxReceiver");
  }

  private boolean setVoteFetcher(long index) {
    if (pluginConfig.getSqlConnectionInfo() == null) {
      return false;
    }

    MySQLVoteFetcher mySqlVoteFetcher = new MySQLVoteFetcher(
        pluginConfig.getSqlConnectionInfo(), index,
        pluginConfig.getVoteFetchOption());

    if (!mySqlVoteFetcher.connect()) {
      getLogger().warning("Failed to connect to SQL server.");
      return false;
    }

    mySqlVoteFetcher.setSaveCurrentIndexFunction((newIndex) -> voteIndexHandler.save(newIndex));
    voteFetcher = mySqlVoteFetcher;
    return true;
  }

  private void runRepeatTasks() {
    Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
      if (voteFetcher != null) {
        Iterator<Vote> votes = voteFetcher.fetchNewVotes();
        while (votes.hasNext()) {
          Vote vote = votes.next();
          getLogger().info(
              "Received vote from: " + vote.getUsername() + " via " + vote.getServiceName());

          Bukkit.getScheduler()
              .runTask(this, () -> Bukkit.getPluginManager().callEvent(new VotifierEvent(vote)));
        }
      }
    }, 100L, (long) (pluginConfig.getFetchVoteIntervalSeconds() * 20d));

    Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
      if (voteFetcher instanceof MySQLVoteFetcher) {
        long index = ((MySQLVoteFetcher) voteFetcher).getCurrentIndex();
        voteIndexHandler.save(index);
      }
    }, 20L * 60L, 20L * 60L);
  }
}
