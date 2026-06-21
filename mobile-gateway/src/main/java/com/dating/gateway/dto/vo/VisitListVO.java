package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "访问列表")
public class VisitListVO {

    private List<VisitInfoVO> visits = new ArrayList<>();
    private String nextPageToken;

    public List<VisitInfoVO> getVisits() {
        return visits;
    }

    public void setVisits(List<VisitInfoVO> visits) {
        this.visits = visits;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}
