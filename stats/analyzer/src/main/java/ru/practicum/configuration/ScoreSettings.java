package ru.practicum.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analyzer.score")
@Data
public class ScoreSettings {

    private double view ;
    private double registration;
    private double like;

}
