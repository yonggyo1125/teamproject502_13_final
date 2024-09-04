package org.choongang.global.configs;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableRedisRepositories
public class MvcConfig implements WebMvcConfigurer {
}
