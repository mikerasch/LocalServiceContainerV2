package com.michael.container.registry.cache.listener.key;

import com.michael.container.distributed.election.enums.MethodAccess;
import com.michael.container.registry.enums.Key;
import org.springframework.data.redis.connection.Message;

public interface KeyListener {
  void onMessage(Message message, byte[] pattern);

  boolean supports(Key key);

  MethodAccess accessLevel();
}
