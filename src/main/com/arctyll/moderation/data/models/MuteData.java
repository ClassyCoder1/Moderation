package com.arctyll.moderation.data.models;

import java.util.UUID;

public class MuteData {
    public UUID playerId;
    public String playerName;
    public String reason;
    public long punishTime;
    public long expireTime;
    public String punishedBy;
    public String muteId;

    public MuteData(UUID playerId, String playerName, String reason, long punishTime, 
					long expireTime, String punishedBy, String muteId) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.reason = reason;
        this.punishTime = punishTime;
        this.expireTime = expireTime;
        this.punishedBy = punishedBy;
        this.muteId = muteId;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expireTime;
    }

    public long getRemainingTime() {
        return Math.max(0, expireTime - System.currentTimeMillis());
    }
}
