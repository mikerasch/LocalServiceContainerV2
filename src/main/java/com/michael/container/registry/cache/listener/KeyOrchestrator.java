package com.michael.container.registry.cache.listener;

import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.state.ElectionState;
import com.michael.container.registry.cache.enums.Key;
import jakarta.annotation.Nonnull;
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
  private final ElectionState electionState;

  public KeyOrchestrator(Set<KeyListener> keyListenerList, ElectionState electionState) {
    this.keyListeners = keyListenerList;
    this.electionState = electionState;
  }

  @Override
  public void onMessage(@Nonnull Message message, byte[] pattern) {
    String keyTable = new String(pattern).split(":")[0];

    Key key = Key.from(keyTable).orElse(null);

    if (key == null) {
      return;
    }

    Role currentRole = electionState.getRole();

    keyListeners.stream()
        .filter(listener -> listener.supports(key))
        .forEach(keyListener -> sendMessage(message, pattern, keyListener, currentRole));
  }

  private static void sendMessage(
      Message message, byte[] pattern, KeyListener keyListener, Role role) {
    if (keyListener.accessLevel().canSend(role)) {
      keyListener.onMessage(message, pattern);
    }
  }
}
