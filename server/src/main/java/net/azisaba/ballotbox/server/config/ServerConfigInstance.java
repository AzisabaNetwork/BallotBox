package net.azisaba.ballotbox.server.config;

import com.vexsoftware.votifier.net.protocol.v1crypto.RSAIO;
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAKeygen;
import io.netty.channel.epoll.Epoll;
import net.azisaba.ballotbox.server.util.Util;
import net.azisaba.ballotbox.server.util.VoteHandler;
import net.azisaba.ballotbox.server.yaml.YamlConfiguration;
import net.azisaba.ballotbox.server.yaml.YamlObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.Key;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

public class ServerConfigInstance {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final List<Field> FIELDS = new ArrayList<>();
  private static YamlObject config;

  public static void init() throws IOException, ClassCastException {
    reset();
    File file = new File("./config.yml");
    boolean shouldSave = !file.exists();
    if (!file.exists() && !file.createNewFile()) {
      LOGGER.warn("Failed to create " + file.getAbsolutePath());
    }
    config = new YamlConfiguration(file).asObject();
    for (Field field : FIELDS) {
      String serializedName = field.getAnnotation(SerializedName.class).value();
      try {
        Object def = field.get(null);
        field.set(null, config.get(serializedName, def));
      } catch (ReflectiveOperationException ex) {
        LOGGER.warn(
            "Failed to get or set field '{}' (serialized name: {})",
            field.getName(),
            serializedName,
            ex);
      }
    }
    // token
    String token = config.getString("token");
    if (token == null || token.isEmpty()) {
      LOGGER.info("Generating new token...");
      token = Util.generateNewToken();
      config.set("token", token);
      LOGGER.info("Generated new token: {}", token);
      shouldSave = true;
    }
    ServerConfigInstance.token = Util.createKeyFromToken(token);
    // keypair
    try {
      keyPair = RSAIO.load(new File("./rsa"));
    } catch (Exception e) {
      LOGGER.warn("Failed to load keypair", e);
      try {
        LOGGER.info("Generating keypair");
        keyPair = RSAKeygen.generate(2048);
        shouldSave = true;
      } catch (Exception ex) {
        LOGGER.fatal("Failed to generate keypair", ex);
        throw new RuntimeException(ex);
      }
    }
    if (shouldSave) {
      try {
        RSAIO.save(new File("./rsa"), keyPair);
      } catch (Exception e) {
        LOGGER.error("Failed to save keypair", e);
      }
      save();
      LOGGER.info("Saved config.yml");
    }
  }

  public static void save() throws IOException {
    if (config == null) throw new RuntimeException("#init was not called");
    for (Field field : FIELDS) {
      String serializedName = field.getAnnotation(SerializedName.class).value();
      try {
        Object value = field.get(null);
        config.setNullable(serializedName, value);
      } catch (ReflectiveOperationException ex) {
        LOGGER.warn(
            "Failed to get field '{}' (serialized name: {})", field.getName(), serializedName, ex);
      }
    }
    config.save(new File("./config.yml"));
  }

  public static void reset() {
    epoll = true;
  }

  public static Key token = null; // v2

  public static KeyPair keyPair = null; // v1

  // (Builtin) Options:
  // - net.azisaba.ballotbox.server.util.voteHandlers.VoteHandlerNoop
  // - net.azisaba.ballotbox.server.util.voteHandlers.VoteHandlerLog
  // - net.azisaba.ballotbox.server.util.voteHandlers.VoteHandlerMariaDB
  @Language(value = "JAVA", prefix = "import ", suffix = ";")
  @SerializedName("voteHandler")
  public static String voteHandler =
      "net.azisaba.ballotbox.server.util.voteHandlers.VoteHandlerLog";

  @SerializedName("listenHost")
  public static String listenHost = "0.0.0.0";

  @SerializedName("listenPort")
  public static int listenPort = 8192;

  @SerializedName("allowV1")
  public static boolean allowV1 = true;

  @SerializedName("debug")
  public static boolean debug = false;

  @SerializedName("epoll")
  public static boolean epoll = true;

  public static boolean isEpoll() {
    return epoll && Epoll.isAvailable();
  }

  public static @NotNull VoteHandler createVoteHandler() {
    try {
      Class<?> clazz = Class.forName(voteHandler);
      return (VoteHandler) clazz.getConstructor().newInstance();
    } catch (ClassCastException e) {
      throw new RuntimeException(voteHandler + " is not a VoteHandler", e);
    } catch (Exception e) {
      throw new RuntimeException("Could not create vote handler", e);
    }
  }

  static {
    for (Field field : ServerConfigInstance.class.getFields()) {
      if (!Modifier.isPublic(field.getModifiers())) continue;
      if (!Modifier.isStatic(field.getModifiers())) continue;
      if (field.isSynthetic()) continue;
      SerializedName serializedNameAnnotation = field.getAnnotation(SerializedName.class);
      if (serializedNameAnnotation == null) continue;
      String serializedName = serializedNameAnnotation.value();
      if (serializedName.equals("")) continue;
      FIELDS.add(field);
    }
  }
}
