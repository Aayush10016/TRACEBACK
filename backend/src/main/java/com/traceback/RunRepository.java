package com.traceback;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunRepository extends JpaRepository<Entities.RunEntity, UUID> {
  long countByStatus(String status);
}
