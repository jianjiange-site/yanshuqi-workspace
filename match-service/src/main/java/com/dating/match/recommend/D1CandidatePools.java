package com.dating.match.recommend;

import java.util.ArrayList;
import java.util.List;

/**
 * D1 召回结果：DH / BH 候选池。
 */
public class D1CandidatePools {

    private final List<CandidateProfile> dhCandidates;
    private final List<CandidateProfile> bhCandidates;

    public D1CandidatePools(List<CandidateProfile> dhCandidates, List<CandidateProfile> bhCandidates) {
        this.dhCandidates = dhCandidates == null ? List.of() : new ArrayList<>(dhCandidates);
        this.bhCandidates = bhCandidates == null ? List.of() : new ArrayList<>(bhCandidates);
    }

    public List<CandidateProfile> getDhCandidates() {
        return dhCandidates;
    }

    public List<CandidateProfile> getBhCandidates() {
        return bhCandidates;
    }
}
