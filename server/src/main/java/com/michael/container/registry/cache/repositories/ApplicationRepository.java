package com.michael.container.registry.cache.repositories;

import com.michael.container.registry.cache.entity.ApplicationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends CrudRepository<ApplicationEntity, String> {}
