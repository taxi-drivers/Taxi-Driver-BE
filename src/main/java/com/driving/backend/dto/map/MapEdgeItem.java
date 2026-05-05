package com.driving.backend.dto.map;

import lombok.Builder;
import lombok.Getter;

/**
 * 지도 모드 폴리라인 렌더링용 초경량 엣지 DTO.
 * 화면에 필요한 최소 필드만 (id + difficulty + 좌표).
 * name/highway 같은 부가 정보는 클릭 시 별도 조회.
 */
@Getter
@Builder
public class MapEdgeItem {
    private String edgeId;
    private double difficulty;
    private String coordinatesJson;
}
