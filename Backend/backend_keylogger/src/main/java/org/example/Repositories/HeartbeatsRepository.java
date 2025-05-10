package org.example.Repositories;

import org.example.Entities.Heartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeartbeatsRepository extends JpaRepository<Heartbeat, Long> {
    List<Heartbeat> findByVictimId(Long victimId);

    Optional<Heartbeat> findByIdAndVictimId(Long heartbeatId, Long victimId);
}