package org.example.Repositories;

import org.example.Entities.GeminiHeartbeatAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GeminiHeartbeatAnalysisRepository
        extends JpaRepository<GeminiHeartbeatAnalysis, Long> {

    Optional<GeminiHeartbeatAnalysis> findByHeartbeatId(Long heartbeatId);
}
