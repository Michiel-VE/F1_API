package be.michielve.f1_api.models.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RaceResultDTO {
    private String raceName;
    private BigDecimal points;
    private String status;
    private Integer lapsCompleted;
}