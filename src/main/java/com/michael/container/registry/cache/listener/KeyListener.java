package com.michael.container.registry.cache.listener;

import com.michael.container.registry.cache.enums.Key;
import org.springframework.data.redis.connection.Message;

public interface KeyListener {
  void onMessage(Message message, byte[] pattern);

  boolean supports(Key key);
}
