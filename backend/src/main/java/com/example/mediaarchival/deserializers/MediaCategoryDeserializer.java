package com.example.mediaarchival.deserializers;

import com.example.mediaarchival.enums.MediaCategory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

/**
 * Custom deserializer for converting JSON strings into {@link MediaCategory} enums.
 */
public class MediaCategoryDeserializer extends JsonDeserializer<MediaCategory> {

  /**
   * Deserializes the JSON string to a {@link MediaCategory} enum.
   *
   * @param p the JsonParser
   * @param ctxt the DeserializationContext
   * @return the deserialized {@link MediaCategory} enum
   * @throws IOException if an input/output error occurs
   */
  @Override
  public MediaCategory deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String value = p.readValueAs(String.class);
    try {
      return MediaCategory.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException ex) {
      return MediaCategory.OTHER;
    }
  }
}
