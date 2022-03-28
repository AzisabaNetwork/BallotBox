package net.azisaba.ballotbox.receiver.fetcher.impl;

import com.vexsoftware.votifier.model.Vote;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import net.azisaba.ballotbox.receiver.config.SQLConnectionInfo;
import net.azisaba.ballotbox.receiver.config.VoteFetchOption;
import net.azisaba.ballotbox.receiver.fetcher.VoteFetcher;

public class MySQLVoteFetcher implements VoteFetcher {

  private final SQLConnectionInfo sqlConnectionInfo;

  private final VoteFetchOption voteFetchOption;

  @Getter
  private long currentIndex;
  @Setter
  private Consumer<Long> saveCurrentIndexFunction;

  @Getter
  private boolean connected = false;
  private Connection connection;

  private final ReentrantLock lock = new ReentrantLock();

  public MySQLVoteFetcher(SQLConnectionInfo sqlConnectionInfo, long currentIndex,
      VoteFetchOption voteFetchOption) {
    this.sqlConnectionInfo = sqlConnectionInfo;
    this.currentIndex = currentIndex;
    this.voteFetchOption = voteFetchOption;
  }

  public boolean connect() {
    try {
      connection = DriverManager.getConnection(
          "jdbc:mysql://" + sqlConnectionInfo.getHostname() + ":" + sqlConnectionInfo.getPort()
              + "/" + sqlConnectionInfo.getDatabase(),
          sqlConnectionInfo.getUsername(), sqlConnectionInfo.getPassword());
      connected = true;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return isConnected();
  }

  @Override
  public Iterator<Vote> fetchNewVotes() {
    if (!isConnected()) {
      throw new IllegalStateException("Not connected to database");
    }

    lock.lock();

    try {
      try {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, voteFetchOption.getExpireDays() * -1);

        PreparedStatement statement = connection.prepareStatement(
            "SELECT * FROM `votes` WHERE id > ? AND timestamp > ? ORDER BY `id` ASC LIMIT ?");
        statement.setLong(1, currentIndex);
        statement.setLong(2, cal.getTime().getTime() / 1000L);
        statement.setInt(3, voteFetchOption.getMaxVotesPerInterval());

        ResultSet resultSet = statement.executeQuery();

        List<Vote> votes = new ArrayList<>();
        while (resultSet.next()) {
          long id = resultSet.getLong("id");
          String serviceName = resultSet.getString("service");
          String username = resultSet.getString("username");
          String address = resultSet.getString("address");
          String timeStamp = resultSet.getString("timestamp");

          Vote vote = new Vote(serviceName, username, address, timeStamp);
          votes.add(vote);

          currentIndex = Long.max(currentIndex, id);
        }

        return votes.iterator();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } finally {
      lock.unlock();
    }

    return Collections.emptyIterator();
  }

  @Override
  public void onClose() {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    if (saveCurrentIndexFunction != null) {
      saveCurrentIndexFunction.accept(currentIndex);
    }
  }
}
