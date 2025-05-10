package org.example.Repositories;

import org.example.Entities.Victim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VictimsRepository extends JpaRepository<Victim, Long> {
    Optional<Victim> findByIpAddress(String ipAddress);
}