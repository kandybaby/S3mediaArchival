package com.example.mediaarchival.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.mediaarchival.enums.MediaCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StringToMediaTypeConverterTest {

  private StringToMediaTypeConverter converter;

  @BeforeEach
  public void setUp() {
    converter = new StringToMediaTypeConverter();
  }

  @Test
  public void StringToMediaTypeConverter_testConvert_ValidValue() {
    MediaCategory result = converter.convert("TV");

    assertEquals(MediaCategory.TV, result);
  }

  @Test
  public void StringToMediaTypeConverter_testConvert_InvalidValue() {
    // Testing an invalid string value
    MediaCategory result = converter.convert("INVALID_TYPE");

    // Ensure that it falls back to the default value (OTHER in this case)
    assertEquals(MediaCategory.OTHER, result);
  }
}
