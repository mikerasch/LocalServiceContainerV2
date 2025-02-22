package com.michael.container.registry.routine;

import com.michael.container.registry.cache.crud.CrudRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RegistryExpirationRoutine {
  private final CrudRegistry crudRegistry;

  public RegistryExpirationRoutine(CrudRegistry crudRegistry) {
    this.crudRegistry = crudRegistry;
  }

  @Scheduled(fixedRate = 2000)
  public void expirationRoutineCheck() {
    //    crudRegistry.fetchAll().values().stream()
    //        .flatMap(map -> map.entrySet().stream())
    //        .filter(entry -> entry.getValue().getStatus() == Status.EXPIRED)
    //        .forEach(
    //            expiredEntry -> {
    //              var registerService = expiredEntry.getKey();
    //              crudRegistry.remove(
    //                  registerService.applicationName(),
    //                  registerService.url(),
    //                  registerService.applicationVersion(),
    //                  registerService.port());
    //            });
  }
}
