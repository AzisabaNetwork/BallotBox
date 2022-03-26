package net.azisaba.ballotbox.server;

import net.azisaba.ballotbox.server.config.ServerConfigInstance;
import net.azisaba.ballotbox.server.connection.ConnectionListener;
import net.azisaba.ballotbox.server.util.Versioning;
import net.azisaba.ballotbox.server.config.ServerConfigInstance;
import net.azisaba.ballotbox.server.connection.ConnectionListener;
import net.azisaba.ballotbox.server.util.Versioning;
import net.azisaba.ballotbox.server.config.ServerConfigInstance;
import net.azisaba.ballotbox.server.connection.ConnectionListener;
import net.azisaba.ballotbox.server.util.Versioning;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ServerInstance {
  private static final Logger LOGGER = LogManager.getLogger();
  private static ServerInstance instance;
  private final AtomicLong workerId = new AtomicLong();
  private final ExecutorService worker =
      Executors.newCachedThreadPool(
          r ->
              new Thread(
                  () -> {
                    LOGGER.debug("Thread {} started", Thread.currentThread().getName());
                    try {
                      r.run();
                    } catch (Throwable t) {
                      LOGGER.warn("Thread {} died", Thread.currentThread().getName(), t);
                      if (t instanceof Error) throw t;
                    } finally {
                      LOGGER.debug("Thread {} shutdown", Thread.currentThread().getName());
                    }
                  },
                  "Worker-Main-" + workerId.incrementAndGet()));
  private final Versioning version = new Versioning();
  private ConnectionListener connectionListener = new ConnectionListener();
  private boolean stopping = false;

  public static ServerInstance getInstance() {
    if (instance == null) {
      throw new IllegalStateException("ServerInstance is not initialized.");
    }
    return instance;
  }

  public ServerInstance() {
    instance = this;
  }

  @NotNull
  public Versioning getVersion() {
    return version;
  }

  public void start() {
    long start = System.currentTimeMillis();
    LOGGER.info("Starting {} Server version {}", version.getName(), version.getVersion());
    reloadConfig().join();
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "Server Shutdown Thread"));
    startInfinitySleepThread();
    LOGGER.info("Started in {}ms", System.currentTimeMillis() - start);
  }

  public void stop() {
    if (stopping) {
      LOGGER.debug("stop() called while the server is already stopping!", new Throwable());
      return;
    }
    LOGGER.info("Stopping!");
    stopping = true;
    fullyCloseListeners();
    LOGGER.info("Shutting down executor");
    worker.shutdownNow();
    try {
      //noinspection ResultOfMethodCallIgnored
      worker.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    LOGGER.info("Goodbye!");
  }

  @NotNull
  public CompletableFuture<Void> reloadConfig() {
    return CompletableFuture.runAsync(
        () -> {
          closeListeners();
          LOGGER.info("Loading config");
          try {
            ServerConfigInstance.init();
          } catch (IOException e) {
            LOGGER.fatal("Failed to load config", e);
            throw new RuntimeException(e);
          }
          LOGGER.info("Initializing listeners");
          if (connectionListener == null) {
            connectionListener = new ConnectionListener();
          }
          connectionListener.start();
        },
        worker);
  }

  public void closeListeners() {
    if (connectionListener != null) {
      LOGGER.info("Closing listeners");
      connectionListener.closeFutures();
    }
  }

  public void fullyCloseListeners() {
    if (connectionListener != null) {
      LOGGER.info("Closing listeners");
      connectionListener.close();
      connectionListener = null;
    }
  }

  private void startInfinitySleepThread() {
    worker.execute(
        () -> {
          while (!stopping) {
            try {
              //noinspection BusyWait
              Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException e) {
              LOGGER.warn("Wait thread interrupted");
              Thread.currentThread().interrupt();
            }
          }
        });
  }
}
