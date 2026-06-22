package com.dating.match.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * ListVisits 业务结果。
 */
public class ListVisitsResult {

    private final List<VisitInfoDto> visits;
    private final String nextPageToken;

    public ListVisitsResult(List<VisitInfoDto> visits, String nextPageToken) {
        this.visits = visits == null ? List.of() : new ArrayList<>(visits);
        this.nextPageToken = nextPageToken == null ? "" : nextPageToken;
    }

    public List<VisitInfoDto> getVisits() {
        return visits;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }
}
