package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "当日 feed 响应")
public class MatchFeedVO {

    @Schema(description = "推荐卡片列表")
    private List<MatchCardVO> cards = new ArrayList<>();

    @Schema(description = "今日卡片是否已耗尽")
    private Boolean exhausted;

    public List<MatchCardVO> getCards() {
        return cards;
    }

    public void setCards(List<MatchCardVO> cards) {
        this.cards = cards;
    }

    public Boolean getExhausted() {
        return exhausted;
    }

    public void setExhausted(Boolean exhausted) {
        this.exhausted = exhausted;
    }
}
