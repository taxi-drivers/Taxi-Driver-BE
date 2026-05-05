package com.driving.backend.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AreaSummaryRequest {
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;
}
