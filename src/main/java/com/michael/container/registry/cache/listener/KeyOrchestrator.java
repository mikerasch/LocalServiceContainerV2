package com.michael.container.registry.cache.listener;

import com.michael.container.registry.cache.enums.Key;
import java.util.Set;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class KeyOrchestrator implements MessageListener {
  private final Set<KeyListener> keyListeners;

  public KeyOrchestrator(Set<KeyListener> keyListenerList) {
    this.keyListeners = keyListenerList;
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    String keyTable = new String(message.getBody()).split(":")[0];

    Key key = Key.from(keyTable).orElse(null);

    if (key == null) {
      return;
    }

    keyListeners.stream()
        .filter(listener -> listener.supports(key))
        .forEach(keyListener -> keyListener.onMessage(message, pattern));
  }
}
