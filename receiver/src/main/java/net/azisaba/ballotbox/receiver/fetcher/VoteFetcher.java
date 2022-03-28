package net.azisaba.ballotbox.receiver.fetcher;

import com.vexsoftware.votifier.model.Vote;
import java.util.Iterator;

public interface VoteFetcher {

  /**
   * Retrieve the latest votes that has not yet been processed
   *
   * @return Returns the Iterator of the latest Vote
   */
  Iterator<Vote> fetchNewVotes();

  /**
   * Method called when the plugin exits. It is mainly used to disconnect from the database.
   */
  default void onClose() {
  }
}
