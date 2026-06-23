package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "LiveKit 通话 token")
public class CallTokenVO {

    private String token;
    private String roomName;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
