package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "匹配列表")
public class MatchListVO {

    private List<MatchInfoVO> matches = new ArrayList<>();
    private String nextPageToken;

    public List<MatchInfoVO> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchInfoVO> matches) {
        this.matches = matches;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}
