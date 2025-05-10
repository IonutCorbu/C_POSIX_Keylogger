package org.example.Entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Victim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ipAddress;

    @OneToMany(mappedBy = "victim", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Heartbeat> heartbeats = new ArrayList<>();

}