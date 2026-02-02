package com.arctyll.moderation.data.models;

import java.util.UUID;

public class BanData {
    public UUID playerId;
    public String playerName;
    public String reason;
    public long punishTime;
    public long expireTime;
    public String punishedBy;
    public String banId;

    public BanData(UUID playerId, String playerName, String reason, long punishTime, 
                   long expireTime, String punishedBy, String banId) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.reason = reason;
        this.punishTime = punishTime;
        this.expireTime = expireTime;
        this.punishedBy = punishedBy;
        this.banId = banId;
    }

    public boolean isPermanent() {
        return expireTime == -1;
    }

    public boolean isExpired() {
        if (isPermanent()) return false;
        return System.currentTimeMillis() >= expireTime;
    }

    public long getRemainingTime() {
        if (isPermanent()) return -1;
        return Math.max(0, expireTime - System.currentTimeMillis());
    }
}
