package org.example.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class HeartbeatAnalyzeResponse {

    private Long heartbeatId;
    private double threshold;
    private boolean cached;
    private List<String> words;

    public HeartbeatAnalyzeResponse(Long heartbeatId,
                                    double threshold,
                                    boolean cached,
                                    List<String> words) {
        this.heartbeatId = heartbeatId;
        this.threshold = threshold;
        this.cached = cached;
        this.words = words;
    }
}