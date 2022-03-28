package net.azisaba.ballotbox.receiver.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VoteFetchOption {

  private final int expireDays;
  private final int maxVotesPerInterval;

}
