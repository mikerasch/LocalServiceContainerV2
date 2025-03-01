package com.michael.container.registry.cache.listener.key;

import com.michael.container.distributed.election.enums.MethodAccess;
import com.michael.container.registry.cache.enums.Key;
import com.michael.container.registry.model.RemoveServiceRequest;
import com.michael.container.registry.service.ServiceRegistryService;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Service;

@Service
public class ExpiredKeyListener implements KeyListener {
  private static final Set<Key> SUPPORTED_KEYS = Set.of(Key.EXPIRED_INSTANCE_ENTITY);
  private static final Pattern pattern =
      Pattern.compile(
          "^instanceEntity:([a-zA-Z0-9-]+-v\\d+):(\\d+):(http[s]?://[^:/]+)(?::(\\d+))?$");
  private static final Logger log = LoggerFactory.getLogger(ExpiredKeyListener.class);

  private final ServiceRegistryService service;

  public ExpiredKeyListener(ServiceRegistryService service) {
    this.service = service;
  }

  @Override
  public void onMessage(Message message, byte[] bytes) {
    String messageString = new String(message.getBody());

    Matcher matcher = pattern.matcher(messageString);

    if (!matcher.matches()) {
      return;
    }

    log.info("Received TTL Expiration for {}", messageString);

    String applicationName = matcher.group(1);
    String version = matcher.group(2);
    String url = matcher.group(3);
    String port = matcher.group(4);

    service.removeService(
        new RemoveServiceRequest(
            applicationName, url, Integer.parseInt(version), Integer.parseInt(port)));
  }

  @Override
  public boolean supports(Key key) {
    return SUPPORTED_KEYS.contains(key);
  }

  @Override
  public MethodAccess accessLevel() {
    return MethodAccess.LEADER_ONLY;
  }
}
