package ru.practicum;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "stats-server")
@Component
@Data
public class ConnectionURL {

    private String url;
}