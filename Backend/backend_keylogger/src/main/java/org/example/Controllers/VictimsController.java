package org.example.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.Entities.Victim;
import org.example.Services.VictimsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

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
    public Optional<Victim> getVictim(@PathVariable Long victimId) {
        return victimService.getVictim(victimId);
    }



}
