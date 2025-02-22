package com.michael.container.registry.cache.repositories;

import com.michael.container.registry.cache.entity.InstanceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstanceRepository extends CrudRepository<InstanceEntity, String> {}
