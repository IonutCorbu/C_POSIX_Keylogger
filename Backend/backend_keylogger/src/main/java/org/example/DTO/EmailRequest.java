package org.example.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
@Data
public class EmailRequest {
    private String to;
    private String subject;
    private String body;
}