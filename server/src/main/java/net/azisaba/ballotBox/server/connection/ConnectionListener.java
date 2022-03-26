package net.azisaba.ballotBox.server.connection;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.azisaba.ballotBox.server.config.ServerConfigInstance;
import net.azisaba.ballotBox.server.protocol.VoteInboundHandler;
import net.azisaba.ballotBox.server.protocol.VotifierGreetingHandler;
import net.azisaba.ballotBox.server.protocol.VotifierProtocolDifferentiator;
import net.azisaba.ballotBox.server.protocol.VotifierSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionListener {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final AtomicLong BOSS_THREAD_COUNT = new AtomicLong();
  private static final AtomicLong WORKER_THREAD_COUNT = new AtomicLong();
  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;
  private final List<ChannelFuture> futures = new ArrayList<>();

  public ConnectionListener() {
    if (ServerConfigInstance.isEpoll()) {
      bossGroup =
          new EpollEventLoopGroup(
              r ->
                  new Thread(
                      r, "Netty Epoll IO Boss Thread #" + BOSS_THREAD_COUNT.incrementAndGet()));
      workerGroup =
          new EpollEventLoopGroup(
              r ->
                  new Thread(
                      r, "Netty Epoll IO Worker Thread #" + WORKER_THREAD_COUNT.incrementAndGet()));
      LOGGER.info("Using epoll channel type");
    } else {
      bossGroup =
          new NioEventLoopGroup(
              r -> new Thread(r, "Netty IO Boss Thread #" + BOSS_THREAD_COUNT.incrementAndGet()));
      workerGroup =
          new NioEventLoopGroup(
              r ->
                  new Thread(
                      r, "Netty IO Worker Thread #" + WORKER_THREAD_COUNT.incrementAndGet()));
      LOGGER.info("Using nio channel type");
    }
  }

  @SuppressWarnings("Convert2Diamond")
  public void start() {
    VoteInboundHandler voteInboundHandler =
        new VoteInboundHandler(ServerConfigInstance.createVoteHandler());
    ChannelFuture future =
        new ServerBootstrap()
            .channel(
                ServerConfigInstance.isEpoll()
                    ? EpollServerSocketChannel.class
                    : NioServerSocketChannel.class)
            .group(bossGroup, workerGroup)
            .childHandler(
                new ChannelInitializer<Channel>() {
                  @Override
                  protected void initChannel(@NotNull Channel ch) throws Exception {
                    try {
                      if (ch instanceof SocketChannel) {
                        ((SocketChannel) ch).config().setTcpNoDelay(true);
                      }
                    } catch (ChannelException ignore) {
                    }
                    ch.attr(VotifierSession.KEY).set(new VotifierSession());
                    ch.pipeline().addLast("greeting_handler", VotifierGreetingHandler.INSTANCE);
                    ch.pipeline()
                        .addLast(
                            "protocol_differentiator",
                            new VotifierProtocolDifferentiator(
                                false, ServerConfigInstance.allowV1));
                    ch.pipeline().addLast("vote_handler", voteInboundHandler);
                  }
                })
            .bind(ServerConfigInstance.listenHost, ServerConfigInstance.listenPort)
            .syncUninterruptibly();
    LOGGER.info("Listening on {}", future.channel().toString());
    futures.add(future);
  }

  public void closeFutures() {
    for (ChannelFuture future : futures) {
      if (future.channel().isActive() || future.channel().isOpen()) {
        LOGGER.info("Closing listener {}", future.channel().toString());
        future.channel().close().syncUninterruptibly();
      }
    }
    futures.clear();
  }

  public void close() {
    closeFutures();
    LOGGER.info("Shutting down event loop");
    workerGroup.shutdownGracefully().syncUninterruptibly();
    bossGroup.shutdownGracefully().syncUninterruptibly();
  }
}
