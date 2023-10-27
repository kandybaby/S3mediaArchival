package com.example.mediaarchival.converters;

import com.example.mediaarchival.enums.ArchivedStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts a String to an {@link ArchivedStatus} enum.
 */
@Component
public class StringToArchivedStatusConverter implements Converter<String, ArchivedStatus> {

  /**
   * Converts the provided String to an {@link ArchivedStatus} enum.
   *
   * @param source the String to convert
   * @return the corresponding {@link ArchivedStatus} enum
   */
  @Override
  public ArchivedStatus convert(String source) {
    try {
      return ArchivedStatus.valueOf(source.toUpperCase());
    } catch (IllegalArgumentException ex) {
      return ArchivedStatus.NOT_ARCHIVED;
    }
  }
}
