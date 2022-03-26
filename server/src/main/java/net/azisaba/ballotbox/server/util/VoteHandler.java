package net.azisaba.ballotbox.server.util;

import com.vexsoftware.votifier.model.Vote;
import net.azisaba.ballotbox.server.protocol.VotifierSession;
import org.jetbrains.annotations.NotNull;

public interface VoteHandler {
  void onVoteReceived(
      @NotNull Vote vote,
      @NotNull VotifierSession.ProtocolVersion version,
      @NotNull String remoteAddress);

  void onError(@NotNull Throwable cause, boolean hasCompletedVote, @NotNull String remoteAddress);
}
