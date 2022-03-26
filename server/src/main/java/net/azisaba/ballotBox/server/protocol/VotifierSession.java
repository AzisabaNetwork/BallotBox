package net.azisaba.ballotBox.server.protocol;

import io.netty.util.AttributeKey;
import net.azisaba.ballotBox.server.util.Util;

public class VotifierSession {
  public static final AttributeKey<VotifierSession> KEY = AttributeKey.valueOf("votifier_session");
  private ProtocolVersion version = ProtocolVersion.UNKNOWN;
  private final String challenge;
  private boolean hasCompletedVote = false;

  public VotifierSession() {
    challenge = Util.generateNewToken();
  }

  public void setVersion(ProtocolVersion version) {
    if (this.version != ProtocolVersion.UNKNOWN)
      throw new IllegalStateException("Protocol version already switched");

    this.version = version;
  }

  public ProtocolVersion getVersion() {
    return version;
  }

  public String getChallenge() {
    return challenge;
  }

  public void completeVote() {
    if (hasCompletedVote) throw new IllegalStateException("Protocol completed vote twice!");

    hasCompletedVote = true;
  }

  public boolean hasCompletedVote() {
    return hasCompletedVote;
  }

  public enum ProtocolVersion {
    UNKNOWN(-1, "unknown"),
    ONE(1, "protocol v1"),
    TWO(2, "protocol v2"),
    TEST(-1, "test");

    public final int id;
    public final String humanReadable;

    ProtocolVersion(int id, String hr) {
      this.id = id;
      this.humanReadable = hr;
    }
  }
}
