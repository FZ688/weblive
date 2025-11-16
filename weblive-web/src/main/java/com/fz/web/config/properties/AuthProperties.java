package com.fz.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author fz
 * @Date: 2025/1/12-15:19
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private List<String> noAuthUrls;
}
