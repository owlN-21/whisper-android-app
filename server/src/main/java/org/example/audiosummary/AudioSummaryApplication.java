package org.example.audiosummary;

import org.example.audiosummary.processing.config.ProcessingServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(ProcessingServiceProperties.class)
@EnableScheduling
public class AudioSummaryApplication {

  public static void main(String[] args) {
    SpringApplication.run(AudioSummaryApplication.class, args);
  }
}
