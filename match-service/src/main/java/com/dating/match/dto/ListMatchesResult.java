package com.dating.match.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * ListMatches 业务结果。
 */
public class ListMatchesResult {

    private final List<MatchInfoDto> matches;
    private final String nextPageToken;

    public ListMatchesResult(List<MatchInfoDto> matches, String nextPageToken) {
        this.matches = matches == null ? List.of() : new ArrayList<>(matches);
        this.nextPageToken = nextPageToken == null ? "" : nextPageToken;
    }

    public List<MatchInfoDto> getMatches() {
        return matches;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }
}
