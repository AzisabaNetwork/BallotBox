package net.azisaba.ballotBox.server.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;

public class Util {
  private static final SecureRandom RANDOM = new SecureRandom();

  public static @NotNull String generateNewToken() {
    return new BigInteger(130, RANDOM).toString(32);
  }

  @Contract("_ -> new")
  public static @NotNull Key createKeyFromToken(@NotNull String token) {
    return new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
  }
}
