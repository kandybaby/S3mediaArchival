package com.example.mediaarchival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

/**
 * Main entry point for the MediaArchival Spring Boot application.
 * This application is designed to archive media files to S3 storage.
 * The application uses JMS for message handling.
 */
@SpringBootApplication
@EnableJms
public class MediaArchivalApplication {
  /**
   * Main method which starts the application.
   *
   * @param args Command line arguments passed to the application.
   * @throws Exception If the application fails to start.
   */
  public static void main(String[] args) throws Exception {
    SpringApplication.run(MediaArchivalApplication.class, args);
  }
}
