package net.azisaba.ballotBox.server.util.voteHandlers;

import com.vexsoftware.votifier.model.Vote;
import net.azisaba.ballotBox.server.protocol.VotifierSession;
import net.azisaba.ballotBox.server.util.VoteHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class VoteHandlerLog implements VoteHandler {
  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public void onVoteReceived(
      @NotNull Vote vote,
      VotifierSession.@NotNull ProtocolVersion version,
      @NotNull String remoteAddress) {
    LOGGER.info("Received {} vote from {}: {}", version.humanReadable, remoteAddress, vote);
  }

  @Override
  public void onError(
      @NotNull Throwable cause, boolean hasCompletedVote, @NotNull String remoteAddress) {
    LOGGER.warn(
        "Failed to handle vote from {} (hasCompletedVote: {}): {}",
        remoteAddress,
        hasCompletedVote,
        cause);
  }
}
