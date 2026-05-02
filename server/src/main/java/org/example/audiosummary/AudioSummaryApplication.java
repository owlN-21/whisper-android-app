package org.example.audiosummary;

import org.example.audiosummary.processing.config.ProcessingServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ProcessingServiceProperties.class)
public class AudioSummaryApplication {

    public static void main(String[] args){
        SpringApplication.run(AudioSummaryApplication.class, args);
    }
}
