package net.azisaba.ballotBox.server.util.voteHandlers;

import com.vexsoftware.votifier.model.Vote;
import net.azisaba.ballotBox.server.protocol.VotifierSession;
import net.azisaba.ballotBox.server.util.VoteHandler;
import org.jetbrains.annotations.NotNull;

public class VoteHandlerNoop implements VoteHandler {
  @Override
  public void onVoteReceived(
      @NotNull Vote vote,
      VotifierSession.@NotNull ProtocolVersion version,
      @NotNull String remoteAddress) {
    // noop
  }

  @Override
  public void onError(
      @NotNull Throwable cause, boolean hasCompletedVote, @NotNull String remoteAddress) {
    // noop
  }
}
