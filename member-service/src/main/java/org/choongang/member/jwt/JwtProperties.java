package org.choongang.member.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix="jwt")
public class JwtProperties {
    private String secret;
    private Integer validSeconds;
}
