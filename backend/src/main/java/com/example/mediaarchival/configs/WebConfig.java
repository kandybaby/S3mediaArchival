package com.example.mediaarchival.configs;

import com.example.mediaarchival.converters.StringToArchivedStatusConverter;
import com.example.mediaarchival.converters.StringToMediaTypeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration that registers custom converters for enum types.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  /**
   * Adds custom converters to the application's formatter registry.
   *
   * @param registry the FormatterRegistry to which converters are added
   */
  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringToMediaTypeConverter());
    registry.addConverter(new StringToArchivedStatusConverter());
  }
}
