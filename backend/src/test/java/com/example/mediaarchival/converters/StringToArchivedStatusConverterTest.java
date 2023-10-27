package com.example.mediaarchival.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.mediaarchival.enums.ArchivedStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StringToArchivedStatusConverterTest {

  private StringToArchivedStatusConverter converter;

  @BeforeEach
  public void setUp() {
    converter = new StringToArchivedStatusConverter();
  }

  @Test
  public void StringToArchivedStatusConverter_testConvert_ValidValue() {
    ArchivedStatus result = converter.convert("ARCHIVED");

    assertEquals(ArchivedStatus.ARCHIVED, result);
  }

  @Test
  public void StringToArchivedStatusConverter_testConvert_InvalidValue() {
    // Testing an invalid string value
    ArchivedStatus result = converter.convert("INVALID_TYPE");

    // Ensure that it falls back to the default value (OTHER in this case)
    assertEquals(ArchivedStatus.NOT_ARCHIVED, result);
  }
}
