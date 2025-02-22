package com.michael.container.registry.cache.listener;

import com.michael.container.registry.cache.enums.Key;
import com.michael.container.registry.model.RemoveServiceRequest;
import com.michael.container.registry.service.ServiceRegistryService;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Service;

@Service
public class ExpiredKeyListener implements KeyListener {
  private static final Set<Key> SUPPORTED_KEYS = Set.of(Key.INSTANCE_ENTITY);
  private static final Pattern pattern =
      Pattern.compile(
          "^instanceEntity:([a-zA-Z0-9-]+-v\\d+):(\\d+):(http[s]?://[^:/]+)(?::(\\d+))?$");

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
}
