package com.michael.container.registry.cache.listener.key;

import com.michael.container.registry.enums.Key;
import java.util.Set;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * The key orchestator for all messages from Redis.
 * Uses {@link KeyListener} supports method to direct messages to all KeyListeners
 * which can handle the key.
 */
@Component
public class KeyOrchestrator implements MessageListener {
  private final Set<KeyListener> keyListeners;

  public KeyOrchestrator(Set<KeyListener> keyListenerList) {
    this.keyListeners = keyListenerList;
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    String channel = new String(message.getChannel());
    String body = new String(message.getBody());
    // TODO this might have to be revisited in the future
    Key key = Key.from(channel.split(":")[1], body.split(":")[0]).orElse(null);

    if (key == null) {
      return;
    }

    keyListeners.stream()
        .filter(listener -> listener.supports(key))
        .forEach(keyListener -> keyListener.onMessage(message, pattern));
  }
}
