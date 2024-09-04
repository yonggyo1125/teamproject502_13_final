package org.choongang.global.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(timeToLive = 600L)
public class SessionEntity {

    // 로그인한 경우라면 JWT 토큰, 아니면 guestUid
    @Id
    private String token;
    private String key;
    private String value;
}
