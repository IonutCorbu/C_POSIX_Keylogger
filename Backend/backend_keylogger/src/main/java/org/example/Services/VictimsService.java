package org.example.Services;

import lombok.RequiredArgsConstructor;
import org.example.Entities.Victim;
import org.example.Repositories.VictimsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VictimsService {

    private final VictimsRepository victimRepository;

    public Victim getOrCreateByIp(String ip) {
        return victimRepository.findByIpAddress(ip)
                .orElseGet(() -> victimRepository.save(Victim.builder()
                        .ipAddress(ip)
                        .build()));
    }

    public List<Victim> getAllVictims() {
        return victimRepository.findAll();
    }

    public Optional<Victim> getVictim(Long victimId){return victimRepository.findById(victimId);}
}