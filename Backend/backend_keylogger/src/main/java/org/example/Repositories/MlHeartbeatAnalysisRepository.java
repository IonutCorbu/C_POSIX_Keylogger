package org.example.Repositories;

import org.example.Entities.MlHeartbeatAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MlHeartbeatAnalysisRepository
        extends JpaRepository<MlHeartbeatAnalysis, Long> {

    Optional<MlHeartbeatAnalysis> findByHeartbeatId(Long heartbeatId);
}