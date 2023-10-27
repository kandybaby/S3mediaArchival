package com.example.mediaarchival.converters;

import com.example.mediaarchival.enums.MediaCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts a String to a {@link MediaCategory} enum.
 */
@Component
public class StringToMediaTypeConverter implements Converter<String, MediaCategory> {

  /**
   * Converts the provided String to a {@link MediaCategory} enum.
   *
   * @param source the String to convert
   * @return the corresponding {@link MediaCategory} enum
   */
  @Override
  public MediaCategory convert(String source) {
    try {
      return MediaCategory.valueOf(source.toUpperCase());
    } catch (IllegalArgumentException ex) {
      // Fallback to OTHER if the string does not match any enum constant
      return MediaCategory.OTHER;
    }
  }
}
