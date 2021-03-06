package net.azisaba.ballotbox.server.util;

import java.sql.SQLException;

public interface SQLThrowableConsumer<T> {
  void accept(T t) throws SQLException;
}
