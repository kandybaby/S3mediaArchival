package com.example.mediaarchival.configs;

import jakarta.jms.ConnectionFactory;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

/**
 * Configuration class for setting up Artemis JMS.
 * This class defines the JMS Listener Container Factory required for message-driven POJOs.
 */
@Configuration
public class ArtemisConfig {

  /**
   * Creates a JmsListenerContainerFactory using the provided ConnectionFactory.
   *
   * @param connectionFactory The factory for creating connections to the JMS broker.
   * @param configurer Configurer to apply all necessary Spring Boot defaults.
   * @return A JmsListenerContainerFactory.
   */
  @Bean
  public JmsListenerContainerFactory<?> containerFactory(
          ConnectionFactory connectionFactory,
          DefaultJmsListenerContainerFactoryConfigurer configurer) {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    configurer.configure(factory, connectionFactory);
    return factory;
  }
}
