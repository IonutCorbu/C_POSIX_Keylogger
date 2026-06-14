package org.example.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ml_heartbeat_analysis",
        uniqueConstraints = @UniqueConstraint(columnNames = {"heartbeat_id"})
)
@Setter
@Getter
@NoArgsConstructor
public class MlHeartbeatAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "heartbeat_id", nullable = false, unique = true)
    private Long heartbeatId;

    @Lob
    @Column(name = "words_json", nullable = false, columnDefinition = "TEXT")
    private String wordsJson;

    @Column(name = "threshold", nullable = false)
    private double threshold;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}