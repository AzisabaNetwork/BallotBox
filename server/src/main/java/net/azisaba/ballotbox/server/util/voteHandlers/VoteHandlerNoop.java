package net.azisaba.ballotbox.server.util.voteHandlers;

import com.vexsoftware.votifier.model.Vote;
import net.azisaba.ballotbox.server.protocol.VotifierSession;
import net.azisaba.ballotbox.server.util.VoteHandler;
import net.azisaba.ballotbox.server.protocol.VotifierSession;
import net.azisaba.ballotbox.server.util.VoteHandler;
import net.azisaba.ballotbox.server.protocol.VotifierSession;
import net.azisaba.ballotbox.server.util.VoteHandler;
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
