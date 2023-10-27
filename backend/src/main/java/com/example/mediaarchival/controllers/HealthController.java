package com.example.mediaarchival.controllers;

import java.sql.Connection;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing health checks of various components of the application.
 * Provides endpoints to check the health of the database and JMS broker.
 */
@RestController
@RequestMapping("/api") // Base path for the endpoints in this controller
public class HealthController {

  @Autowired private DataSource dataSource;

  @Autowired private JmsTemplate jmsTemplate;


  /**
   * Endpoint to check the responsiveness of the service.
   *
   * @return A simple "Pong!" response indicating that the service is running.
   */
  @GetMapping("/ping")
  public ResponseEntity<String> ping() {
    return ResponseEntity.ok("Pong!");
  }

  /**
   * Performs health checks for the database and JMS broker.
   *
   * @return ResponseEntity indicating the overall health status of the service.
   */
  @GetMapping("/health")
  public ResponseEntity<?> healthCheck() {
    Health dbHealth = checkDatabaseHealth();
    Health mqHealth = checkArtemisHealth();

    boolean isUp =
            dbHealth.getStatus().equals(org.springframework.boot.actuate.health.Status.UP)
                    && mqHealth.getStatus().equals(org.springframework.boot.actuate.health.Status.UP);

    if (isUp) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(503).body("Service Unavailable");
    }
  }

  private Health checkDatabaseHealth() {
    try (Connection connection = dataSource.getConnection()) {
      if (connection.isValid(1)) {
        return Health.up().build();
      } else {
        return Health.down().withDetail("Error", "Invalid Database Connection").build();
      }
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }

  private Health checkArtemisHealth() {
    try {
      Objects.requireNonNull(jmsTemplate.getConnectionFactory()).createConnection().close();
      return Health.up().build();
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
