package com.traceback;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtifactRepository extends JpaRepository<Entities.Artifact, UUID> {
  long countByKind(String kind);

  Optional<Entities.Artifact> findFirstByRunIdAndKindOrderByCreatedAtDesc(UUID runId, String kind);
}
