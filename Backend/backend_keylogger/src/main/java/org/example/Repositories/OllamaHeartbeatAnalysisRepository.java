package org.example.Repositories;

import org.example.Entities.OllamaHeartbeatAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OllamaHeartbeatAnalysisRepository
        extends JpaRepository<OllamaHeartbeatAnalysis, Long> {

    Optional<OllamaHeartbeatAnalysis> findByHeartbeatId(Long heartbeatId);
}
